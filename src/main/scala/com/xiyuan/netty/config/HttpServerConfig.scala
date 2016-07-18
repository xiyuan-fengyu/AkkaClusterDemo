package com.xiyuan.netty.config

import com.xiyuan.netty.util.ConfigUtil

object HttpServerConfig {

	private val properties = ConfigUtil.loadProperties("HttpServerConfig.properties")

	val packageToScan = properties.getProperty("packageToScan")

	val port = properties.getProperty("port").toInt

	val htmlParser = properties.getProperty("htmlParser")

	val htmlFor404 = properties.getProperty("htmlFor404")

}