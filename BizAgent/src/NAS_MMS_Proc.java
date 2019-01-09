import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class NAS_MMS_Proc implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private String DB_URL ;
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	public static boolean isRunning = false;
	public boolean isPremonth = false;
	public static boolean isPreRunning = false;
	public Logger log;
	public String monthStr;
	
	public NAS_MMS_Proc(String _db_url, Logger _log) {
		DB_URL = _db_url;
		log = _log;
	}
	
	public void run() {
		if(!isRunning || (isPremonth && !isPreRunning)) {
			if(monthStr == null || monthStr.isEmpty()) {
				Date month = new Date();
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
				monthStr = transFormat.format(month);
			}
			
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
		if(isPremonth) {
			isPreRunning = true;
		} else {
			isRunning = true;
		}

		//log.info("Nano it summary 실행");  수정 테스트...
		
		Connection conn = null;
		Statement bkgmms_msg = null;
		int totalcnt = 0;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);

			bkgmms_msg = conn.createStatement();
			String bkgmms_str = "SELECT   cml.ETC1 AS MSGID," + 
								"         cml.MSGKEY as MSGKEY," + 
								"         cml.RSLT as RSLT," + 
								"         cml.ETC2 AS REMARK4," + 
								"         cm.mem_userid," + 
								"         cm.mem_level," + 
								"         cm.mem_phn_agent," + 
								"         cm.mem_sms_agent," + 
								"         cm.mem_2nd_send," + 
								"         cm.mem_id," + 
								"         cml.STATUS as status," + 
								"         cml.FILE_PATH1 as mms1," + 
								"         cml.FILE_PATH2 as mms2," + 
								"         cml.FILE_PATH3 as mms3" + 
								"    FROM cb_nas_mms_msg_log_" + monthStr + " cml," + 
								"         cb_member cm" + 
								"   WHERE cml.etc4 = cm.mem_id" + 
								"     AND cml.status= '3'" + 
								" order by msgkey" + 
								"     limit 0, 1000";
			ResultSet rs = bkgmms_msg.executeQuery(bkgmms_str);
			
			String pre_mem_id = "";
			Price_info price = null;
			while(rs.next()) {
				totalcnt++;
				String mem_id = rs.getString("mem_id");
				String sent_key = rs.getString("REMARK4");
				String userid = rs.getString("mem_userid");
				String msgid = rs.getString("MSGID");
				
				String wtudstr;
				String msgudstr;
				PreparedStatement wtud;
				PreparedStatement msgud; 
				
				String 	amtStr = "insert into cb_amt_" + userid + "(amt_datetime," +
											                        "amt_kind," +
											                        "amt_amount," +
											                        "amt_memo," +
											                        "amt_reason," +
											                        "amt_payback," +
											                        "amt_admin)" +
											                 "values(?," +
															           "?," +	
															           "?," +	
															           "?," +	
															           "?," +	
															           "?," +	
															           "?)";
				PreparedStatement amtins;
				String kind = "";
				float amount = 0;
				String memo = "";
				float payback = 0;
				float admin_amt = 0;
				
				if(pre_mem_id != mem_id) {
					price = new Price_info(DB_URL, Integer.valueOf(mem_id));
					pre_mem_id = mem_id;
				}
				//성공 혹은 기간만료 오류는 성공 처리 함.
				if(rs.getString("RSLT").equals("0") || rs.getString("RSLT").equals("110")) {
					wtudstr = "update cb_wt_msg_sent set mst_nas = ifnull(mst_nas,0) + 1, mst_wait = mst_wait - 1  where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ns', MESSAGE = '웹(B) 성공', RESULT = 'Y' "
							+ " where MSGID = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, msgid);
					msgud.executeUpdate();
					msgud.close();
				} else {
					wtudstr = "update cb_wt_msg_sent set mst_err_nas = ifnull(mst_err_nas,0) + 1, mst_wait = mst_wait - 1  where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ns',CODE='NAS', MESSAGE = ?, RESULT = 'N' "
							+ " where MSGID = ?";
					//log.info(msgudstr);
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, rs.getString("RSLT"));
					msgud.setString(2, msgid);
					msgud.executeUpdate();
					msgud.close();
										
					kind = "3";
					String mms1 = rs.getString("mms1");
					if( mms1 == null || mms1.isEmpty() ) {
						amount = price.member_price.price_nas;
						payback = price.member_price.price_nas - price.parent_price.price_nas;
						admin_amt = price.base_price.price_nas;
						memo = "웹(B) 발송실패 환불";
					} else { 
						amount = price.member_price.price_nas_mms;
						payback = price.member_price.price_nas_mms - price.parent_price.price_nas_mms;
						admin_amt = price.base_price.price_nas_mms;
						memo = "웹(B) MMS 발송실패 환불";
					}
					
					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount); 
					amtins.setString(4, memo); 
					amtins.setString(5, msgid); 
					amtins.setFloat(6, payback * -1 ); 
					amtins.setFloat(7, admin_amt * -1 ); 
					
					amtins.executeUpdate();
					amtins.close();
					
				}
				
				String udFUNsmsStr  = "update cb_nas_mms_msg_log_" + monthStr + " set status = '5' where msgkey =? and status = ?";
				PreparedStatement udFUNsms = conn.prepareStatement(udFUNsmsStr);
				udFUNsms.setString(1, rs.getString("MSGKEY"));
				udFUNsms.setString(2, rs.getString("status"));
				udFUNsms.executeUpdate();
				udFUNsms.close();
			}
 
			rs.close();
			
		}catch(Exception ex) {
			log.info("NASelf MMS 오류 - " + ex.toString());
		}
		
		if(totalcnt > 0) {
			log.info("NASelf MMS " + totalcnt + " 건 처리 함.");
		}
		
		try {
			if(bkgmms_msg!=null) {
				bkgmms_msg.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		
		if(isPremonth) {
			isPreRunning = false;
		} else {
			isRunning = false;
		}
		//log.info("Nano it summary 끝");
	}
	
	 
}
