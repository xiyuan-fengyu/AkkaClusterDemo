package com.xiyuan.cluster.actor

import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.xiyuan.cluster.info.ClusterInfo
import com.xiyuan.cluster.msg.{WorkerRegiste, StopWorkerManager, NewWorker}

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class WorkerManagerActor(sys: ActorSystem) extends ClusterItemActor(sys = sys) {

  override def receive: Receive = {
    case MemberUp(member) =>
      println(member.address.toString)
      member.getRoles.toArray().foreach(println)

    case MemberRemoved(member, status) =>
      if (member.hasRole("master")) {
        context.actorSelection("*") ! PoisonPill
        sys.shutdown()
      }

    case newWorker: NewWorker =>
      val workerName = newWorker.uniqueName
      val worker = context.actorOf(Props(new WorkerActor(sys)), workerName)
      sender() ! new WorkerRegiste(workerName)

    case StopWorkerManager() =>
      context.actorSelection("*") ! PoisonPill
      sys.shutdown()
  }

}
