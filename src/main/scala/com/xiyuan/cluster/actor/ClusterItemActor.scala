package com.xiyuan.cluster.actor

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
abstract class ClusterItemActor(val sys: ActorSystem) extends Actor with ActorLogging {

  val cluster = Cluster(sys)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberUp], classOf[UnreachableMember], classOf[MemberEvent])
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = cluster.unsubscribe(self)

}
