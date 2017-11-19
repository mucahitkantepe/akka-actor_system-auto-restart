package com.mucahit.environment

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
