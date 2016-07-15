package com.xiyuan.netty.dispatcher.viewParser.simple.tags

import java.util.Date
import java.util.regex.Matcher

import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.dispatcher.viewParser.simple.ViewParserSimple

import scala.collection.mutable
/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
class ViewForTag(number: Int, matcher: Matcher) {

  val tag = s"VIEW_FOR_TAG_${number}_${new Date().getTime}"

  val str = matcher.group(1)

  def init(): (String, String, String, String) = {
    var list: String = null
    var item: String = null
    var index: String = null
    val content: String = matcher.group(matcher.groupCount())
    for (i <- 0 until matcher.groupCount()) {
      val t = matcher.group(i + 1)
      if (t != null) {
        val temp = t.replaceAll(" ", "")
        if (temp.startsWith("list=\"")) {
          list = temp.substring(8, temp.length - 2)
        }
        else if (temp.startsWith("item=\"")) {
          item = temp.substring(6, temp.length - 1)
        }
        else if (temp.startsWith("index=\"")) {
          index = temp.substring(7, temp.length - 1)
        }
      }
    }
    (list, item, index, content)
  }
  private val analysisResult = init()

  val listStr = analysisResult._1
  val listNodes = ViewParserSimple.nodesStrToNodesArr(listStr)
  val item = analysisResult._2
  val index = analysisResult._3
  val content = analysisResult._4

  def parse(matchMap: mutable.HashMap[String, ViewForTag], model: ViewModel, parentItem: ViewModel): String = {
    val strBld = new StringBuilder()

    try {
      var tempData = ViewParserSimple.getDataByNodes(model, parentItem, listNodes)
      if (tempData != null) {
        if (!ViewParserSimple.isArray(tempData)) {
          tempData = Array[Any](tempData)
        }
        val tempDataArr = tempData.asInstanceOf[Array[_]]
        for(i <- tempDataArr.indices) {
          val data = tempDataArr(i)
          parentItem += ((item, data))
          if (index != null && index != "") {
            parentItem += ((index, i))
          }
          strBld.append(ViewParserSimple.parseContent(content, matchMap, model, parentItem))
        }
      }
    }
    catch  {
      case e: Exception =>
        e.printStackTrace()
        strBld.clear()
    }

    strBld.toString()
  }

}
