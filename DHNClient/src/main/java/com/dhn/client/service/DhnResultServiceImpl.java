package com.dhn.client.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dhn.client.model.DhnResult;
import com.dhn.client.repository.DhnResultRepo;

@Component
public class DhnResultServiceImpl implements DhnResultService{
	
	@Autowired
	DhnResultRepo dhnResultRepo;
	
	@Override
	public DhnResult save(DhnResult dhnResult) {
		dhnResultRepo.save(dhnResult);
		return dhnResult;
	}

	@Override
	public List<DhnResult> findByResult(String res) {
		List<DhnResult> dhnResults = new ArrayList<DhnResult>();
		dhnResultRepo.findByResult(res).forEach(e -> dhnResults.add(e));
		return dhnResults;
	}
	
	@Override
	public void updateByMsgidQuery(String msgid, String code, String message) {
		dhnResultRepo.updateByMsgidQuery(msgid, code, message);
		
	}
	

}
