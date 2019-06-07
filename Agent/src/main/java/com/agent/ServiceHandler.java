package com.agent;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

@Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  
	private static final AtomicInteger count = new AtomicInteger(0);
  
	final AttributeKey<Integer> id = AttributeKey.newInstance("id");

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		int value = count.incrementAndGet();
		ctx.channel().attr(id).set(value);
		channels.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		logger.debug("message : {} ",byteBuf.toString(Charset.defaultCharset()));
		channels.writeAndFlush(msg);
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}