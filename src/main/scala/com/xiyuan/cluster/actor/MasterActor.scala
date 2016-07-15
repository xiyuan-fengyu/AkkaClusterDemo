package com.xiyuan.cluster.actor

import akka.actor.ActorSystem
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class MasterActor(sys: ActorSystem) extends ClusterItemActor(sys = sys) {

  override def receive: Receive = {
    case MemberUp(member) =>
      println(member.address)//akka.tcp://AkkaCluster@192.168.1.66:2552
    case MemberRemoved(member, status) =>
      println(member)
      println(status)

    case msg: String =>
      println(msg)
  }

}
