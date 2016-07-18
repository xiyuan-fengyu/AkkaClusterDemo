package com.xiyuan.cluster.msg

/**
  * Created by xiyuan_fengyu on 2016/7/18.
  */
class MsgCase {}

case class StopAll()

case class StopWorkerManager()

case class NewWorker(manager: String, uniqueName: String)

case class WorkerRegiste(name: String)