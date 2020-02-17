package com.dhn.client.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.dhn.client.model.DhnRequest;
import com.dhn.client.repository.DhnRequestRepo;

@Component
public class DhnRequestServiceImpl implements DhnRequestService {

	@Autowired
	private DhnRequestRepo dhnRequestRepo;
	
	@Override
	public List<DhnRequest> findAll() {
		List<DhnRequest> dhnRequests = new ArrayList<>();
		Pageable limit = PageRequest.of(0,  1000);
		dhnRequestRepo.findAll(limit).forEach(e -> dhnRequests.add(e));
		return dhnRequests;
	}

	@Override
	public DhnRequest findByMsgid(String msgid) {
		DhnRequest dhnRequest = null;
		try {
			dhnRequest = dhnRequestRepo.findById(msgid).orElseThrow(()->new Exception("존재하지 않는 Msg ID 입니다."));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dhnRequest;
	}

	@Override
	public void deleteByMsgidQeury(String msgid) {
		dhnRequestRepo.deleteByMsgidQuery(msgid);
	}

}
