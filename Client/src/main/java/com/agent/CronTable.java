package com.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CronTable {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Scheduled(initialDelay=1000, fixedDelay=5000)
	public void serverJob() {
		try {
			ClientHandler.objSending();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//temp.resultProc();
	}
	
	@Scheduled(initialDelay=10000, fixedDelay=5000)
	public void Connectserver() {
		//System.out.println("호출 됨 :  " + NettyServer.isConnect);
		log.info("연결 시도" + NettyServer.isConnect);
		if(!NettyServer.isConnect) {
			log.info("서버 연결 시도");
			NettyServer nettyServer = Agent.context.getBean(NettyServer.class);
			nettyServer.start();
			log.info("서버 연결 시도");
		}
	}
}
