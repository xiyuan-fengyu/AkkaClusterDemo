package com.xiyuan.cluster.actor

import akka.actor.{ActorLogging, Actor, ActorSystem}
import com.xiyuan.cluster.msg.WorkerRegiste
import com.xiyuan.deploy.DeployMaster

/**
  * Created by xiyuan_fengyu on 2016/7/12.
  */
class WorkerActor(sys: ActorSystem) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: String =>

  }

}
