package com.xiyuan.netty.dispatcher.viewParser

import com.xiyuan.netty.dispatcher.ViewModel

/**
  * Created by xiyuan_fengyu on 2016/7/14.
  */
trait ViewParser {
  def isParseNecessary(html: String): Boolean
  def parse(html: String, model: ViewModel): String
}
