package com.xiyuan.deploy

import com.xiyuan.util.LinuxUtil

/**
  * Created by xiyuan_fengyu on 2016/7/18 11:16.
  */
object StopMaster {

  def main(args: Array[String]) {
    LinuxUtil.killByPort(DeployMaster.masterPort, DeployMaster.masterHost, DeployMaster.masterUsername, DeployMaster.masterPassword)
  }

}