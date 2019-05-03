import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.mysql.jdbc.Driver;

public class BizDBCPInit {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static String USER_NAME = "root";
	private static String PASSWORD = "sjk4556!!22";
	private static String DB_URL = "jdbc:mysql://210.114.225.53/dhn?characterEncoding=utf8";  
	//private static final String DB_URL = "jdbc:mysql://222.122.203.68/dhn?characterEncoding=utf8";
	private static Logger log;
	
	private BizDBCPInit() {
		initConnectionPool();
	}
	
	private static class Singleton {
		private static final BizDBCPInit instance = new BizDBCPInit();
	}
	
	public static BizDBCPInit getInstance(Logger _log) {
		BizDBCPInit.log = _log;
		return Singleton.instance;
	}
	
	public void initConnectionPool() {
		Properties p = new Properties();
		try {
			
			p.load(new FileInputStream("/root/BizAgent/conf/db.properties"));
        	//p.load(new FileInputStream("E:\\Git\\BizAgent\\conf\\db.properties")); 

			BizDBCPInit.DB_URL = p.getProperty("DB_URL");
			BizDBCPInit.USER_NAME = p.getProperty("USER_NAME");
			BizDBCPInit.PASSWORD = p.getProperty("PASSWORD");
			
			Class.forName(BizDBCPInit.JDBC_DRIVER);
		
			BizDBCPInit.log.info("DB URL : " + BizDBCPInit.DB_URL);
			BizDBCPInit.log.info("USER NAME : " + BizDBCPInit.USER_NAME);
			
			ConnectionFactory connFactory = new DriverManagerConnectionFactory(BizDBCPInit.DB_URL, BizDBCPInit.USER_NAME, BizDBCPInit.PASSWORD);

			//close 할경우 종료하지 않고 커넥션 풀에 반환
			PoolableConnectionFactory poolableConnFactory = new PoolableConnectionFactory(connFactory, null);

			//커넥션이 유효한지 확인
			poolableConnFactory.setValidationQuery(" SELECT 1 FROM DUAL ");

			//커넥션 풀의 설정 정보를 생성
			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

			//유효 커넥션 검사 주기
			poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 60L * 1L);

			//풀에 있는 커넥션이 유효한지 검사 유무 설정
			poolConfig.setTestWhileIdle(true);

			//기본값  : false /true 일 경우 validationQuery 를 매번 수행한다.
			poolConfig.setTestOnBorrow(false);

			//커넥션 최소갯수 설정
			poolConfig.setMinIdle(10);

		    poolConfig.setMaxIdle(10);

			//커넥션 최대 갯수 설정
			poolConfig.setMaxTotal(30);

			GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnFactory,poolConfig);

			//PoolableConnectionFactory 커넥션 풀 연결
			poolableConnFactory.setPool(connectionPool);

			//커넥션 풀을 제공하는 jdbc 드라이버 등록
			Class.forName("org.apache.commons.dbcp2.PoolingDriver");

			PoolingDriver driver = (PoolingDriver)DriverManager.getDriver("jdbc:apache:commons:dbcp:");

			driver.registerPool("cp", connectionPool);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static final Connection getConnection() throws Exception {

	    String jdbcDriver = "jdbc:apache:commons:dbcp:cp";

	            return DriverManager.getConnection(jdbcDriver);

	    }
}
