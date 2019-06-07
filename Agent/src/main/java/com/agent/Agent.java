package com.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
@SpringBootApplication
public class Agent {
	
    @Autowired
    private ApplicationContext context;
    
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Agent.class, args);

		NettyServer nettyServer = context.getBean(NettyServer.class);
		nettyServer.start();
	}
}
