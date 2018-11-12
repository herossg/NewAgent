import java.sql.*;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;

public class Nano_it_summary implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private final String DB_URL = "jdbc:mysql://210.114.225.53/dhn?characterEncoding=utf8";
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	public static boolean isRunning = false;
	public Logger log;
	
	public void run() {
		if(!Nano_it_summary.isRunning) {
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
		Nano_it_summary.isRunning = true;	
		//log.info("Nano it summary 실행");  수정 테스트...
		
		Connection conn = null;
		Connection nconn = null;
		Statement nano_msg = null;

		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);

			nano_msg = conn.createStatement();
			String nanomsg_str = "select cnm.remark4" + 
									"   ,(sn div 50) as part" + 
									"     ,group_concat(cnm.phn) as PHN" + 
									"     ,group_concat(concat('''','82', right(phn, length(phn)-1)), '''') as msg_phn" + 
									"     ,group_concat(cnm.sn) as sn" + 
									"     ,wms.mst_lms_content as MSG_SMS" + 
									"     ,wms.mst_sms_callback as SMS_SENDER" + 
									"     ,cnm.msg_type" + 
									"     ,cm.mem_userid as user_id" + 
									"     ,cm.mem_id" + 
									"     ,cm.mem_level" + 
									"     ,wms.mst_reserved_dt as RESERVE_DT" + 
									"     ,max(sn) as max_sn" + 
									" from cb_nanoit_msg cnm " + 
									"inner join cb_wt_msg_sent wms" + 
									"   on cnm.remark4 = wms.mst_id " + 
									"inner join cb_member cm" + 
									"   on wms.mst_mem_id = cm.mem_id " + 
									"group by cnm.remark4" + 
									"        ,(sn div 50)" + 
									"        ,wms.mst_lms_content" + 
									"     ,wms.mst_sms_callback" + 
									"     ,cnm.msg_type " + 
									"order by remark4" + 
									"        ,sn";
			ResultSet rs = nano_msg.executeQuery(nanomsg_str);
			
			String pre_mem_id = "";
			Price_info price = null;
			while(rs.next()) {
				
				String mem_id = rs.getString("mem_id");
				String mem_resend = rs.getString("msg_type");
				String sent_key = rs.getString("remark4");
				int ins_cnt = rs.getString("PHN").split(",").length;
				String userid = rs.getString("user_id");
				
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
					price = new Price_info(Integer.valueOf(mem_id));
					pre_mem_id = mem_id;
				}
				
				String nanoDelstr = "delete from cb_nanoit_msg where sn in (" + rs.getString("sn") + ")";
				PreparedStatement nanodel = conn.prepareStatement(nanoDelstr);
				nanodel.executeUpdate();
				nanodel.close();
				
				switch(mem_resend) {
				case "015":
					
					String _015 = "insert into cb_pms_msg(rcv_phone"
													  + ",subject"
													  + ",text"
													  + ",msg_st"
													  + ",ins_dttm"
													  + ",req_dttm"
													  + ",cb_msg_id"
													  + ",remark4"
													  + ",max_sn)"
												+ "values(?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?)";
					PreparedStatement _015ins = conn.prepareStatement(_015);
					_015ins.setString(1, rs.getString("PHN"));
					if( rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").length()>40) {
						_015ins.setString(2, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").substring(0, 40));
					} else {
						_015ins.setString(2, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""));
					}
					_015ins.setString(3, rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " "));
					_015ins.setString(4, "0");
					_015ins.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
					_015ins.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
					_015ins.setString(7, mem_id);
					_015ins.setString(8, sent_key);
					_015ins.setString(9, rs.getString("max_sn"));
					_015ins.executeUpdate();
					_015ins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_015=ifnull(mst_015,0)+" + ins_cnt + " where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='15',CODE='015', MESSAGE = '결과 수신대기' "
							+ " where remark4=? and phn in (" + rs.getString("msg_phn") +")";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.executeUpdate();
					msgud.close();
										
					kind = "P";
					amount = price.member_price.price_015;
					payback = price.member_price.price_015 - price.parent_price.price_015;
					admin_amt = price.base_price.price_015;
					memo = "015저가문자";
					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount * (float)ins_cnt); 
					amtins.setString(4, memo); 
					amtins.setString(5, (sent_key+ "_" + rs.getString("max_sn"))); 
					amtins.setFloat(6, payback * (float)ins_cnt); 
					amtins.setFloat(7, admin_amt * (float)ins_cnt); 
					
					amtins.executeUpdate();
					amtins.close();
					
					break;
				case "PHONE":
					String phonestr = "insert into cb_pms_msg(rcv_phone"
													  + ",subject"
													  + ",text"
													  + ",msg_st"
													  + ",ins_dttm"
													  + ",req_dttm"
													  + ",cb_msg_id"
													  + ",remark4"
													  + ",max_sn)"
												+ "values(?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?)";
					PreparedStatement Phoneins = conn.prepareStatement(phonestr);
					Phoneins.setString(1, rs.getString("PHN"));
					if(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").length()>40) {
						Phoneins.setString(2, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").substring(0, 40));
					} else {
						Phoneins.setString(2, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""));
					}
					Phoneins.setString(3, rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " "));
					Phoneins.setString(4, "0");
					Phoneins.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
					Phoneins.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
					Phoneins.setString(7, mem_id);
					Phoneins.setString(8, sent_key);
					Phoneins.setString(9, rs.getString("max_sn"));
					Phoneins.executeUpdate();
					Phoneins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_phn=ifnull(mst_phn,0)+" + ins_cnt + " where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ph',CODE='PHN', MESSAGE = '결과 수신대기' "
							+ " where remark4=? and phn in (" + rs.getString("msg_phn") +")";
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.executeUpdate();
					msgud.close();
										
					kind = "P";
					amount = price.member_price.price_phn;
					payback = price.member_price.price_phn - price.parent_price.price_phn;
					admin_amt = price.base_price.price_phn;
					memo = "폰문자";
					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount * (float)ins_cnt); 
					amtins.setString(4, memo); 
					amtins.setString(5, (sent_key + "_" + rs.getString("max_sn"))); 
					amtins.setFloat(6, payback * (float)ins_cnt); 
					amtins.setFloat(7, admin_amt * (float)ins_cnt); 
					
					amtins.executeUpdate();
					amtins.close();
					
					break;	 
				case "GRS":
					String grsstr = "insert into cb_grs_msg(msg_gb"
													  + ",msg_st"
													  + ",msg_snd_phn"
													  + ",msg_rcv_phn"
													  + ",subject"
													  + ",text"
													  + ",cb_msg_id"
													  + ",remark4"
													  + ",max_sn"
													  + ",msg_req_dttm"
													  + ",msg_ins_dttm)"
												+ "values(?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?)";
					PreparedStatement grsins = conn.prepareStatement(grsstr);
					grsins.setString(1, "LMS");
					grsins.setString(2, "0");
					grsins.setString(3, rs.getString("SMS_SENDER") );
					grsins.setString(4, rs.getString("PHN"));
					if(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").length()>36) {
						grsins.setString(5, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", "").substring(0, 36));
					}else {
						grsins.setString(5, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""));
					}
					grsins.setString(6, rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " ") );
					grsins.setString(7, mem_id);
					grsins.setString(8, sent_key);
					grsins.setString(9, rs.getString("max_sn"));
					grsins.setTimestamp(10, new java.sql.Timestamp(System.currentTimeMillis())); 
					grsins.setTimestamp(11, new java.sql.Timestamp(System.currentTimeMillis())); 
					grsins.executeUpdate();
					grsins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_grs=ifnull(mst_grs,0)+" + ins_cnt + " where mst_id=?";
					wtud = conn.prepareStatement(wtudstr);
					wtud.setString(1, sent_key);
					wtud.executeUpdate();
					wtud.close();
					
					msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs',CODE='GRS', MESSAGE = '결과 수신대기' "
							+ " where remark4=? and phn in (" + rs.getString("msg_phn") +")";
					
					msgud = conn.prepareStatement(msgudstr);
					msgud.setString(1, sent_key);
					msgud.executeUpdate();
					//log.info(msgud.toString());
					msgud.close();
					
										
					kind = "P";
					amount = price.member_price.price_grs;
					payback = price.member_price.price_grs - price.parent_price.price_grs;
					admin_amt = price.base_price.price_grs;
					memo = "웹(A)";
					if(amount == 0 || amount == 0.0f) {
						amount = admin_amt;
					}

					amtins = conn.prepareStatement(amtStr);
					amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
					amtins.setString(2, kind); 
					amtins.setFloat(3, amount * (float)ins_cnt); 
					amtins.setString(4, memo); 
					amtins.setString(5, (sent_key+ "_" + rs.getString("max_sn"))); 
					amtins.setFloat(6, payback * (float)ins_cnt); 
					amtins.setFloat(7, admin_amt * (float)ins_cnt); 
					
					amtins.executeUpdate();
					amtins.close();
					
					break;	  
				}
				
			}
			
			rs.close();
			
		}catch(Exception ex) {
			log.info("Nano Summary 오류 - " + ex.toString());
		}
		
		try {
			if(nano_msg!=null) {
				nano_msg.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		
		Nano_it_summary.isRunning = false;
		//log.info("Nano it summary 끝");
	}
	
	 
}
