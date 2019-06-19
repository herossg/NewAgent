package com.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

import com.mysql.cj.xdevapi.PreparableStatement;

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
    	
    	mCtx = ctx;
    	
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
	    			String selQuery = SQL.SelectMaster;
	    			
	    			Statement stm = con.createStatement();
	    			ResultSet rs = stm.executeQuery(selQuery);
	    			while(rs.next()) {
	    				
	    				PreparedStatement detailsql = con.prepareStatement(SQL.SelectDetail);
	    				detailsql.setString(1, rs.getString("MSGID"));
	    				ResultSet detailrs = detailsql.executeQuery();
	    				
	    				StringBuffer sb = new StringBuffer();
	    				while(detailrs.next()) {
	    					sb.append(detailrs.getString("phn"));
	    				}
	    				
	    				final ClientMessage clientMsg = new ClientMessage();
	    				
	    				clientMsg.setUserid(DbInfo.LOGIN_ID);
	    				clientMsg.setMsgid(rs.getInt("MSGID"));
	    				clientMsg.setBtn1(rs.getString("BUTTON1"));
	    				clientMsg.setBtn2(rs.getString("BUTTON2"));
	    				clientMsg.setBtn3(rs.getString("BUTTON3"));
	    				clientMsg.setBtn4(rs.getString("BUTTON4"));
	    				clientMsg.setBtn5(rs.getString("BUTTON5"));
	    				clientMsg.set1stMessage_type(rs.getString("MESSAGE_TYPE"));
	    				clientMsg.set2ndMessage_type(rs.getString("2ND_MSG_TYPE"));
	    				clientMsg.setSender(rs.getString("SENDER"));
	    				clientMsg.setReserve_date(rs.getString("RESERVE_DT"));
	    				clientMsg.setMessage(rs.getString("MSG"));
	    				clientMsg.setProfile(rs.getString("PROFILE"));
	    				clientMsg.setTmpl_id(rs.getString("TMPL_ID"));
	    				clientMsg.setReg_dt(rs.getString("REG_DT"));
	    				clientMsg.setAdd1(rs.getString("ADD1"));
	    				clientMsg.setAdd2(rs.getString("ADD2"));
	    				clientMsg.setAdd3(rs.getString("ADD3"));
	    				clientMsg.setAdd4(rs.getString("ADD4"));
	    				clientMsg.setAdd5(rs.getString("ADD5"));
	    				clientMsg.setMMS1(rs.getString("MMS1"));
	    				clientMsg.setMMS2(rs.getString("MMS2"));
	    				clientMsg.setMMS3(rs.getString("MMS3"));
	    				clientMsg.setMMS4(rs.getString("MMS4"));
	    				clientMsg.setPhnList(sb);
	    		    	mCtx.writeAndFlush(clientMsg);
	    		    	
	    		    	PreparedStatement udsql = con.prepareStatement("update DHN_REQUEST set proc_flag = 'Y' where MSGID = ?");
	    		    	udsql.setInt(1, rs.getInt("MSGID"));
	    		    	udsql.executeUpdate();
	    		    	udsql.close();
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