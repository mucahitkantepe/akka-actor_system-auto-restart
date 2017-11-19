package com.mucahit.component

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props}

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


