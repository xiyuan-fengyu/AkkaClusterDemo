package com.xiyuan.netty.dispatcher

import java.lang.reflect.Method

import com.google.gson.JsonObject
import com.xiyuan.netty.annotation.{View, RequestMapping, Controller}
import com.xiyuan.netty.config.HttpServerConfig
import com.xiyuan.netty.dispatcher.viewParser.ViewParser
import com.xiyuan.netty.http.HttpStaticFile
import com.xiyuan.netty.util.{JavaMethodUtil, ClassUtil}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
object DispatchCenter {

  private val pathMapping = new mutable.HashMap[String, (AnyRef, Method, Array[(String, Class[_])])]()

  def init(): Unit = {
    //扫描所有带有Controller注解的类
    val classes = ClassUtil.getClasses(HttpServerConfig.packageToScan)
    classes.foreach(clazz => {
      if (isController(clazz)) {
        val instance = clazz.newInstance().asInstanceOf[AnyRef]
        searchAllRequestMethod(clazz, instance)
      }
    })
  }

  private def isController(clazz: Class[_]): Boolean = {
    clazz.getAnnotations.exists(_.annotationType() == classOf[Controller])
  }

  private def searchAllRequestMethod(clazz: Class[_], instance: AnyRef): Unit = {
    val methods = clazz.getDeclaredMethods
    methods.foreach(m => {
      val requestMapping = getRequestMappingAnno(m)
      if (requestMapping != null) {
        val returnType = m.getReturnType
        if (pathMapping.contains(requestMapping.value())) {
          val exist = pathMapping(requestMapping.value())
          throw new Exception(s"${clazz.getSimpleName}的${m.getName}和${exist._1.getClass.getSimpleName}的${exist._2.getName}的${classOf[RequestMapping].getSimpleName}注解的value值重复")
        }
        else if (returnType == classOf[String]
          || returnType == classOf[JsonObject]
          || returnType == classOf[Int]
          || returnType == classOf[Long]
          || returnType == classOf[Double]
          || returnType == classOf[Float]
          || returnType == classOf[Short]
          || returnType == classOf[Boolean]) {
          if (m.getDeclaredAnnotations.exists(_.annotationType() == classOf[View]) && returnType != classOf[String]) {
            throw new Exception(s"${classOf[RequestMapping].getSimpleName},${classOf[View].getSimpleName}注解的方法的返回值只支持String")
          }
          else {
            val methodParams = ClassUtil.getMethodParam(m)
            if (methodParams.count(_._2 == classOf[ViewModel]) > 1) {
              throw new Exception(s"${classOf[RequestMapping].getSimpleName},${classOf[View].getSimpleName}注解的方法的参数列表中类型为${classOf[ViewModel].getSimpleName}的参数个数不超过1")
            }
            else pathMapping += ((requestMapping.value(), (instance, m, methodParams)))
          }
        }
        else {
          throw new Exception(s"${classOf[RequestMapping].getSimpleName}注解的方法的返回值只支持String,Int,Long,Double,Float,Boolean,JsonObject(Gson)")
        }
      }
    })
  }

  private def getRequestMappingAnno(method: Method): RequestMapping = {
    val find = method.getDeclaredAnnotations.filter(_.annotationType() == classOf[RequestMapping])
    if (find.nonEmpty) find(0).asInstanceOf[RequestMapping] else null
  }

  private val viewParser: ViewParser = this.getClass.getClassLoader.loadClass(HttpServerConfig.htmlParser).newInstance().asInstanceOf[ViewParser]

  def dispatchRequest(request: HttpRequest, e: MessageEvent): Unit = {
    try {
      val uri = request.getUri
      val urlPath = uri.split("\\?")(0).substring(1)
      if (pathMapping.contains(urlPath)) {
        val controllerAndMethod = pathMapping(urlPath)
        val controller = controllerAndMethod._1
        val method = controllerAndMethod._2

        //匹配参数列表
        val methodParams = controllerAndMethod._3
        val paramMap = new QueryStringDecoder(uri).getParameters
        val paramAB = new ArrayBuffer[AnyRef]()
        methodParams.foreach(item => {
          //是否是ViewModel
          if (item._2 == classOf[ViewModel]) {
            paramAB += new ViewModel
          }
          else if (paramMap.containsKey(item._1)) {
            val values = paramMap.get(item._1).toArray().map(_.asInstanceOf[String])
            val castValues = paramTypeCast(values, item._2)
            if (castValues != null) {
              paramAB += castValues
            }
            else {
              responseString(e, "参数类型错误", HttpResponseStatus.BAD_REQUEST)
              return
            }
          }
          else {
            if (item._2 == classOf[Int]
              || item._2 == classOf[Long]
              || item._2 == classOf[Double]
              || item._2 == classOf[Float]
              || item._2 == classOf[Short]
              || item._2 == classOf[Boolean]) {
              responseString(e, "缺少必要参数", HttpResponseStatus.BAD_REQUEST)
              return
            }
            else if (item._2 == classOf[String] || item._2 == classOf[Array[_]]) {
              paramAB += null
            }
            else {
              //自定义的对象
              val tempInstance = item._2.newInstance()
              item._2.getDeclaredFields.foreach(f => {
                f.setAccessible(true)
                val fName = f.getName
                if (paramMap.containsKey(fName)) {
                  val fType = f.getType
                  val values = paramMap.get(fName).toArray.map(_.asInstanceOf[String])
                  val castValues = paramTypeCast(values, fType)
                  if (castValues != null) {
                    f.set(tempInstance, castValues)
                  }
                  else {
                    responseString(e, "参数类型错误", HttpResponseStatus.BAD_REQUEST)
                    return
                  }
                }
              })
              paramAB += tempInstance.asInstanceOf[AnyRef]
            }
          }
        })

        //调用函数,目前返回类型只支持String,int,long,double,float,boolean,JsonObject
        val methodReturnType = method.getReturnType
        if (methodReturnType == classOf[String]) {
          //判断是返回存字符串还是view
          val result = JavaMethodUtil.invoke(controller, method, paramAB.toArray).asInstanceOf[String]
          if (method.getAnnotations.exists(_.annotationType() == classOf[View])) {
            val viewContentRaw = HttpStaticFile.get(result).replaceAll("\n", " ")

            //标签，model解析
            val viewContentParsed = if (viewParser.isParseNecessary(viewContentRaw)) {
              val modelFind = paramAB.find(_.getClass == classOf[ViewModel])
              val model = if (modelFind.nonEmpty) modelFind.get.asInstanceOf[ViewModel] else new ViewModel
              try {
                viewParser.parse(viewContentRaw, model)
              }
              catch {
                case e: Exception =>
                  "解析错误<br>" + e.getMessage
              }
            }
            else viewContentRaw

//            println(viewContentParsed)
            responseString(e, viewContentParsed)
          }
          else {
            responseString(e, result)
          }
        }
        else if (methodReturnType == classOf[JsonObject]) {
          val result = JavaMethodUtil.invoke(controller, method, paramAB.toArray).asInstanceOf[JsonObject]
          responseString(e, result.toString)
        }
        else if (methodReturnType == classOf[Int]
          || methodReturnType == classOf[Long]
          || methodReturnType == classOf[Double]
          || methodReturnType == classOf[Float]
          || methodReturnType == classOf[Short]
          || methodReturnType == classOf[Boolean]) {
          val result = JavaMethodUtil.invoke(controller, method, paramAB.toArray).toString
          responseString(e, result)
        }
      }
      else {
        val html404 = HttpStaticFile.get(HttpServerConfig.htmlFor404)
        responseString(e, html404, HttpResponseStatus.NOT_FOUND)
      }
    }
    catch {
      case ee: Exception =>
        ee.printStackTrace()
        responseString(e, "服务器发生内部错误", HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }
  }

  private def paramTypeCast(values: Array[String], clazz: Class[_]): AnyRef = {
    if (clazz == classOf[String]) {
      values.last
    }
    else if (clazz == classOf[Array[String]]) {
      values
    }
    else if (clazz == classOf[Int]
      || clazz == classOf[Long]
      || clazz == classOf[Double]
      || clazz == classOf[Float]
      || clazz == classOf[Short]
      || clazz == classOf[Boolean]) {
      try {
        ClassUtil.valueTypeToJavaObj(values.last, clazz)
      }
      catch {
        case ee: Exception =>
          ee.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Int]]) {
      try {
        val tempCastValues = values.map(_.toInt)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Long]]) {
      try {
        val tempCastValues = values.map(_.toLong)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Double]]) {
      try {
        val tempCastValues = values.map(_.toDouble)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Float]]) {
      try {
        val tempCastValues = values.map(_.toFloat)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Short]]) {
      try {
        val tempCastValues = values.map(_.toShort)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else if (clazz == classOf[Array[Boolean]]) {
      try {
        val tempCastValues = values.map(_.toBoolean)
        tempCastValues
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }
    }
    else null
  }

  private def responseString(e: MessageEvent, str: String, status: HttpResponseStatus = HttpResponseStatus.OK) {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    val bytes = str.getBytes(CharsetUtil.UTF_8)
    response.headers.set(Names.CONTENT_LENGTH, bytes.length)
    val channelBuffers = ChannelBuffers.wrappedBuffer(bytes)
    response.setContent(channelBuffers)
    e.getChannel.write(response)
  }

}
