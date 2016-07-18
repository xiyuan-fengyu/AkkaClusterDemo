package com.xiyuan.cluster.info

import akka.actor.ActorRef

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
object ClusterInfo {

  private var _master: ActorRef = null

  def master = _master

  var masterAddress: String = ""

  def init(master: ActorRef): Unit = {
    _master = master
  }

  var workerManagers = new mutable.HashMap[String, ArrayBuffer[String]]()

  def getWorkerManagerByIpPort(ip: String, port: Int): String = {
    val keyWord = ip + ":" + port
    val finder = workerManagers.find(_._1.contains(keyWord))
    if (finder.nonEmpty) finder.get._1
    else null
  }

}
