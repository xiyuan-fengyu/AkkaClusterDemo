package com.xiyuan.cluster.launcher

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import com.xiyuan.cluster.actor.{WorkerManagerActor, WorkerActor, MasterActor}
import com.xiyuan.cluster.info.ClusterInfo
import com.xiyuan.cluster.util.ActorSystemFactory
import com.xiyuan.netty.http.HttpServer

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
object Launcher {

  def main(args: Array[String]) {
    var flag =  true
    if (args.length >= 1) {
      args(0) match  {
        case "master" if args.length >= 2 && args(1).matches("[0-9]+") =>
          val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(1)}")
            .withFallback(ConfigFactory.parseString("akka.cluster.roles = [master]"))
            .withFallback(ActorSystemFactory.configFromFile("AkkaCluster.conf"))

          val system = ActorSystem("AkkaCluster", config)
          val master = system.actorOf(Props(new MasterActor(system)), "master")
          ClusterInfo.init(master)
          //启动内置服务器，提供管理界面
          new HttpServer()

        case "workerManager" if args.length >= 3 && args(1).matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}") && args(2).matches("[0-9]+") =>
          val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname=${args(1)}")
            .withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(2)}"))
            .withFallback(ConfigFactory.parseString("akka.cluster.roles = [workerManager]"))
            .withFallback(ActorSystemFactory.configFromFile("AkkaCluster.conf"))

          val system = ActorSystem("AkkaCluster", config)
          val workerManager = system.actorOf(Props(new WorkerManagerActor(system)), "workerManager")

//        case "worker" if args.length >= 2 && args(1).matches("[0-9]+") =>
//          val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(1)}")
//            .withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker]"))
//            .withFallback(ActorSystemFactory.configFromFile("AkkaCluster.conf"))
//
//          val system = ActorSystem("AkkaCluster", config)
//          val worker = system.actorOf(Props(new WorkerActor(system)), "worker")
//          Thread.sleep(2000)
//          worker ! "sendHello"

        case _ =>
          flag = false
      }
    }
    else flag = false

    if (!flag) {
      println("参数有误，启动命令如下：")
      println("启动worker：java -jar *.jar master <端口>")
      println("启动workerManager：java -jar *.jar workerManager <ip> <端口>")
    }

  }

}
