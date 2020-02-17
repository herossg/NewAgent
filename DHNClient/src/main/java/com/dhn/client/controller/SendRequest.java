package com.dhn.client.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.dhn.client.model.DhnRequest;
import com.dhn.client.model.DhnResult;
import com.dhn.client.service.DhnRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SendRequest {
	
	
	private static DhnRequestService dhnReqService ;
	
	public static boolean isRunning = false;
	private static final Logger log = LoggerFactory.getLogger(SendRequest.class);
	private static Environment env;
	
	@Autowired
	public SendRequest(DhnRequestService dhnReqService, Environment env) {
		SendRequest.dhnReqService = dhnReqService;
		SendRequest.env = env;
	}
	
	public static void run() {
		if(!isRunning) {
			try {
				isRunning = true;
				List<DhnRequest> dhnReqs = dhnReqService.findAll();
				
				if(dhnReqs != null && dhnReqs.size() > 0) {
					final String URL =  env.getProperty("server") + "req";
					HttpHeaders headers = new HttpHeaders();

					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.set("userid", env.getProperty("userid"));
					//log.info("Request Send Start !! ");
					
					AsyncRestTemplate restTemp = new AsyncRestTemplate();
					ObjectMapper mapper = new ObjectMapper();
					
					for(int i=0; i<dhnReqs.size(); i++) {
						DhnRequest dr = (DhnRequest)dhnReqs.get(i);
						
						String jsonStr = mapper.writeValueAsString(dr);
//						
						
						HttpEntity<String> entity = new HttpEntity<String>(jsonStr,headers);						
						ListenableFuture<ResponseEntity<String>> response = restTemp.postForEntity(URL, entity, String.class); 
						response.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

							@Override
							public void onSuccess(ResponseEntity<String> result) {
								log.info("Send Req Response : " + result);
							}

							@Override
							public void onFailure(Throwable ex) {
								// TODO Auto-generated method stub
								log.info("Send Req Response Failure : " + ex.toString() );
							}
						});
						
						dhnReqService.deleteByMsgidQeury(dr.getMSGID());
						
						DhnResult dhnResult = new DhnResult();
						
						dhnResult.setMsgid(dr.getMSGID()) ;
						dhnResult.setAd_flag(dr.getAD_FLAG()) ;
						dhnResult.setButton1(dr.getBUTTON1()) ;
						dhnResult.setButton2(dr.getBUTTON2()) ;
						dhnResult.setButton3(dr.getBUTTON3()) ;
						dhnResult.setButton4(dr.getBUTTON4()) ;
						dhnResult.setButton5(dr.getBUTTON5()) ;
						dhnResult.setCode("");
						dhnResult.setImage_link(dr.getIMAGE_LINK()) ;
						dhnResult.setImage_url(dr.getIMAGE_URL()) ;
						dhnResult.setKind("");
						dhnResult.setMessage("") ;
						dhnResult.setMessage_type(dr.getMESSAGE_TYPE() ) ;
						dhnResult.setMsg(dr.getMSG() ) ;
						dhnResult.setMsg_sms(dr.getMSG_SMS() ) ;
						dhnResult.setOnly_sms(dr.getONLY_SMS() ) ;
						dhnResult.setP_com(dr.getP_COM() ) ;
						dhnResult.setP_invoice(dr.getP_INVOICE() ) ;
						dhnResult.setPhn(dr.getPHN() ) ;
						dhnResult.setProfile(dr.getPROFILE() ) ;
						dhnResult.setReg_dt(dr.getREG_DT() ) ;
						dhnResult.setRemark1(dr.getREMARK1() ) ;
						dhnResult.setRemark2(dr.getREMARK2() ) ;
						dhnResult.setRemark3(dr.getREMARK3() ) ;
						dhnResult.setRemark4(dr.getREMARK4() ) ;
						dhnResult.setRemark5(dr.getREMARK5() ) ;
						dhnResult.setRes_dt(dr.getRESERVE_DT()) ;
						dhnResult.setReserve_dt(dr.getRESERVE_DT() ) ;
						dhnResult.setResult("N") ;
						dhnResult.setS_code(dr.getS_CODE() ) ;
						dhnResult.setSms_kind(dr.getSMS_KIND() ) ;
						dhnResult.setSms_lms_tit(dr.getSMS_LMS_TIT() ) ;
						dhnResult.setSms_sender(dr.getSMS_SENDER() ) ;
						dhnResult.setSync("N" ) ;
						dhnResult.setTmpl_id(dr.getTMPL_ID() ) ;
						dhnResult.setWide(dr.getWIDE() ) ;
						
						SaveResult.Save(dhnResult);
					}
					//log.info("Request Send : " + dhnReqs.size());
				}
			} catch (Exception e) {
				log.info(e.toString());
			} finally {
				isRunning = false;	
			}
		}
	}
}
