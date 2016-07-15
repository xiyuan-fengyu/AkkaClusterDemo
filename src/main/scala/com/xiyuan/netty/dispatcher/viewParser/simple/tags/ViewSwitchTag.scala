package com.xiyuan.netty.dispatcher.viewParser.simple.tags

import java.util.Date
import java.util.regex.Matcher

import com.xiyuan.netty.dispatcher.viewParser.simple.ViewParserSimple

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xiyuan_fengyu on 2016/7/15.
  */
class ViewSwitchTag(matcher: Matcher) {

  val switchStr = matcher.group(1)

  val casesStr = matcher.group(2)

  private def parseCases(): Array[ViewCaseTag] = {
    var tempCasesStr = casesStr

    val caseAB = new ArrayBuffer[ViewCaseTag]()

    var caseIndex = 0
    while (tempCasesStr.matches(ViewParserSimple.caseRegex.regex)) {
      val caseMatcher = ViewParserSimple.caseRegex.pattern.matcher(tempCasesStr)
      if (caseMatcher.find()) {
        val caseTag = new ViewCaseTag(caseMatcher, caseIndex)
        tempCasesStr = tempCasesStr.replace(caseTag.caseStr, caseTag.tag)
        caseAB.insert(0, caseTag)
        caseIndex += 1
      }
    }
    caseAB.toArray
  }

  val caseArr: Array[ViewCaseTag] = parseCases()
}

class ViewCaseTag(matcher: Matcher, index: Int) {

  val tag = s"VIEW_CASE_TAG_${index}_${new Date().getTime}"

  val caseStr = matcher.group(1)

  val condition = matcher.group(2)

  val exp1 = if (matcher.group(4) != null) matcher.group(4).trim else null

  val option = if (matcher.group(10) != null) matcher.group(10).trim else null

  val exp2 = if (matcher.group(12) != null) matcher.group(12).trim else null

  val content = if (matcher.group(17) != null) matcher.group(17).trim else ""

}