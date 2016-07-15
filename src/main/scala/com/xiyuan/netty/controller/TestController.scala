package com.xiyuan.netty.controller

import com.xiyuan.netty.annotation.{View, RequestMapping, Controller}
import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.model.TestParam

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
@Controller
class TestController {

  @RequestMapping(value="")
  @View
  def index(model: ViewModel): String = {
    model += (("int", 1))
    model += (("string", "string"))
    model += (("boolean", true))

    val testParam0 = new TestParam()
    testParam0.ids = Array(1, 2)
    testParam0.msg = "test msg0"
    val testParam1 = new TestParam()
    testParam1.ids = Array(1, 2)
    testParam1.msg = "test msg1"
    model += (("list0", Array[AnyRef](testParam0)))
    model += (("list1", Array[AnyRef](testParam1)))
    model += (("obj", testParam0))

    model += (("conditions", Array(false, false, true)))

    "web/page/index.html"
  }

  @RequestMapping(value="test")
  def test(testParam: TestParam, model: ViewModel): String = {
    s"${testParam.ids.map(_ + "\t").reduce(_ + _)}${testParam.msg}"
  }

}
