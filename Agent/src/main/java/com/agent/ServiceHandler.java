package com.agent;

import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

@Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  
	private static final AtomicInteger count = new AtomicInteger(0);
  
	final AttributeKey<Integer> id = AttributeKey.newInstance("id");
	final static AttributeKey<String> userid = AttributeKey.newInstance("userid");

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		int value = count.incrementAndGet();
		ctx.channel().attr(id).set(value);
		ctx.channel().attr(userid).set("");
		channels.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ClientMessage t = (ClientMessage)msg; 
    	
    	if(ctx.channel().attr(userid).get().isEmpty())
    	{
    		ctx.channel().attr(userid).set(t.getUserid());
    	}
    	
    	logger.info("user id : " + t.getUserid() + " / MSG : " + t.getMessage());
    	//t.SaveIMG1("");
	}
	
	static public void resultProc() {
		for(Channel c: channels) {
			ClientMessage msg = new ClientMessage();
			long time = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
			String str = dayTime.format(new Date(time));
 
	    	msg.setMessage(str +  ": Server 메세지 :" + c.attr(userid).get());
	    	 
	    	//System.out.println(msg.getmResultMsg());
			c.writeAndFlush(msg);
		}
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	//ServerMessage msg = new ServerMessage();
    	//msg.setmResultMsg("읽기 완료.");
    	//ctx.writeAndFlush(msg);
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}