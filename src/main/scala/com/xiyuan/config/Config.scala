package com.xiyuan.config

import com.xiyuan.netty.util.ConfigUtil

object Config {

	private val properties = ConfigUtil.loadProperties("HttpServerConfig.properties")

	val packageToScan = properties.getProperty("packageToScan")

}