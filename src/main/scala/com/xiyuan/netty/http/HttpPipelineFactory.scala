package com.xiyuan.netty.http

import org.jboss.netty.channel.{DefaultChannelPipeline, ChannelPipeline, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.http.{HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}
import org.jboss.netty.handler.stream.ChunkedWriteHandler

/**
  * Created by xiyuan_fengyu on 2016/7/13.
  */
class HttpPipelineFactory extends ChannelPipelineFactory {

  override def getPipeline: ChannelPipeline = {
    val pipeline = new DefaultChannelPipeline()
    pipeline.addLast("decoder", new HttpRequestDecoder())
    pipeline.addLast("encoder", new HttpResponseEncoder())
    pipeline.addLast("chunkedWriter", new ChunkedWriteHandler())
    pipeline.addLast("deflater", new HttpContentCompressor())
    pipeline.addLast("handler", new HttpRequestHandler())
    pipeline
  }

}
