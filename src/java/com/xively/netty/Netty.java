/* We use this to overcome a bug in clojure with private abstract class
 * ancestors.
 */
package com.xively.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.Bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ServerChannel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelHandlerContext;

public class Netty
{
  public static ChannelFuture close(Channel channel)
  {
    return channel.close();
  }

  public static Channel flush(Channel channel)
  {
    return channel.flush();
  }

  public static ChannelFuture write(Channel channel, Object message)
  {
    return channel.write(message);
  }

  public static ChannelFuture writeAndFlush(Channel channel, Object message)
  {
    return channel.writeAndFlush(message);
  }

  public static ChannelFuture writeAndFlush(ChannelHandlerContext context, Object message)
  {
    return context.writeAndFlush(message);
  }

  public static ChannelFuture close(ChannelHandlerContext context)
  {
    return context.close();
  }

  public static ChannelFuture disconnect(ChannelHandlerContext context)
  {
    return context.disconnect();
  }

  public static ChannelPipeline pipeline(Channel channel)
  {
    return channel.pipeline();
  }

  public static ServerBootstrap channel(ServerBootstrap bootstrap,
                                         Class<? extends ServerChannel> channelClass)
  {
    return bootstrap.channel(channelClass);
  }

  public static Bootstrap clientChannel(Bootstrap bootstrap,
                                         Class<? extends Channel> channelClass)
  {
    return bootstrap.channel(channelClass);
  }

  public static Bootstrap group(Bootstrap bootstrap, EventLoopGroup group)
  {
    return bootstrap.group(group);
  }

  public static Bootstrap handler(Bootstrap bootstrap,
                                   ChannelHandler handler)
  {
    return bootstrap.handler(handler);
  }

  public static Bootstrap option(Bootstrap bootstrap,
                                  ChannelOption<java.lang.Object> option,
                                  Object value)
  {
    return bootstrap.option(option, value);
  }

  public static ChannelPipeline pipeline(ChannelHandlerContext ctx)
  {
    return ctx.pipeline();
  }
}
