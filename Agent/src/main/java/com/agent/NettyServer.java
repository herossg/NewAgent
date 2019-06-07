package com.agent;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
@PropertySource("classpath:server.properties")
public class NettyServer {

    @Value("${tcp.port}")
    private int tcpPort;
    
    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    private static final ServiceHandler SERVICE_HANDLER = new ServiceHandler();
    
    public void start() {
    	EventLoopGroup bossGroup = new NioEventLoopGroup(bossCount);
    	EventLoopGroup workerGroup = new NioEventLoopGroup(workerCount);

    	try {
    		ServerBootstrap b = new ServerBootstrap();
    		b.group(bossGroup, workerGroup)
    		.channel(NioServerSocketChannel.class)
    		.handler(new LoggingHandler(LogLevel.INFO))
        	.childHandler(new ChannelInitializer<SocketChannel>() {
        		@Override
        		protected void initChannel(SocketChannel ch) throws Exception {
        			ChannelPipeline pipeline = ch.pipeline();
        			pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        			pipeline.addLast(new ObjectEncoder());
        			pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        			pipeline.addLast(SERVICE_HANDLER);
        		}
        	});

    		ChannelFuture channelFuture = b.bind(tcpPort).sync();
    		channelFuture.channel().closeFuture().sync();
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	} finally {
    		bossGroup.shutdownGracefully();
    		workerGroup.shutdownGracefully();
    	}
    }
}