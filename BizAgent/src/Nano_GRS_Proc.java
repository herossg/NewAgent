import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import com.mysql.jdbc.Driver;
import java.util.Date;

import javax.net.ssl.SSLEngineResult.Status;

public class Nano_GRS_Proc implements Runnable {

	private String DB_URL;
	
	//public static boolean isRunning = false;
	public boolean isPremonth = false;
	public static boolean isPreRunning = false;
	public Logger log;
	public String monthStr;
	public boolean isRunning = false;
	public String div_str;
	//static int proc_cnt;

	public boolean isRefund;

	public Nano_GRS_Proc(String _db_url, Logger _log) {
		DB_URL = _db_url;
		log = _log;

		Date month = new Date();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		div_str = transFormat.format(month);

	}
	
	public void run() {
		if(BizAgent.GRS_Proc_cnt <= 20) {
			if(monthStr == null || monthStr.isEmpty()) {
				Date month = new Date();
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
				monthStr = transFormat.format(month);
			}
			
			Proc();
		}
	}
	
	private synchronized  void Proc() {
	 
		
		Connection conn = null;
		Statement grs_msg = null;
		int totalcnt = 0;
		BizAgent.GRS_Proc_cnt ++;
		try {
 
			conn = BizDBCPInit.getConnection();

			String updateStr = "update cb_grs_broadcast_"+ monthStr +" cgb " + 
					              "set cgb.BC_SND_ST = '3' " + 
					            "WHERE cgb.bc_snd_st = '2' " + 
					              "and date_add(cgb.BC_SND_DTTM, interval 12 HOUR) < now()";
			
			Statement updateExe = conn.createStatement();
			updateExe.execute(updateStr);
			
			updateExe.close();

			String grs_proc_str = "SELECT count(1) as cnt" + 
		              "  FROM cb_nano_broadcast_list b " + 
		         " inner join cb_grs_broadcast_" + monthStr + " a on a.MSG_ID = b.msg_id and a.BC_RCV_PHN = b.rcv_phone " + 
		              " WHERE a.BC_SND_ST IN (3, 4) " + 
		                " and b.proc_str is null ";
			Statement grs_proc_st = conn.createStatement();
			ResultSet proc_rs = grs_proc_st.executeQuery(grs_proc_str);
			if(proc_rs.next()) {
				if(proc_rs.getInt(1) > 0) {
					String grs_proc_ud = "update cb_nano_broadcast_list b set proc_str= '" + div_str + 
							            "' where exists (select 1 from cb_grs_broadcast_" + monthStr  + " a " +
							            "                        where  a.MSG_ID = b.msg_id and a.BC_RCV_PHN = b.rcv_phone  " +
							                                     " and  a.bc_snd_st in ('3', '4') ) " +
							            "    and b.proc_str is null " +
							            "  order by b.msg_id limit 300";
					
					Statement grs_proc_ud_st = conn.createStatement();
					grs_proc_ud_st.execute(grs_proc_ud);
					grs_proc_ud_st.close();
					
					isRunning = true;
							           
				}
			}
			proc_rs.close();
			grs_proc_st.close();
			
			if(isRunning) {
				grs_msg = conn.createStatement();
				String grs_str    = "select SQL_NO_CACHE cgm.msg_id" + 
									"      ,cgm.max_sn" + 
									"      ,cgb.BC_RSLT_NO" + 
									"      ,cgb.bc_rslt_text as BC_RSLT_TEXT" + 
									"	   ,cgb.bc_rcv_phn" + 
									"      ,(case when cgb.bc_rcv_phn like '01%' then " +
									"                concat('82', right(cgb.bc_rcv_phn, length(cgb.bc_rcv_phn) - 1)) " +
									"             else " +
									"                cgb.bc_rcv_phn" +
									"        end) as PHN" + 
									"      ,cgm.mst_id as REMARK4" + 
									"      ,cm.mem_userid" + 
									"      ,cm.mem_level" + 
									"      ,cm.mem_phn_agent" + 
									"      ,cm.mem_sms_agent" + 
									"      ,cm.mem_2nd_send" + 
									"      ,cm.mem_id" + 
									"      ,cgm.FILE_PATH1 as mms1" + 
									"      ,cgm.FILE_PATH2 as mms2" + 
									"      ,cgm.FILE_PATH3 as mms3" + 
									"      ,cgm.cb_msg_id " + 
									"      ,cgb.msg_gb " +
									"      ,cgb.BC_MSG_ID " +
									"  from cb_nano_broadcast_list cgm" + 
									" inner join cb_grs_broadcast_"+ monthStr +" cgb" + 
									"    on cgm.msg_id = cgb.msg_id" + 
									"   and cgm.rcv_phone = cgb.bc_rcv_phn" + 
									" inner join cb_member cm" + 
									"    on cm.mem_id = cgm.mem_id" + 
									" where cgb.bc_snd_st in( '3', '4') " + 
									"   and length(cgb.bc_rcv_phn) >= 8 " +
									"   and cgm.proc_str = '" + div_str + "'" ;
				
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
					
					int mst_grs_biz_qty = 0;
					
					// 성공 혹은 5일이 지나 기간만료 오류는 성공 처리 함.
					if(rs.getString("BC_RSLT_NO").equals("0") || rs.getString("BC_RSLT_NO").equals("111")) {
						
						if(rs.getString("msg_gb").equals("LMS")) 
							mst_grs_biz_qty = 1;
						
						wtudstr = "update cb_wt_msg_sent set mst_grs = ifnull(mst_grs,0) + 1, mst_wait = mst_wait - 1, mst_grs_biz_qty = ifnull(mst_grs_biz_qty,0) + " + mst_grs_biz_qty + "   where mst_id=?";
						wtud = conn.prepareStatement(wtudstr);
						wtud.setString(1, sent_key);
						wtud.executeUpdate();
						wtud.close();
						
						msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs', MESSAGE = '웹(A) 성공', RESULT = 'Y' "
								+ " where remark4=? and msgid = ?";
						msgud = conn.prepareStatement(msgudstr);
						msgud.setString(1, sent_key);
						msgud.setString(2, rs.getString("cb_msg_id"));
						msgud.executeUpdate();
						msgud.close();
					} else if(this.isRefund){
						wtudstr = "update cb_wt_msg_sent set mst_err_grs = ifnull(mst_err_grs,0) + 1, mst_wait = mst_wait - 1  where mst_id=?";
						wtud = conn.prepareStatement(wtudstr);
						wtud.setString(1, sent_key);
						wtud.executeUpdate();
						wtud.close();
						
						msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs', MESSAGE = ?, RESULT = 'N' "
								+ " where remark4=? and msgid = ?";
						msgud = conn.prepareStatement(msgudstr);
						msgud.setString(1, rs.getString("BC_RSLT_NO"));
						msgud.setString(2, sent_key);
						msgud.setString(3, rs.getString("cb_msg_id"));
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
					
					String nanobc_del = "delete from cb_nano_broadcast_list where msg_id = ? and rcv_phone = ?";
					PreparedStatement st_nanobc_del = conn.prepareStatement(nanobc_del);
					st_nanobc_del.setString(1,  rs.getString("msg_id"));
					st_nanobc_del.setString(2, rs.getString("bc_rcv_phn"));
					st_nanobc_del.executeUpdate();
					st_nanobc_del.close();

					String nanobc_st_ud = "update cb_grs_broadcast_"+ monthStr +" set bc_snd_st = bc_snd_st + 2 where bc_msg_id = ?";
					PreparedStatement st_nanobc_st_ud = conn.prepareStatement(nanobc_st_ud);
					st_nanobc_st_ud.setString(1,  rs.getString("BC_MSG_ID"));
					st_nanobc_st_ud.executeUpdate();
					st_nanobc_st_ud.close();

					/*
					 * broadcast 를 이용하는 방법으로 재 개발 예정 적용시 아래 내용은 삭제 예정
					 */
					
				/*	String udPmsStr  = "update cb_grs_msg_bk set MSG_RCV_PHN = replace(MSG_RCV_PHN,?,'-1') where msg_id = ? and max_sn = ?";
					PreparedStatement udPms = conn.prepareStatement(udPmsStr);
					udPms.setString(1, rs.getString("bc_rcv_phn"));
					udPms.setString(2, rs.getString("msg_id"));
					udPms.setString(3, rs.getString("max_sn"));
					udPms.executeUpdate();
					udPms.close();*/
				}
	
				/*if(div_str == 0) {
					String delstr = "DELETE FROM cb_grs_msg_bk WHERE REPLACE(MSG_RCV_PHN, '-1', '') not REGEXP '[0-9]'";
					PreparedStatement delquery = conn.prepareStatement(delstr);
					delquery.executeUpdate();
					delquery.close();
				}*/
				
				rs.close();
			}
			
			/*
			 * 구분된 처리 에서 혹시 제외된 건이 있으면 다시 처리 하기 위해서 Null 처리 함.
			 */
			String grs_proc_ud = "update cb_nano_broadcast_list b set proc_str = null " + 
		                         " where proc_str = '" + div_str + "'" ;

			Statement grs_proc_ud_st = conn.createStatement();
			grs_proc_ud_st.execute(grs_proc_ud);
			grs_proc_ud_st.close();

		}catch(Exception ex) {
			log.info("" + div_str + " 처리 오류 - " + ex.toString());
			
			String grs_proc_ud = "update cb_nano_broadcast_list b set proc_str = null " + 
                    " where proc_str = '" + div_str + "'" ;
			
			try {
				Statement grs_err_ud_st = conn.createStatement();
				grs_err_ud_st.execute(grs_proc_ud);
				grs_err_ud_st.close();
			} catch(Exception e) {
				log.info("" + div_str + " 초기화 실패");
			} finally {
				log.info("" + div_str + " 초기화 성공 ");
			}
		}
		
		if(totalcnt > 0) {
			log.info("Nano GRS " + totalcnt + " 건 처리 함.( " + div_str + " )_Total Proc : " + BizAgent.GRS_Proc_cnt);
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
		
		BizAgent.GRS_Proc_cnt --;
		//isRunning[div_str] = false;
		
		//log.info("Nano it summary 끝");
	}
	
	 
}
