package com.dhn.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dhn.client.model.DhnResult;
import com.dhn.client.service.DhnResultService;

@Component
public class SaveResult {

	private static DhnResultService dhnResService;
	
	@Autowired
	public SaveResult(DhnResultService dhnResService) {
		SaveResult.dhnResService = dhnResService;
	}
	
	public static void Save(DhnResult dhnResult) {
		dhnResService.save(dhnResult);
	}
}
