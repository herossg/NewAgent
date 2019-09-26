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
		if(!ServiceHandler.SHisRunning)
			ServiceHandler.resultProc();
		//temp.resultProc();
	}
}
