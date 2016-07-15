package com.xiyuan.cluster.actor

import akka.actor.ActorSystem
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.xiyuan.cluster.info.ClusterInfo

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class MasterActor(sys: ActorSystem) extends ClusterItemActor(sys = sys) {

  override def receive: Receive = {
    case MemberUp(member) =>
      if (member.hasRole("master")) {
        ClusterInfo.masterAddress = self.path.toStringWithAddress(member.address)
      }
    case MemberRemoved(member, status) =>
      println(member)
      println(status)

    case msg: String =>
      println(msg)
  }

}
