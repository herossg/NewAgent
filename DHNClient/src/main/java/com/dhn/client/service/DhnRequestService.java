package com.dhn.client.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dhn.client.model.DhnRequest;

@Service
public interface DhnRequestService {
	List<DhnRequest> findAll();
	
	DhnRequest findByMsgid(String msgid);

	void deleteByMsgidQeury(String msgid);
	
	
}
