import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;
import java.util.Date;

public class Nano_PMS_Proc implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private String DB_URL;
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	public static boolean isRunning = false;
	public boolean isPremonth = false;
	public static boolean isPreRunning = false;
	public Logger log;
	public String monthStr;
	
	public Nano_PMS_Proc(String _db_url, Logger _log) {
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
		Connection nconn = null;
		Statement pms_msg = null;
		int totalcnt = 0;
		try {
			//Class.forName(JDBC_DRIVER);
			conn =  BizDBCPInit.getConnection(); //conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);

			pms_msg = conn.createStatement();
			String pms_str    = "select cpm.cb_msg_id as msgid" + 
								"      ,'M103' as code" + 
								"      ,'pn' as message_type" + 
								"      ,cpm.REMARK4" + 
								"      ,cpm.max_sn" + 
								"      ,cpm.text as msg" + 
								"      ,cpm.text as msg_sms" + 
								"      ,cpl.BC_RCV_PHONE" + 
								"      ,(case when cpl.BC_RCV_PHONE like '01%' then " +
								"                concat('82', right(cpl.bc_rcv_phone, length(cpl.bc_rcv_phone) - 1)) " +
								"             else " +
								"                cpl.BC_RCV_PHONE" +
								"        end) as PHN" + 
								"      ,cpm.req_dttm as reg_date" + 
								"      ,cpm.MSG_ID" + 
								"      ,cpl.BC_RSLT_NO " + 
								"      ,cpl.BC_RSLT_TEXT" + 
								"      ,cpl.req_snd_dttm" + 
								"      ,cm.mem_userid" + 
								"      ,cm.mem_level" + 
								"      ,cm.mem_phn_agent" + 
								"      ,cm.mem_sms_agent" + 
								"      ,cm.mem_2nd_send" + 
								"      ,cm.mem_id" + 
								"  from cb_pms_msg_bk cpm" + 
								" inner join CB_PMS_BROADCAST_LOG_" + monthStr + " cpl" + 
								"    on cpm.msg_id = cpl.msg_id" + 
								"   and cpm.rcv_phone like concat('%', cpl.bc_rcv_phone, '%')" + 
								" inner join cb_member cm" + 
								"    on cm.mem_id = split(cpm.cb_msg_id, '_', 1)" + 
								" where cpm.msg_st = '1'" + 
								"   and cpl.BC_SND_ST in (3, 4)" + 
								" order by cpm.msg_id limit 0" + 
								"         ,1000";
			
			ResultSet rs = pms_msg.executeQuery(pms_str);
			
			String pre_mem_id = "";
			Price_info price = null;
			while(rs.next()) {
				totalcnt++;
				String mem_id = rs.getString("mem_id");
				String sent_key = rs.getString("REMARK4");
				String userid = rs.getString("mem_userid");
				
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
				
				if(rs.getString("BC_RSLT_NO").equals("0")) {
					wtudstr = "update cb_wt_msg_sent set mst_phn = ifnull(mst_phn,0) + 1, mst_wait = mst_wait-1  where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ph', MESSAGE = '폰문자 성공', RESULT = 'Y' "
							+ " where remark4=? and phn = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.setString(2, rs.getString("PHN"));
					msgud.executeUpdate();
					msgud.close();
				} else {
					wtudstr = "update cb_wt_msg_sent set mst_err_phn = ifnull(mst_err_phn,0) + 1, mst_wait = mst_wait-1 where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ph', MESSAGE = ?, RESULT = 'N' "
							+ " where remark4=? and phn = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, rs.getString("BC_RSLT_NO"));
					msgud.setString(2, sent_key);
					msgud.setString(3, rs.getString("PHN"));
					msgud.executeUpdate();
					msgud.close();
										
					kind = "3";
					amount = price.member_price.price_phn;
					payback = price.member_price.price_phn - price.parent_price.price_phn;
					admin_amt = price.base_price.price_phn;
					memo = "폰문자 발송실패 환불";
					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount); 
					amtins.setString(4, memo); 
					amtins.setString(5, (sent_key+ "_" + rs.getString("max_sn") + "_" + rs.getString("PHN"))); 
					amtins.setFloat(6, payback * -1 ); 
					amtins.setFloat(7, admin_amt * -1 ); 
					
					amtins.executeUpdate();
					amtins.close();
					
				}
				
				String udPmsStr  = "update cb_pms_msg_bk set rcv_phone = replace(rcv_phone,?,'-1') where msg_id = ? and max_sn = ?";
				PreparedStatement udPms = conn.prepareStatement(udPmsStr);
				udPms.setString(1, rs.getString("BC_RCV_PHONE"));
				udPms.setString(2, rs.getString("MSG_ID"));
				udPms.setString(3, rs.getString("max_sn"));
				udPms.executeUpdate();
				udPms.close();
			}

			String delstr = "DELETE FROM cb_pms_msg_bk WHERE REPLACE(RCV_PHONE, '-1', '') not REGEXP '[0-9]'";
			PreparedStatement delquery = conn.prepareStatement(delstr);
			delquery.executeUpdate();
			delquery.close();
			
			rs.close();
			
		}catch(Exception ex) {
			log.info("Nano 폰문자 처리 오류 - " + ex.toString());
		}
		if(totalcnt > 0) {
			log.info("Nano 폰문자  " + totalcnt + " 건 처리 함.");
		}
		
		try {
			if(pms_msg!=null) {
				pms_msg.close();
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
