package com.xiyuan.netty.controller

import java.util.Date

import com.google.gson.JsonObject
import com.xiyuan.cluster.info.ClusterInfo
import com.xiyuan.cluster.msg.{NewWorker, StopAll}
import com.xiyuan.deploy.{DeployMaster, DeployWorkerManager, LinuxAccount}
import com.xiyuan.netty.annotation.{RequestMapping, View, Controller}
import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.model.MasterInfo
import com.xiyuan.util.LinuxUtil

/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
@Controller
class ClusterController {

  @RequestMapping(value="")
  @View
  def index(model: ViewModel): String = {
    val masterInfo = new MasterInfo()
    masterInfo.address = ClusterInfo.masterAddress
    model += (("masterInfo", masterInfo))
    model += (("workerManagers", ClusterInfo.workerManagers.toArray.map(item => (item._1, item._2.toArray))))
    "web/page/index.html"
  }

  @RequestMapping(value="newWorkerManager")
  def newWorkerManager(host: String, port: Int): JsonObject = {
    val result = new JsonObject

    val account = LinuxAccount(host)
    if (account != null) {
      val isPortFree = LinuxUtil.isPortFree(port, host, account.username, account.password)
      if (isPortFree) {
        DeployWorkerManager.deploy(host, port)
        result.addProperty("success", true)
        result.addProperty("message", "workerManager添加成功")
      }
      else {
        result.addProperty("success", false)
        result.addProperty("message", s"主机${host}的${port}已被占用")
      }
    }
    else {
      result.addProperty("success", false)
      result.addProperty("message", s"没有主机${host}可用的登录用户信息")
    }

    result
  }

  @RequestMapping(value="newWorker")
  def newWorker(manager: String): JsonObject = {
    val result = new JsonObject

    if (ClusterInfo.workerManagers.contains(manager)) {
      val uniqueName = "worker_" + new Date().getTime + "_" + (10000 * math.random).toInt
      ClusterInfo.master ! new NewWorker(manager, uniqueName)

      var waitIndex = 0
      while (waitIndex < 10) {
        if (ClusterInfo.workerManagers(manager).indexOf(uniqueName) > -1) {
          waitIndex = 100
        }
        else {
          Thread.sleep(500)
          waitIndex += 1
        }
      }
      if (waitIndex == 100) {
        result.addProperty("success", true)
      }
      else {
        result.addProperty("success", false)
      }
    }
    else {
      result.addProperty("success", false)
      result.addProperty("message", s"${manager}不存在")
    }
    result
  }

  @RequestMapping(value="stop")
  def stop(): String = {
    ClusterInfo.master ! new StopAll
    while (ClusterInfo.workerManagers.nonEmpty) {
      Thread.sleep(250)
    }
    DeployMaster.stop()
    ""
  }

}
