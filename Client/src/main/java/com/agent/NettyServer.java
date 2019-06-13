package com.agent;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

@Component
public class NettyServer {

	private String tcpHost;
    private String tcpPort;


    private static final ClientHandler SERVICE_HANDLER = new ClientHandler();
    
    public void start() {
    	EventLoopGroup bossGroup = new NioEventLoopGroup();
    	
    	try {
		String current = new java.io.File( "." ).getCanonicalPath();
		current = current + "/conf/server.properties";
		//log.info("PWD : " + current);

		Properties p = new Properties();
		p.load(new FileInputStream(current));
		
		tcpHost = p.getProperty("HOST");
		System.out.println("Host : " + tcpHost);
    	} catch(Exception ex) {
    		System.out.println(ex.toString());
    	}
    	
    	try {
    		Bootstrap b = new Bootstrap();
    		b.group(bossGroup)
    		.channel(NioSocketChannel.class)
    		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
    		.handler(new LoggingHandler(LogLevel.ERROR))
        	.handler(new ChannelInitializer<SocketChannel>() {
        		@Override
        		protected void initChannel(SocketChannel ch) throws Exception {
        			ChannelPipeline pipeline = ch.pipeline();
        			pipeline.addLast(new IdleStateHandler(20,10,0));
        			pipeline.addLast(new ObjectEncoder());
        			pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        			pipeline.addLast(SERVICE_HANDLER);
        		}
        	});

    		ChannelFuture channelFuture = b.connect(tcpHost, 8080).sync();
    		channelFuture.channel().closeFuture().sync();
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	} finally {
    		bossGroup.shutdownGracefully();
    	}
    }
}