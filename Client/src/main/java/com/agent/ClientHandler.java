package com.agent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
@Repository
public class ClientHandler extends ChannelInboundHandlerAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static boolean isRunning = false;
	private boolean isShort = true;
	private byte[] recData;
	private static ChannelHandlerContext mCtx = null;

    static DataSource userSource;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	
    	//StatusSending(ctx);
    	mCtx = ctx;
    	//objSending();
    	
    }
 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)   {
    	try {
    		ClientMessage t = (ClientMessage) msg;
			long time = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
			String str = dayTime.format(new Date(time));
	    	//logger.info(str + " - MSG : " + t.getmMessage());
    	} catch(Exception ex ) {
    		logger.error(ex.toString());
    	}
    	//t.SaveIMG1("");
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	
        ctx.flush();
    }    
    
    public static void objSending() throws Exception {
    	if(mCtx != null) {
    		if(!ClientHandler.isRunning) {
    			ClientHandler.isRunning = true;
	    		
	    		String table = "cb_dhn_msg1";
	    		
	    		Connection con = null;
	    		
	    		try {
	    			
	    			con = DbInfo.dbSource.getConnection();
	    			String selQuery = "select * from " + table;
	    			
	    			Statement stm = con.createStatement();
	    			ResultSet rs = stm.executeQuery(selQuery);
	    			while(rs.next()) {
	    				final ClientMessage clientMsg = new ClientMessage();
	    				
	    				clientMsg.setUserid("DHN");
	    				clientMsg.setMessage(rs.getString("TEXT"));  
	    				clientMsg.setMsgid(rs.getInt("MSG_ID"));  
	    				clientMsg.set1stMessage_type(rs.getString("MSG_GB"));  
	    				clientMsg.setMsg_title(rs.getString("SUBJECT"));
	    				clientMsg.setSender(rs.getString("MSG_SND_PHN"));
	    				 
	    		    	mCtx.writeAndFlush(clientMsg);
	    			}
	    			
	    		} catch(Exception ex )  {
	    			ex.printStackTrace();
	    		}
	    		
	    		if(con != null) {
	    			con.close();
	    		}
	    		

    		}
    		ClientHandler.isRunning = false;
    	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }    

}