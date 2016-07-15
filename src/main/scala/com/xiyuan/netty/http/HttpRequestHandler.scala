package com.xiyuan.netty.http

import com.xiyuan.netty.config.HttpServerConfig
import com.xiyuan.netty.dispatcher.DispatchCenter
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.util.CharsetUtil

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
class HttpRequestHandler extends SimpleChannelUpstreamHandler {

  private var curRequest: HttpRequest = null

  private var isReadingChunks: Boolean = false

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
      if (!isReadingChunks) {
        curRequest = e.getMessage.asInstanceOf[HttpRequest]
        println(curRequest.getMethod + "\t" + curRequest.getUri)

        if (HttpHeaders.is100ContinueExpected(curRequest)) {
          response100Continue(e)
        }

        if (curRequest.isChunked) {
          isReadingChunks = true
        }
        else handleRequest(curRequest, e)
      }
      else if (e.getMessage.asInstanceOf[HttpChunk].isLast) {
        handleRequest(curRequest, e)
        isReadingChunks = false
      }
  }

  private def handleRequest(request: HttpRequest, e: MessageEvent): Unit = {
    val keepAlive = HttpHeaders.isKeepAlive(request)

    try {
      request.getUri match {
        case url: String if url.matches("^.*\\.(html|css|js|jpg|png|bmp|jpeg|gif|ico)(\\?(.*\\=.*\\|)*(.*\\=.*){0,1}){0,1}$") =>
          responseStaticFile(request, e)
        case url: String if url.matches("^.*(\\?(.*\\=.*\\|)*(.*\\=.*){0,1}){0,1}$") =>
          DispatchCenter.dispatchRequest(request, e)
        case _ =>
          response404(request, e)
      }
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
    }

    if (!keepAlive) {
      e.getFuture.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def response100Continue(e: MessageEvent) {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)
    e.getChannel.write(response)
  }

  private def responseStaticFile(request: HttpRequest, e: MessageEvent): Unit = {
    val staticFile = HttpStaticFile.get(request.getUri)
    if (staticFile == null) {
      response404(request, e)
    }
    else {
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      val bytes = staticFile.getBytes(CharsetUtil.UTF_8)
      response.headers.set(Names.CONTENT_LENGTH, bytes.length)
      val channelBuffers = ChannelBuffers.wrappedBuffer(bytes)
      response.setContent(channelBuffers)
      e.getChannel.write(response)
    }
  }

  private def response404(request: HttpRequest, e: MessageEvent): Unit = {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
    val html404 = HttpStaticFile.get(HttpServerConfig.htmlFor404)
    val bytes = html404.getBytes(CharsetUtil.UTF_8)
    response.headers.set(Names.CONTENT_LENGTH, bytes.length)
    val channelBuffers = ChannelBuffers.wrappedBuffer(bytes)
    response.setContent(channelBuffers)
    e.getChannel.write(response)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    e.getCause.printStackTrace()
    e.getChannel.close()
  }

}
