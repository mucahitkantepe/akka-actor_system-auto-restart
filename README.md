### Restarting Actor System using Cake Pattern

Suppose that you have a multi node Akka Cluster that communicates over network. There might be problems that block communication, maybe at least one of your node is under heavy load, they will not be able to 
communicate in a healthy manner and you are going to see some log messages like

`Association to [akka.tcp://Main@1192.168.2.41:2555] having UID [-1595655826] is irrecoverably failed.
 UID is now quarantined and all messages to this UID will be delivered to dead letters.
 Remote actorsystem must be restarted to recover from this situation.`

You can find further information for quarantine state in this [article](https://livingston.io/understanding-akkas-quarantine-state/).

If Actor System is not restarted, you will see those kind of warning logs
` Couldn't join seed nodes after [2] attempts, will try again. seed-nodes=[akka://Main@192.168.1.136:2556]`

So, now your node is unreachable by other nodes, but it still runs (might be harmful).
To make it run healthier again, we should restart the Actor System and initialize evey piece of our "environment".

Rest of the tutorial will be based on Cake Pattern, if you are not familiar with Cake Pattern, there are some nice articles:[1](https://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth), [2](https://medium.com/@itseranga/scala-cake-pattern-e0cd894dae4e), [3](http://jonasboner.com/real-world-scala-dependency-injection-di/) 
 
 Let's get start with our first and foremost cake: 
 
 **Akka System Component**
 
 ```scala
 trait AkkaSystemComponent {
 
   val akka: AkkaSystem
   
   trait AkkaSystem {
    val system: ActorSystem
   }
 
 }
 
 trait DefaultAkkaSystemComponent extends AkkaSystemComponent {
 
   override lazy val akka: DefaultAkkaSystem = new DefaultAkkaSystem
 
   class DefaultAkkaSystem extends AkkaSystem {
 
     override val system: ActorSystem = ActorSystem("Main")
 
   }
 }
 ```
 You can use this cake to inject ActorSystem dependency that you will need sometimes, also in project, i added some additional
 fields like selfAddress, actorMaterializer, etc.
 
 And here is our Mock cake that simply does nothing. You should replace your own cakes here.
 
 **Mock Service Component**
 This component compromised of MockService and MockServiceActor. I divided them to just to be able to test bussiness logic class and Actor separately and easily.
 
 ```scala
trait MockServiceComponent {
  val mockService: MockService
  val mockServiceActor: ActorRef
}

trait MockService {
  def init(implicit context: ActorContext): Unit
}

class MockServiceActor(mockService: MockService) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("MockService started")

  override def receive = Actor.emptyBehavior
}

object MockServiceActor {
  def props(mockService: MockService): Props = Props(new MockServiceActor(mockService))
}

trait DefaultMockServiceComponent extends MockServiceComponent {
  this: AkkaSystemComponent =>

  override val mockService: MockService = new DefaultMockService

  override val mockServiceActor: ActorRef = akka.system.actorOf(MockServiceActor.props(mockService))
}

class DefaultMockService extends MockService {
  override def init(implicit context: ActorContext): Unit = ()
}
 ```
 
 Now we have an "environment" class that includes everything we need when we restart the system.
 
 So lets get start with EnvironmentRestarter class.
 
 We need to restart ActorSystem and get rid of running harmful threads(or actors)
 
 ```scala
  object EnvironmentRestarter extends LazyLogging {
 
   @tailrec
   def startAndSupervise(environment: => BaseEnvironment): Unit = {
     logger.info(s"Environment is starting...")
 
     //wait until ActorSystem terminates
     Await.result(environment.akka.system.whenTerminated, Duration.Inf)
 
     logger.warn("ActorSystem is terminated, starting again in 10 seconds...")
     Thread.sleep(10000)
 
     startAndSupervise(environment)
   }
 } 
 ```
 `startAndSupervise` method stars the given environment and waits for the Actor Systeme to be terminated, when it terminates,
 it will start the environment again and again...
 
 