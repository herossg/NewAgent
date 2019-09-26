package com.agent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.json.JsonParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties
public class DataSourceProperties {

    static boolean isStart = false;
    Logger log = LoggerFactory.getLogger(getClass());
	//UserInfo userInfo = new UserInfo();
	
	@Bean(name="userds")
	@Qualifier("userds")
	public DataSource userDataSource() {
		
		HikariDataSource hds = new HikariDataSource();
		String current;

		try {
			current = new java.io.File( "." ).getCanonicalPath();
			current = current + "/conf/db.properties";
			//log.info("PWD : " + current);

			Properties p = new Properties();
			p.load(new FileInputStream(current));
			
			log.info("URL  : " + p.getProperty("URL"));
			log.info("User : " + p.getProperty("USERNAME"));
			log.info("PW   : " + p.getProperty("PASSWORD"));
			
			hds.setJdbcUrl(p.getProperty("URL"));
			hds.setUsername(p.getProperty("USERNAME"));
			hds.setPassword(p.getProperty("PASSWORD"));
			hds.setMaximumPoolSize(10);
			hds.setAutoCommit(true);

			DbInfo.DBMS = p.getProperty("DBMS");
			DbInfo.SID = p.getProperty("SID");
			DbInfo.MSG_TABLE = p.getProperty("MSG_TABLE");
			DbInfo.BROADCAST_TABLE = p.getProperty("BROADCAST_TABLE");
			
			DataSourceProperties.isStart = true;
			
			DbInfo.dbSource = hds;
			return hds;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		}

	    System.exit(0);
		
		return null;
	}
	
}