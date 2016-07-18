package com.xiyuan.netty.http

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import com.xiyuan.netty.config.HttpServerConfig
import com.xiyuan.netty.dispatcher.DispatchCenter
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
class HttpServer {

  private def init(): Unit = {
    DispatchCenter.init()

    val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))
    bootstrap.setPipelineFactory(new HttpPipelineFactory)
    bootstrap.bind(new InetSocketAddress(HttpServerConfig.port)).getCloseFuture.sync()
  }
  init()

}
