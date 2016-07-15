package com.xiyuan.cluster.info

import akka.actor.ActorRef

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

}
