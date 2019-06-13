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
public class ClientHandler extends ChannelInboundHandlerAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean isTransfer = false;
	private boolean isShort = true;
	private byte[] recData;
	private static ChannelHandlerContext mCtx = null;
	//Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	
    	//StatusSending(ctx);
    	mCtx = ctx;
    	objSending();
    	
    }
 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)   {
    	try {
			ClientMessage t = (ClientMessage)msg;
			long time = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
			String str = dayTime.format(new Date(time));
	    	System.out.println(str + " - MSG : " + t.getmMessage());
    	} catch(Exception ex ) {
    		System.out.println(ex.toString());
    	}
    	//t.SaveIMG1("");
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	
        ctx.flush();
    }    
    
    public static void objSending() throws Exception {
    	if(mCtx != null) {
	    	final ClientMessage user = new ClientMessage();
	    	user.setmMessage("메세지다.");
	    	user.setmUserid("dhn 3");
	    	mCtx.writeAndFlush(user);
    	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }    

}