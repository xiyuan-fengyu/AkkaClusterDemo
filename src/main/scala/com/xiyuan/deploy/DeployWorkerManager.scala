package com.xiyuan.deploy

import com.xiyuan.util.LinuxUtil

import scala.collection.mutable

/**
  * Created by xiyuan_fengyu on 2016/7/18.
  */
object DeployWorkerManager {

  def deploy(host: String, port: Int): Unit = {
    val account = LinuxAccount(host)
    if (account != null) {
      LinuxUtil.killByPort(port, host, account.username, account.password)

      val fileMap = new mutable.HashMap[String, Boolean]()

      val dirStatus = LinuxUtil.pathType(DeployMaster.masterDir, host, account.username, account.password)
      if (dirStatus != 1) {
        if (dirStatus == 0) {
          LinuxUtil.sshExecute("sudo rm " + DeployMaster.masterDir, host, account.username, account.password)
        }
        LinuxUtil.mkdirs(DeployMaster.masterDir, host, account.username, account.password)
      }
      else {
        LinuxUtil.ls(DeployMaster.masterDir, host, account.username, account.password).foreach(fileMap.update(_, true))
      }

      val scpCmdBld = new StringBuilder()
      val filesOnMaster = LinuxUtil.ls(DeployMaster.masterDir, DeployMaster.masterHost, DeployMaster.masterUsername, DeployMaster.masterPassword)
      filesOnMaster.foreach(f => {
        if (!fileMap.contains(f) || f.equals(DeployMaster.outMainJarName)) {
          scpCmdBld.append("scp -r " + DeployMaster.masterDir + "/" + f + " " + host + ":" + DeployMaster.masterDir + "; ")
        }
      })
      LinuxUtil.sshExecute(scpCmdBld.toString(), DeployMaster.masterHost, DeployMaster.masterUsername, DeployMaster.masterPassword)

      //启动 workerMaster
      LinuxUtil.sshExecute("source /etc/profile; java -jar " + DeployMaster.masterDir + "/" + DeployMaster.outMainJarName + " workerManager " + host + " " + port, host, account.username, account.password, ignoreStd = true)
    }

  }

}
