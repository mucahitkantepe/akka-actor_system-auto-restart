package com.mucahit.component

import akka.actor.{ActorSystem, Address}
import akka.cluster.{Cluster, Member}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

trait AkkaSystemComponent {

  val akka: AkkaSystem

  trait AkkaSystem {

    implicit val system: ActorSystem
    implicit val actorMaterializer: ActorMaterializer
    val config: Config
    val cluster: Cluster
    val selfMember: Member
    val selfAddress: Address

  }

}


trait DefaultAkkaSystemComponent extends AkkaSystemComponent {

  override lazy val akka: DefaultAkkaSystem = new DefaultAkkaSystem

  class DefaultAkkaSystem extends AkkaSystem {

    override implicit val system: ActorSystem = ActorSystem("Main")
    override implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    override val config: Config = ConfigFactory.load()
    override val cluster: Cluster = Cluster(system)
    override val selfMember: Member = cluster.selfMember
    override val selfAddress: Address = selfMember.address

  }

}

