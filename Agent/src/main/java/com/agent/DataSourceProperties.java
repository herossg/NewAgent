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
			

			log.info("User ID         : " + p.getProperty("LOGIN_ID"));
			log.info("USer Password   : " + p.getProperty("LOGIN_PW"));
			
			
			DbInfo.dbSource = hds;
			return hds;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
			
		}

	    System.exit(0);
		
		return null;
	}
	
	private String check_login(String id, String pw) {
		 
		Map params = new HashMap<>();
		params.put("id", id);
		params.put("pw", pw);
		
		String res = post("http://onlineat.kr/api/login", params);
		
		return res;
	}
	
	public String post(String url, Map params, String encoding){
        HttpClient client = new DefaultHttpClient();
         
        try{
            HttpPost post = new HttpPost(url);
            //System.out.println("POST : " + post.getURI());
             
            List<NameValuePair> paramList = convertParam(params);
            post.setEntity(new UrlEncodedFormEntity(paramList, encoding));
             
            ResponseHandler<String> rh = new BasicResponseHandler();
 
            return client.execute(post, rh);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            client.getConnectionManager().shutdown();
        }
         
        return "error";
    }
     
    public String post(String url, Map params){
        return post(url, params, "UTF-8");
    }

    private List<NameValuePair> convertParam(Map params){
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        Iterator<String> keys = params.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            paramList.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
         
        return paramList;
    }  	
	
}