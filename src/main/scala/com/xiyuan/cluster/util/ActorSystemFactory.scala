package com.xiyuan.cluster.util

import java.io.InputStreamReader

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by YT on 2016/5/23.
  */
object ActorSystemFactory {

  private var _sysDefault: ActorSystem = null

  def sysDefault = if(_sysDefault == null) {
    _sysDefault = ActorSystem("default")
    _sysDefault
  }
  else _sysDefault

  def createFromFile(fileName: String): ActorSystem = {
    ActorSystem(fileName.split("\\.")(0), configFromFile(fileName))
  }

  def configFromFile(fileName: String): Config = {
    ConfigFactory.parseReader(new InputStreamReader(this.getClass.getResourceAsStream("/" + fileName))).resolve()
  }

}
