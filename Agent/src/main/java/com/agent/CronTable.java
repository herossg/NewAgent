package com.agent;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CronTable {
	@Scheduled(initialDelay=1000, fixedDelay=5000)
	public void serverJob() {
		ServiceHandler.resultProc();
		//temp.resultProc();
	}
}
