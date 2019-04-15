import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import com.mysql.jdbc.Driver;
import java.util.Date;

import javax.net.ssl.SSLEngineResult.Status;

public class Imc_Proc implements Runnable {


	private String DB_URL;
	
	public boolean isPremonth = false;
	public Logger log;
	public String monthStr;
	public static boolean isRunning = false;
	
	public Imc_Proc(String _db_url, Logger _log) {
		DB_URL = _db_url;
		log = _log;

	}
	
	public void run() {
		if(!isRunning) {
			isRunning = true;
			Proc();
		}
	}
	
	private synchronized  void Proc() {
	 
		
		Connection conn = null;
		Statement imc_msg = null;
		int stotalcnt = 0;
		int ftotalcnt = 0;

		try {

			conn = BizDBCPInit.getConnection();
			
			if(isRunning) {
				// 성공 처리
				imc_msg = conn.createStatement();
				String imc_str    = "select SQL_NO_CACHE ir.message_id " + 
									"	   ,cm.mem_userid " + 
									"	   ,cm.mem_level  " + 
									"	   ,cm.mem_phn_agent " + 
									"	   ,cm.mem_sms_agent " + 
									"	   ,cm.mem_2nd_send " + 
									"	   ,cm.mem_id " + 
									"	   ,ims.sub_id as REMARK4 " + 
									"	   ,ims.name as cb_msg_id " + 
									"  from IMC.IMC_MART_SUB ims  " + 
									"  		 inner join IMC.IMC_REPORT ir  " + 
									" 		       on ims.request_id = ir.message_id  " + 
									"		       and ir.proc_flag = 'N' " + 
									"		 inner join cb_member cm " + 
									"		       on cm.mem_userid = ims.user_id" ;
				
				ResultSet rs = imc_msg.executeQuery(imc_str);
				
				while(rs.next()) {
					stotalcnt++;
					//if(totalcnt <= 1)
					//	log.info("Nano GRS 처리 실행.( " + div_str + " )"); 
	
					String mem_id = rs.getString("mem_id");
					String sent_key = rs.getString("REMARK4");
					String userid = rs.getString("mem_userid");
					
					String wtudstr;
					String msgudstr;
					PreparedStatement wtud;
					PreparedStatement msgud; 
					
					wtudstr = "update cb_wt_msg_sent set mst_imc = ifnull(mst_imc,0) + 1, mst_wait = mst_wait - 1 where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='im', MESSAGE = '폰 성공', RESULT = 'Y' "
							+ " where remark4=? and msgid = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.setString(2, rs.getString("cb_msg_id"));
					msgud.executeUpdate();
					msgud.close();
					
					String imc_del = "delete from IMC.IMC_MART_SUB where request_id = ?  ";
					PreparedStatement st_imc_del = conn.prepareStatement(imc_del);
					st_imc_del.setString(1,  rs.getString("message_id"));
					st_imc_del.executeUpdate();
					st_imc_del.close();
 
				}
				
				rs.close();
				
				// 실패 처리
				String fimc_str    = "select SQL_NO_CACHE ir.message_id " +
									"      ,ir.error " + 
									"	   ,cm.mem_userid " + 
									"	   ,cm.mem_level  " + 
									"	   ,cm.mem_phn_agent " + 
									"	   ,cm.mem_sms_agent " + 
									"	   ,cm.mem_2nd_send " + 
									"	   ,cm.mem_id " + 
									"	   ,ims.sub_id as REMARK4 " + 
									"	   ,ims.name as cb_msg_id " + 
									"  from IMC.IMC_MART_SUB ims  " + 
									"  		 inner join IMC.IMC_SEND ir  " + 
									" 		       on ims.request_id = ir.message_id  " + 
									"		       and ir.send_status = 'FAIL' " + 
									"		 inner join cb_member cm " + 
									"		       on cm.mem_userid = ims.user_id" ;
				
				ResultSet frs = imc_msg.executeQuery(fimc_str);
				
				String pre_mem_id = "";
				Price_info price = null;
				
				while(frs.next()) {
					ftotalcnt++;
					//if(totalcnt <= 1)
					//	log.info("Nano GRS 처리 실행.( " + div_str + " )"); 
	
					String mem_id = frs.getString("mem_id");
					String sent_key = frs.getString("REMARK4");
					String userid = frs.getString("mem_userid");
					
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
					
					wtudstr = "update cb_wt_msg_sent set mst_err_imc = ifnull(mst_err_imc,0) + 1, mst_wait = mst_wait - 1 where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='im', MESSAGE = ?, RESULT = 'N' "
							+ " where remark4=? and msgid = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, frs.getString("error"));
					msgud.setString(2, sent_key);
					msgud.setString(3, frs.getString("cb_msg_id"));
					msgud.executeUpdate();
					msgud.close();

					kind = "3";
					amount = price.member_price.price_imc;
					payback = price.member_price.price_imc - price.parent_price.price_imc;
					admin_amt = price.base_price.price_imc;
					memo = "폰문자  발송실패 환불";

					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount); 
					amtins.setString(4, memo); 
					amtins.setString(5, (sent_key+ "_" + frs.getString("cb_msg_id"))); 
					amtins.setFloat(6, payback * -1 ); 
					amtins.setFloat(7, admin_amt * -1 ); 
					amtins.executeUpdate();
					amtins.close();
					
					String imc_del = "delete from IMC.IMC_MART_SUB where request_id = ?  ";
					PreparedStatement st_imc_del = conn.prepareStatement(imc_del);
					st_imc_del.setString(1,  frs.getString("message_id"));
					st_imc_del.executeUpdate();
					st_imc_del.close();
 
				}
				
				frs.close();				
			}
			
 

		}catch(Exception ex) {
			log.info("IMC 처리 오류 - " + ex.toString());
		}
		
		if(stotalcnt > 0 || ftotalcnt > 0) {
			log.info("IMC 처리 - 성공 : " + stotalcnt + " , 실패 : " + ftotalcnt);
		}
		
		try {
			if(imc_msg!=null) {
				imc_msg.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		
		isRunning = false;
	}
	 
}
