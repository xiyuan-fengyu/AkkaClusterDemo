package com.xiyuan.netty.dispatcher.viewParser.simple

import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.dispatcher.viewParser.ViewParser
import com.xiyuan.netty.dispatcher.viewParser.simple.tags.{ViewCaseTag, ViewSwitchTag, ViewForTag}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

/**
  * Created by xiyuan_fengyu on 2016/7/14.
  */
object ViewParserSimple {

  val modelValueRegex = new Regex(".*(\\$\\{(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1}))?\\}?).*")

  val chooseValueRegex = new Regex(".*(\\$\\{( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *)((<|<=|==|!=|>=|>?)( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})+|\".*?\"|[0-9.]+|true|false)? *)){0,1}\\?( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *):( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *)\\}?).*")

  val dataNodesRegex = new Regex("( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})")

  val dataStrRegex = new Regex("\".*?\"")

  val dataNumberRegex = new Regex("[0-9]+")

  val dataBooleanRegex = new Regex("true|false")

  val forRegex = new Regex(".*(< {0,4}view {0,4}: {0,4}for {0,4}(?<=< {0,4}view {0,4}: {0,4}for {0,4}[^>]{0,255})(list {0,4}= {0,4}\" *\\$\\{( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})\\} *\" {0,4}?)(?=[^>]{0,255}>) {0,4}(?<=< {0,4}view {0,4}: {0,4}for {0,4}[^>]{0,255})(item {0,4}= {0,4}\"[^ ]+\"?)(?=[^>]{0,255}>) {0,4}(?<=< {0,4}view {0,4}: {0,4}for {0,4}[^>]{0,255})((index {0,4}= {0,4}\"[^ ]+\" {0,4}){0,1}?)(?=[^>]{0,255}>) {0,4}>(.*?)</ {0,4}view {0,4}: {0,4}for {0,4}>?).*")

  val viewForTagRegex = new Regex(".*(VIEW_FOR_TAG_[0-9]+_[0-9]{13,13}?).*")

  val switchRegex = new Regex(".*(< *view *: *switch *> *(< *view *: *case *condition *= *\"\\$\\{( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *)((<|<=|==|!=|>=|>?)( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})+|\".*?\"|[0-9.]+|true|false)? *)){0,1}\\}\" *>(.*?)</ *view *: *case *>)* *</ *view *: *switch *>).*")

  val caseRegex = new Regex(".*(< *view *: *case *condition *= *\" *(\\$\\{( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *)((<|<=|==|!=|>=|>)( *(( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1} *\\.)*( *[^{}().\"?: ]+ *(\\( *[0-9]+ *\\)){0,1})|\".*?\"|[0-9.]+|true|false) *)){0,1}\\}|true) *\" *>(.*?)</ *view *: *case *>).*")

  def nodesStrToNodesArr(str: String): Array[(String, Int)] = {
    val listStrSplit = str.split("\\.")
    val buffer = new ArrayBuffer[(String, Int)]()
    val nameAndIndexRegex = new Regex("([^{}().\"\\?:]+?)\\(([0-9]+?)\\)")
    listStrSplit.foreach(item => {
      if (item.matches(nameAndIndexRegex.toString())) {
        val nameAndIndexRegex(n, i) = item
        buffer += ((n, i.toInt))
      }
      else buffer += ((item, -1))
    })
    buffer.toArray
  }

  def getDataByNodes(model: ViewModel, parentItem: ViewModel, nodes: Array[(String, Int)]): Any = {
    val firstNodeName = nodes(0)._1
    val firstNodeIndex = nodes(0)._2
    var tempData = if (model.contains(firstNodeName)) {
      val modelData = model(firstNodeName)
      if (firstNodeIndex == -1) modelData
      else if (firstNodeIndex > -1 && isArray(modelData)) {
        modelData.asInstanceOf[Array[_]](firstNodeIndex)
      }
      else null
    }
    else if (parentItem.contains(firstNodeName)) {
      val modelData = parentItem(firstNodeName)
      if (firstNodeIndex == -1) modelData
      else if (firstNodeIndex > -1 && isArray(modelData)) {
        modelData.asInstanceOf[Array[_]](firstNodeIndex)
      }
      else null
    }
    else null

    if (tempData != null) {
      for (i <- 1 until nodes.length; if tempData != null) {
        val nodeName = nodes(i)._1
        val nodeIndex = nodes(i)._2
        if (tempData.getClass == classOf[mutable.HashMap[String, _]]) {
          val tempMap = tempData.asInstanceOf[mutable.HashMap[String, _]]
          if (tempMap.contains(nodeName)) {
            val modelData = tempMap(nodeName)
            if (nodeIndex == -1) tempData = modelData
            else if (nodeIndex > -1 && isArray(modelData)) {
              tempData = modelData.asInstanceOf[Array[_]](nodeIndex)
            }
            else tempData = null
          }
          else tempData =  null
        }
        else {
          tempData.getClass.getDeclaredFields.exists(f => {
            val flag = f.getName == nodeName
            if (flag) {
              f.setAccessible(true)
              if (nodeIndex == -1) tempData = f.get(tempData)
              else if (nodeIndex > -1 && isArray(f.getType)) tempData = f.get(tempData).asInstanceOf[Array[_]](nodeIndex)
              else tempData = null
            }
            flag
          })
        }
      }
    }
    tempData
  }

  def isArray(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.startsWith("class [")
  }

  def isBoolean(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Boolean|boolean")
  }

  def isDouble(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Double|double")
  }

  def isInt(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Integer|int")
  }

  def isLong(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Long|long")
  }

  def isFloat(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Float|float")
  }

  def isShort(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.Short|short")
  }

  def isString(tempData: Any): Boolean = {
    tempData != null && tempData.getClass.toString.matches("class java.lang.String")
  }

  def parseContent(content: String, matchMap: mutable.HashMap[String, ViewForTag], model: ViewModel, parentItem: ViewModel): String = {
    var str = content

    //解析switchTag
    while (str.matches(ViewParserSimple.switchRegex.toString())) {
      val matcher = ViewParserSimple.switchRegex.pattern.matcher(str)
      if (matcher.find()) {
        val switchTag = new ViewSwitchTag(matcher)
        var trueCaseTag: ViewCaseTag = null
        switchTag.caseArr.exists(caseTag => {
          var flag = false
          if (caseTag.condition == "true") {
            trueCaseTag = caseTag
            flag = true
          }
          else if (caseTag.exp1 == null) {
            throw new Exception(s"${switchTag.switchStr}中${caseTag.caseStr}的条件${caseTag.condition}格式有误")
          }
          else {
            if (caseTag.exp2 == null && (caseTag.option == "" || caseTag.option == null)) {
              val exp1Value = getExpressValue(caseTag.exp1, model, parentItem)
              if (isBoolean(exp1Value)) {
                if (exp1Value.asInstanceOf[Boolean]) {
                  trueCaseTag = caseTag
                  flag = true
                }
              }
              else {
                throw new Exception(s"${switchTag.switchStr}中${caseTag.caseStr}的条件${caseTag.condition}的值($exp1Value)不是boolean类型")
              }
            }
            else if (caseTag.exp2 != null && caseTag.option != "" && caseTag.option != null) {
              val exp1Value = getExpressValue(caseTag.exp1, model, parentItem)
              val exp2Value = getExpressValue(caseTag.exp2, model, parentItem)
              if (exp1Value.getClass == exp2Value.getClass) {
                if (compareOption(exp1Value, exp2Value, caseTag.option)) {
                  trueCaseTag = caseTag
                  flag = true
                }
              }
              else {
                throw new Exception(s"${switchTag.switchStr}中${caseTag.caseStr}的条件${caseTag.condition}的两个表达式值类型不一致，不能比较:${caseTag.exp1}=$exp1Value,${caseTag.exp2}=$exp2Value")
              }
            }
            else {
              throw new Exception(s"${switchTag.switchStr}中${caseTag.caseStr}的条件${caseTag.condition}格式有误")
            }
          }

          flag
        })

        if (trueCaseTag != null) {
          str = str.replace(switchTag.switchStr, trueCaseTag.content)
        }
        else {
          str = str.replace(switchTag.switchStr, "")
        }
      }
    }


    //解析三元式${a <|<=|==|>=|> b ? c : d} 或者 ${a ? b: c}
    while (str.matches(ViewParserSimple.chooseValueRegex.toString())) {
      val matcher = ViewParserSimple.chooseValueRegex.pattern.matcher(str)
      if (matcher.find()) {
        //1,3,9,11,17,23
        //${exp1 option exp2 ? chos1 : chos2}
//        for (i <- 0 to matcher.groupCount()) {
//          println(matcher.group(i))
//        }

        val matcherStr = matcher.group(1)
        val exp1 = if (matcher.group(3) != null) matcher.group(3).trim else null
        val option = if (matcher.group(9) != null) matcher.group(9).trim else null
        val exp2 = if (matcher.group(11) != null) matcher.group(11).trim else null
        val chos1 = if (matcher.group(17) != null) matcher.group(17).trim else null
        val chos2 = if (matcher.group(23) != null) matcher.group(23).trim else null

        var chosIndex= 0
        if (exp1 != null && chos1 != null && chos2 != null) {
          if ((option == null || option == "") && exp2 == null) {
            val exp1Value = getExpressValue(exp1, model, parentItem)
            if (exp1Value != null && (exp1Value.getClass == classOf[Boolean] || exp1Value.getClass == classOf[java.lang.Boolean])) {
              if (exp1Value.asInstanceOf[Boolean]) chosIndex = 1 else chosIndex = 2
            }
            else {
              throw new Exception(s"${matcherStr}格式有误：${exp1}的值($exp1Value)不是boolean类型")
            }
          }
          else if (option != null && option != "" && exp2 != null) {
            val exp1Value = getExpressValue(exp1, model, parentItem)
            val exp2Value = getExpressValue(exp2, model, parentItem)
            if (exp1Value == null || exp2Value == null) {
              if (option == "==") {
                if (exp1Value == exp2Value) chosIndex = 1 else chosIndex = 2
              }
              else if (option == "!=") {
                if (exp1Value == exp2Value) chosIndex = 2 else chosIndex = 1
              }
              else chosIndex = 2
            }
            else {
              val exp1ValueType = exp1Value.getClass
              val exp2ValueType = exp2Value.getClass
              if (exp1ValueType == exp2ValueType) {
                if (compareOption(exp1Value, exp2Value, option)) chosIndex = 1 else chosIndex = 2
              }
              else {
                throw new Exception(s"${matcherStr}格式有误：${exp1}的值($exp1Value)和${exp2}的值($exp2Value)类型不一致，无法比较")
              }
            }
          }
        }

        if (chosIndex == 0) {
          throw new Exception(s"${matcherStr}格式有误")
        }
        else {
          val wholeExpressValue = if (chosIndex == 1) getExpressValue(chos1, model, parentItem)
          else getExpressValue(chos2, model, parentItem)
          str = str.replace(matcherStr, wholeExpressValue.toString)
        }

      }
    }

    //解析普通的取值表达式
    while (str.matches(ViewParserSimple.modelValueRegex.toString())) {
      val matcher = ViewParserSimple.modelValueRegex.pattern.matcher(str)
      if (matcher.find()) {
        val matcherStr = matcher.group(1)
        val dataNodesStr = matcher.group(2)
        val dataNodes = ViewParserSimple.nodesStrToNodesArr(dataNodesStr)
        val tempData = ViewParserSimple.getDataByNodes(model, parentItem, dataNodes)
        str = str.replace(matcherStr, if (tempData != null) tempData.toString else "null")
      }
    }

    //解析view;for表达式
    while (str.matches(ViewParserSimple.viewForTagRegex.toString())) {
      val matcher = ViewParserSimple.viewForTagRegex.pattern.matcher(str)
      if (matcher.find()) {
        val viewForTagStr = matcher.group(1)
        val viewForTag = matchMap(viewForTagStr)
        str = str.replace(viewForTagStr, viewForTag.parse(matchMap, model, parentItem))
      }
    }

    str
  }

  private def getExpressValue(exp: String, model: ViewModel, parentItem: ViewModel): Any = {
    if (exp.matches(dataNumberRegex.toString())) {
      exp.toDouble
    }
    else if (exp.matches(dataBooleanRegex.toString())) {
      exp.toBoolean
    }
    else if (exp.matches(dataStrRegex.toString())) {
      exp.substring(1, exp.length - 1)
    }
    else if (exp.matches(dataNodesRegex.toString())) {
      val nodesArr = nodesStrToNodesArr(exp)
      val tempValue = getDataByNodes(model, parentItem, nodesArr)
      if (isInt(tempValue) || isLong(tempValue) || isFloat(tempValue) || isShort(tempValue)) tempValue.toString.toDouble
      else tempValue
    }
    else null
  }

  private def compareOption(value1: Any, value2: Any, option: String): Boolean = {
    if (isDouble(value1) && isDouble(value2)) {
      option match {
        case "<" =>
          value1.asInstanceOf[Double] < value2.asInstanceOf[Double]
        case "<=" =>
          value1.asInstanceOf[Double] <= value2.asInstanceOf[Double]
        case "==" =>
          value1.asInstanceOf[Double] == value2.asInstanceOf[Double]
        case "!=" =>
          value1.asInstanceOf[Double] != value2.asInstanceOf[Double]
        case ">=" =>
          value1.asInstanceOf[Double] >= value2.asInstanceOf[Double]
        case ">" =>
          value1.asInstanceOf[Double] > value2.asInstanceOf[Double]
      }
    }
    else if (isBoolean(value1) && isBoolean(value2)) {
      option match {
        case "<" =>
          value1.asInstanceOf[Boolean] < value2.asInstanceOf[Boolean]
        case "<=" =>
          value1.asInstanceOf[Boolean] <= value2.asInstanceOf[Boolean]
        case "==" =>
          value1.asInstanceOf[Boolean] == value2.asInstanceOf[Boolean]
        case "!=" =>
          value1.asInstanceOf[Boolean] != value2.asInstanceOf[Boolean]
        case ">=" =>
          value1.asInstanceOf[Boolean] >= value2.asInstanceOf[Boolean]
        case ">" =>
          value1.asInstanceOf[Boolean] > value2.asInstanceOf[Boolean]
      }
    }
    else if (isString(value1) && isString(value2)) {
      option match {
        case "<" =>
          value1.asInstanceOf[String] < value2.asInstanceOf[String]
        case "<=" =>
          value1.asInstanceOf[String] <= value2.asInstanceOf[String]
        case "==" =>
          value1.asInstanceOf[String] == value2.asInstanceOf[String]
        case "!=" =>
          value1.asInstanceOf[String] != value2.asInstanceOf[String]
        case ">=" =>
          value1.asInstanceOf[String] >= value2.asInstanceOf[String]
        case ">" =>
          value1.asInstanceOf[String] > value2.asInstanceOf[String]
      }
    }
    else {
      if (option == "==") {
        value1 == value2
      }
      else if (option == "!=") {
        value1 != value2
      }
      else false
    }
  }



}

class ViewParserSimple extends ViewParser {

  override def isParseNecessary(html: String): Boolean = {
    html.matches(".*\\$\\{.*\\}.*")
  }

  /**
    * 目前只解析三种表达式
    * ${***.***(0)}
    * ${***?***:****}
    * <view:for list="${***}" item="***" index="">***</view:for>
    *
    * @param html
    * @param model
    * @return
    */
  override def parse(html: String, model: ViewModel): String = {
    var str = html

    var matchIndex = 0
    val matchMap = new mutable.HashMap[String, ViewForTag]()
    while (str.matches(ViewParserSimple.forRegex.toString())) {
      val matcher = ViewParserSimple.forRegex.pattern.matcher(str)
      if (matcher.find()) {
        val viewForTag = new ViewForTag(matchIndex, matcher)
        matchMap += ((viewForTag.tag, viewForTag))

        str = str.replace(viewForTag.str, viewForTag.tag)
        matchIndex += 1
      }
    }

    ViewParserSimple.parseContent(str, matchMap, model, new ViewModel)
  }

}
