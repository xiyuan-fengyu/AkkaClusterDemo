package com.xiyuan.cluster.actor

import akka.actor.ActorSystem

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class WorkerActor(sys: ActorSystem) extends ClusterItemActor(sys = sys) {

  override def receive: Receive = {
    case "sendHello" =>
      sys.actorSelection("akka.tcp://AkkaCluster@192.168.1.66:2550/user/master") ! "hello"
    case msg: String =>
      println(msg)
  }

}
