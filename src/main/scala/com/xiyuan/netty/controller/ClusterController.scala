package com.xiyuan.netty.controller

import com.xiyuan.cluster.info.ClusterInfo
import com.xiyuan.netty.annotation.{View, RequestMapping, Controller}
import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.model.MasterInfo

/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
@Controller
class ClusterController {

  @RequestMapping(value="")
  @View
  def index(model: ViewModel): String = {
    val masterInfo = new MasterInfo()
    masterInfo.address = ClusterInfo.masterAddress
    model += (("masterInfo", masterInfo))
    "web/page/index.html"
  }

}
