import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import com.mysql.jdbc.Driver;
import java.util.Date;

public class Nano_GRS_Proc implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private String DB_URL;
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	//public static boolean isRunning = false;
	public boolean isPremonth = false;
	public static boolean isPreRunning = false;
	public Logger log;
	public String monthStr;
	public static boolean[] isRunning = {false,false,false,false,false,false,false,false,false,false,};
	public int div_str;
	
	public Nano_GRS_Proc(String _db_url, Logger _log, int _div) {
		DB_URL = _db_url;
		log = _log;
		div_str = _div;
	}
	
	public void run() {
		if(!isRunning[div_str]  ) {
			if(monthStr == null || monthStr.isEmpty()) {
				Date month = new Date();
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
				monthStr = transFormat.format(month);
			}
			
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
	 
		isRunning[div_str] = true; 
		
		Connection conn = null;
		Connection nconn = null;
		Statement grs_msg = null;
		int totalcnt = 0;
		try {
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();

			grs_msg = conn.createStatement();
			String grs_str    = "select SQL_NO_CACHE cgm.cb_msg_id as msgid" + 
								"      ,cgm.msg_id" + 
								"      ,cgm.msg_st" + 
								"      ,cgm.max_sn" + 
								"      ,cgb.BC_RSLT_NO" + 
								"      ,(case" + 
								"         when cgb.bc_snd_st = '2' and cgb.bc_rslt_no = '0' and" + 
								"              addtime(cgb.bc_snd_dttm, '00:10:00') < now() then" + 
								"          '성공'" + 
								"         else" + 
								"          cgb.bc_rslt_text" + 
								"       end) as BC_RSLT_TEXT" + 
								"	  ,cgb.bc_rcv_phn" + 
								"      ,(case when cgb.bc_rcv_phn like '01%' then " +
								"                concat('82', right(cgb.bc_rcv_phn, length(cgb.bc_rcv_phn) - 1)) " +
								"             else " +
								"                cgb.bc_rcv_phn" +
								"        end) as PHN" + 
								"      ,cgm.REMARK4" + 
								"      ,cm.mem_userid" + 
								"      ,cm.mem_level" + 
								"      ,cm.mem_phn_agent" + 
								"      ,cm.mem_sms_agent" + 
								"      ,cm.mem_2nd_send" + 
								"      ,cm.mem_id" + 
								"      ,cgm.FILE_PATH1 as mms1" + 
								"      ,cgm.FILE_PATH2 as mms2" + 
								"      ,cgm.FILE_PATH3 as mms3" + 
								"  from cb_grs_msg_bk cgm" + 
								" inner join cb_grs_broadcast_"+ monthStr +" cgb" + 
								"    on cgm.msg_id = cgb.msg_id" + 
								"   and cgm.msg_rcv_phn regexp CONCAT( '^', cgb.bc_rcv_phn, ',|,', cgb.BC_RCV_PHN, '$', '|,', cgb.BC_RCV_PHN, ',|^',cgb.BC_RCV_PHN,'$')" + 
								" inner join cb_member cm" + 
								"    on cm.mem_id = cgm.cb_msg_id" + 
								" where cgm.msg_st in ('1', '0')" + 
								"   and cgb.bc_snd_st in( '3', '4') " + 
								"   and length(cgb.bc_rcv_phn) >= 8 " +
								"   and (cgm.msg_id % 10) = " + div_str +
								" order by cgm.msg_id limit 0" + 
								"         ,1000";
			
			ResultSet rs = grs_msg.executeQuery(grs_str);
			
			String pre_mem_id = "";
			Price_info price = null;
			
			
			while(rs.next()) {
				totalcnt++;
				//if(totalcnt <= 1)
				//	log.info("Nano GRS 처리 실행.( " + div_str + " )"); 

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
				// 성공 혹은 5일이 지나 기간만료 오류는 성공 처리 함.
				if(rs.getString("BC_RSLT_NO").equals("0") || rs.getString("BC_RSLT_NO").equals("111")) {
					
					wtudstr = "update cb_wt_msg_sent set mst_grs = ifnull(mst_grs,0) + 1, mst_wait = mst_wait - 1  where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs', MESSAGE = '웹(A) 성공', RESULT = 'Y' "
							+ " where remark4=? and phn = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.setString(2, rs.getString("PHN"));
					msgud.executeUpdate();
					msgud.close();
				} else {
					wtudstr = "update cb_wt_msg_sent set mst_err_grs = ifnull(mst_err_grs,0) + 1, mst_wait = mst_wait - 1  where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs', MESSAGE = ?, RESULT = 'N' "
							+ " where remark4=? and phn = ?";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, rs.getString("BC_RSLT_NO"));
					msgud.setString(2, sent_key);
					msgud.setString(3, rs.getString("PHN"));
					msgud.executeUpdate();
					msgud.close();
										
					kind = "3";
					String mms1 = rs.getString("mms1");
					String mms2 = rs.getString("mms2");
					String mms3 = rs.getString("mms3");
					
					if( ( mms1 == null || mms1.isEmpty())  && ( mms2 == null || mms2.isEmpty()) && ( mms3 == null || mms3.isEmpty())) {
						amount = price.member_price.price_grs;
						payback = price.member_price.price_grs - price.parent_price.price_grs;
						admin_amt = price.base_price.price_grs;
						memo = "웹(A) 발송실패 환불";
					} else {
						amount = price.member_price.price_grs_mms;
						payback = price.member_price.price_grs_mms - price.parent_price.price_grs_mms;
						admin_amt = price.base_price.price_grs_mms;
						memo = "웹(A) MMS 발송실패 환불";
					}

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
				
				String udPmsStr  = "update cb_grs_msg_bk set MSG_RCV_PHN = replace(MSG_RCV_PHN,?,'-1') where msg_id = ? and max_sn = ?";
				PreparedStatement udPms = conn.prepareStatement(udPmsStr);
				udPms.setString(1, rs.getString("bc_rcv_phn"));
				udPms.setString(2, rs.getString("msg_id"));
				udPms.setString(3, rs.getString("max_sn"));
				udPms.executeUpdate();
				udPms.close();
			}

			if(div_str == 0) {
				String delstr = "DELETE FROM cb_grs_msg_bk WHERE REPLACE(MSG_RCV_PHN, '-1', '') not REGEXP '[0-9]'";
				PreparedStatement delquery = conn.prepareStatement(delstr);
				delquery.executeUpdate();
				delquery.close();
			}
			
			rs.close();
			
		}catch(Exception ex) {
			log.info("Nano GRS 오류 - " + ex.toString());
		}
		
		if(totalcnt > 0) {
			log.info("Nano GRS " + totalcnt + " 건 처리 함.( " + div_str + " )");
		}
		
		try {
			if(grs_msg!=null) {
				grs_msg.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		
		isRunning[div_str] = false;
		
		//log.info("Nano it summary 끝");
	}
	
	 
}
