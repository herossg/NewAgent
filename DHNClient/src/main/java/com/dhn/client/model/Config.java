package com.dhn.client.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Config {
	public static String userid;
	public static String server;
	public static String req_table;
	public static String res_table;

	@Autowired
	public  Config(Environment env) {
		Config.userid = env.getProperty("userid");
		Config.server = env.getProperty("server");
		Config.req_table = env.getProperty("REQUEST_TABLE");
		Config.res_table = env.getProperty("DHN_REQUEST_RESULT");
	}
}
