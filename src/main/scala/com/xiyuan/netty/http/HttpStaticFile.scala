package com.xiyuan.netty.http

import scala.io.Source

/**
  * Created by xiyuan_fengyu on 2016/7/14.
  */
object HttpStaticFile {

  def get(path: String): String = {
    if (path == null) {
      null
    }
    else {
      val removeParamsPath = path.split("\\?")(0)
      val tempPath = if (removeParamsPath.startsWith("/")) removeParamsPath.substring(1) else removeParamsPath
      try {
        val tempUrl = this.getClass.getClassLoader.getResource(tempPath)
        if (tempUrl != null) {
          Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(tempPath)).getLines().map(_ + "\n").reduce(_ + _)
        }
        else null
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
  }

}
