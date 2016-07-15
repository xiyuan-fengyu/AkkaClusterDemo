package com.xiyuan

import java.net.InetAddress
import java.util.Date
import java.util.regex.Matcher

import com.xiyuan.netty.dispatcher.ViewModel
import com.xiyuan.netty.dispatcher.viewParser.simple.ViewParserSimple
import com.xiyuan.netty.http.HttpStaticFile
import com.xiyuan.netty.model.TestParam
import com.xiyuan.netty.util.{JavaMethodUtil, ClassUtil}

import scala.StringBuilder
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

/**
  * Created by xiyuan_fengyu on 2016/7/12 17:38.
  */
object Test {

  def testMethod(zInt: Array[Int], paramStr: String): String = {
    println(s"$zInt\t$paramStr")
    ""
  }

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
//  val caseRegex = new Regex(".*(< *view *: *case *condition *= *\"\\$\\{\\}\" *>(.*?)</ *view *: *case *>).*")
  //( *(( *[^{}()."?: ]+ *(\( *[0-9]+ *\)){0,1} *\.)*( *[^{}()."?: ]+ *(\( *[0-9]+ *\)){0,1})|".*?"|[0-9.]+|true|false) *)
  //(<|<=|==|!=|>=|>)


  def main(args: Array[String]) {
//    println("123.html?123=1&dsd=34".matches("^.*\\.(html|css|js|jpg|png|bmp|jpeg|gif|ico)(\\?(.*\\=.*\\|)*(.*\\=.*){0,1}){0,1}$"))
//    val methods = Test.getClass.getMethods
//    ClassUtil.getMethodParam(methods(1)).foreach(println)
//    val arr = Array[AnyRef](Array(1), "2")
//    JavaMethodUtil.invoke(this, methods(1), arr)
//    methods(1).getDeclaredAnnotations.foreach(println)


    var str = HttpStaticFile.get("web/page/index.html").replaceAll("\n", " ")
    println(str)

    val model = new ViewModel
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

    model += (("conditions", Array(false, false, false)))

    str = new ViewParserSimple().parse(str, model)
    println(str)


  }

}