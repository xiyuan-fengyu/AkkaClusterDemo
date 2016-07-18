package com.xiyuan.cluster.actor

import akka.actor.ActorSystem
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.xiyuan.cluster.info.ClusterInfo
import com.xiyuan.cluster.msg.{WorkerRegiste, NewWorker, StopWorkerManager, StopAll}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class MasterActor(sys: ActorSystem) extends ClusterItemActor(sys = sys) {

  override def receive: Receive = {
    case MemberUp(member) =>
      if (member.hasRole("master")) {
        ClusterInfo.masterAddress = self.path.toStringWithAddress(member.address)
      }
      else if (member.hasRole("workerManager")) {
        ClusterInfo.workerManagers.update(member.address.toString + "/user/workerManager", new ArrayBuffer[String]())
      }
      else {
        println(member.address.toString)
        member.getRoles.toArray().foreach(println)
      }

    case MemberRemoved(member, status) =>
      println(member)
      println(status)
      if (member.hasRole("workerManager")) {
        ClusterInfo.workerManagers -= member.address.toString + "/user/workerManager"
      }

    case StopAll() =>
      ClusterInfo.workerManagers.foreach(m => {
        sys.actorSelection(m._1) ! new StopWorkerManager
      })

    case newWorker: NewWorker =>
      sys.actorSelection(newWorker.manager) ! newWorker

    case WorkerRegiste(name) =>
      val manager = sender().path.toString
      ClusterInfo.workerManagers(manager) += name

    case msg: String =>
      println(msg)
      println(sender().path)
  }

}
