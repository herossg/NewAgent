package com.dhn.client.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.dhn.client.model.DhnResult;
import com.dhn.client.service.DhnResultService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GetResult {
	private static DhnResultService dhnResService;
	
	public static boolean isRunning = false;
	private static final Logger log = LoggerFactory.getLogger(GetResult.class);
	private static Environment env;
	
	@Autowired
	public GetResult(DhnResultService dhnresService, Environment env) {
		GetResult.dhnResService = dhnresService;
		GetResult.env = env;
	}
	
	public static void run() {
		
		if(!isRunning) {
			try {
				isRunning = true;
				List<DhnResult> dhnResults = dhnResService.findByResult("N");
				
				if(dhnResults != null && dhnResults.size() > 0 ) {
					final String URL = env.getProperty("server") + "res/";
					HttpHeaders headers = new HttpHeaders();
	
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.set("userid", env.getProperty("userid"));
					//log.info("Request Send Start !! ");
					
					ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Include.NON_NULL);

					AsyncRestTemplate restTemp = new AsyncRestTemplate();
					
					for(int i=0; i<dhnResults.size(); i++) {
						DhnResult dr = (DhnResult)dhnResults.get(i);
						
						ListenableFuture<ResponseEntity<String>> response = restTemp.getForEntity(URL + dr.getMsgid(), String.class); 
						response.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

							@Override
							public void onSuccess(ResponseEntity<String> result) {
								Map<String, String> res;
								try {
									res = mapper.readValue(result.getBody(),  new TypeReference<Map<String, String>>(){});
									if(res != null & !res.get("code").equals("9999")) {
										log.info("Get Result Response : " + result);
										dhnResService.updateByMsgidQuery(dr.getMsgid(), res.get("code"), res.get("message"));
									}
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(Throwable ex) {
								// TODO Auto-generated method stub
								log.info("Get Result Response Failure : " + ex.toString() );
							}
						});
						
					}
				}
			} catch(Exception ex) {
				log.info(ex.toString());
			} finally {
				isRunning = false;
			}
		}
	}
	
}
