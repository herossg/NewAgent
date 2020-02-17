package com.dhn.client.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dhn.client.model.DhnResult;

@Service
public interface DhnResultService {
	DhnResult save(DhnResult dhnResult);
	
	List<DhnResult> findByResult(String res);
	
	public void updateByMsgidQuery(String msgid, String code, String message);
}
