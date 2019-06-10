package com.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 
@SpringBootApplication
@EnableScheduling
public class Agent {
	
    @Autowired
    private ApplicationContext context;
    
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Agent.class, args);

		NettyServer nettyServer = context.getBean(NettyServer.class);
		nettyServer.start();
	}
	
	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
		ts.setPoolSize(10);
		
		return ts;
	}
}
