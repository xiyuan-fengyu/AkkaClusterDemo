package com.xiyuan

import java.io.File

import com.xiyuan.util.LinuxUtil

import scala.collection.mutable

/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
object DeployMaster {

  val masterHost = "192.168.1.240"

  val masterUsername = "root"

  val masterPassword = "111111"

  val masterPort = 2550

  val masterDir = "/root/workplace/akkaDemo"

  //是主要jar包的路径，上传的时候会将该jar所在目录的jar包全部上传
  val outMainJarPath = "./out/artifacts/AkkaClusterDemo_jar/AkkaClusterDemo.jar"

  def main(args: Array[String]) {
    //杀死暂用 2550 端口的进程
    LinuxUtil.killByPort(masterPort, masterHost, masterUsername, masterPassword)

    //在 masterHost 中创建部署文件夹
    LinuxUtil.mkdirs(masterDir, masterHost, masterUsername, masterPassword)

    //获取已上传的文件列表
    val uploadedMap = new mutable.HashMap[String, Boolean]()
    LinuxUtil.ls(masterDir, masterHost, masterUsername, masterPassword).foreach(uploadedMap.update(_, true))

    //将build之后的所有 jar 包拷上传到该目录
    val outMainJarFile = new File(outMainJarPath)
    val outMainJarName = outMainJarFile.getName
    val files = outMainJarFile.getParentFile.listFiles()
    var index = 1
    files.foreach(f => {
      val fPath = f.getPath
      val fName = f.getName
      println(s"正在上传 $fPath ，进度：$index / ${files.length}")

      //避免非主要jar包的重复上传
      if (!uploadedMap.contains(fName) || fName.equals(outMainJarName)) {
        LinuxUtil.uploadFile(fPath, masterHost, masterUsername, masterPassword, masterDir)
      }
      else {
        println(masterDir + "/" + fName + " 已存在")
      }

      index += 1
    })

    //启动 master
    LinuxUtil.sshExecute("source /etc/profile; java -jar " + masterDir + "/" + outMainJarName + " master " + masterPort, masterHost, masterUsername, masterPassword)
  }

}
