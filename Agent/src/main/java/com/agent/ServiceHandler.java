package com.agent;

import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import scala.xml.Elem;

@Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {

	Logger log = LoggerFactory.getLogger(getClass());

	public static boolean SHisRunning = false;
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
    	
    	if( t.getMessage() != null && !t.getMessage().isEmpty() ) {
    		log.info("메세지 왔다. : " + t.getMessage());
    	}
    	//log.info("user id : " + t.getUserid() + " / MSG : " + t.getMessage() + " / Phn : " + t.getPhnList().toString());
	}
	
	static public void resultProc() {
		SHisRunning = true;
		for(Channel c: channels) {
			
			ClientMessage msg = new ClientMessage();
			String _userid = c.attr(userid).get();
			
			if(!_userid.isEmpty()) {
				String table = "cb_wt_msg_sent";
	    		Connection con = null;
	    		int rowCount = 0;
	    		
	    		try {
		    		DocumentBuilderFactory _xmlFactory = DocumentBuilderFactory.newInstance();
		    		DocumentBuilder _xmlBuilder = _xmlFactory.newDocumentBuilder();
		    		Document _xmlDoc = _xmlBuilder.newDocument();
		    		Element _xmlElement = _xmlDoc.createElement("Result");
		    		_xmlDoc.appendChild(_xmlElement);
		    		
	    			con = DbInfo.dbSource.getConnection();
	    			String selQuery = "select * from " + table + " where mst_mem_id = '" + _userid + "' and mst_015 = '1' limit 0, 5";
	    			
	    			Statement stm = con.createStatement();
	    			ResultSet rs = stm.executeQuery(selQuery);
	    			ResultSetMetaData rsmd = rs.getMetaData();
	    			int colCount = rsmd.getColumnCount();
	    			
	    			try {
		    			while(rs.next())
		    			{
		    				Element _row = _xmlDoc.createElement("Row");
		    				_xmlElement.appendChild(_row);
		    				rowCount++;
		    				for(int i=1; i<= colCount; i++) {
		    					String colName = rsmd.getColumnName(i);
		    					Object value = rs.getObject(i);
		    					Element node = _xmlDoc.createElement(colName);
		    					if(value != null) {
		    						node.appendChild(_xmlDoc.createTextNode(value.toString()));
		    					} else {
		    						node.appendChild(_xmlDoc.createTextNode(""));
		    					}
		    					_row.appendChild(node);
		    				}
		    				
		    				Statement _upd = con.createStatement();
		    				_upd.executeUpdate("update cb_wt_msg_sent set mst_015 = '0' where mst_id = '" + rs.getInt("mst_id") + "'");
		    			}
	    			}catch(Exception ex) {
	    				System.out.println("Row : " + rowCount + " / " + ex.toString());
	    			}
	    			
	    			rs.close();
	    			
	    			DOMSource domSource = new DOMSource(_xmlDoc);
	    			TransformerFactory tf = TransformerFactory.newInstance();
	    			Transformer transformer = tf.newTransformer();
	    			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    			StringWriter sw = new StringWriter();
	    			StreamResult sr = new StreamResult(sw);
	    			transformer.transform(domSource, sr);
	    			
	    			msg.mServerXML = sw.toString();
	    			//System.out.println(msg.mServerXML);
	    		} catch(Exception ex) {
	    			System.out.println(ex.toString());
	    		}
	    		
	    		if(con != null) {
	    			try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		
				long time = System.currentTimeMillis(); 
				SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
				String str = dayTime.format(new Date(time));
	 
		    	msg.setMessage(str +  ": Server 메세지 :" + c.attr(userid).get());
		    	 
		    	//System.out.println("User ID : " + c.attr(userid).get());
		    	if(rowCount > 0)
		    		c.writeAndFlush(msg);
			}

		}
		SHisRunning = false;
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