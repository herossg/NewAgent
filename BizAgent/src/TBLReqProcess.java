import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;

public class TBLReqProcess implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	private String DB_URL;
//	private final String USER_NAME = "root";
//	private final String PASSWORD = "sjk4556!!22";
	
//	private final String NURL = "jdbc:mysql://125.128.249.42/bizsms";
//	private final String NUSER_NAME = "bizsms";
//	private final String NPASSWORD = "!@nanum0915";
	public int div_str;
	
	public static boolean[] isRunning = {false,false,false,false,false,false,false,false,false,false,};
	public Logger log;
	
	Properties p = BizAgent.getProp();
	
	public TBLReqProcess(String _db_url, Logger _log, int _div) {
		DB_URL = _db_url;
		log = _log;
		div_str = _div;
	}
	
	public void run() {
		if(!TBLReqProcess.isRunning[div_str]) {
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
		TBLReqProcess.isRunning[div_str] = true;	
		
		//log.info("TBL RESULT PROC 실행");
		Connection conn = null;
		Connection nconn = null;
		Statement tbl_result = null;
		Connection smtconn = null;
		boolean isPass = false; // 전체 Loop 를 그냥 지나 가기 위한 변수 Loop 가 시작시에는 무조건 False
		try {
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();
			
			if(p.get("SMTPHNDB").equals("1")) {
				smtconn = SmtDBCPInit.getConnection();
			}
			
			tbl_result = conn.createStatement();
						
	        Date reserve_dt = new Date();
	        SimpleDateFormat rd = new SimpleDateFormat("yyyyMMddHHmmss");
	 		
			String sqlstr = "select SQL_NO_CACHE a.*" + 
					"              ,b.mem_userid " + 
					"              ,b.mem_id " + 
					"              ,b.mem_level" + 
					"              ,b.mem_phn_agent" + 
					"              ,b.mem_sms_agent" + 
					"              ,b.mem_2nd_send" + 
					"              ,(select mst_mms_content from cb_wt_msg_sent wms where wms.mst_id = a.remark4) as mms_id" + 
					"              ,(select mst_type2 from cb_wt_msg_sent wms where wms.mst_id = a.remark4) as mst_type2" + 
					"          from TBL_REQUEST_RESULT a inner join cb_member b on b.mem_id = a.REMARK2" + 
					"         where REMARK3 = '" + div_str + "'" +
					"           and ( a.reserve_dt < '" + rd.format(reserve_dt) + "'" + 
					"              or a.reserve_dt = '00000000000000')" +
					"           order by reg_dt " +
					"          limit 0, 1000 ";
			ResultSet rs = tbl_result.executeQuery(sqlstr);
			
			String msgtype = "LMS";
			String pre_mem_id = "";
			Price_info price = null;
			int msgcnt = 0;
			while(rs.next()) {
				msgcnt ++;
				isPass = false;
				
				String userid = rs.getString("mem_userid");
				if(rs.getString("mst_type2").contains("s")) {
					msgtype = "SMS";
				} else {
					msgtype = "LMS";
				}
				
				//log.info("MSG ID : " + rs.getString("MSGID") + " 진행 시작 !!");
				String msg_id = rs.getString("MSGID");
				String mem_id = rs.getString("mem_id");
				String mem_lv = rs.getString("mem_level");
				String mem_phn = rs.getString("mem_phn_agent");
				String mem_sms = rs.getString("mem_sms_agent");
				String mem_p_invoice = rs.getString("P_INVOICE");
				String mem_resend = rs.getString("mem_2nd_send");
				String msg_sms = rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " ") ;
				String sent_key = rs.getString("REMARK4");
				String phn = "";
				String mem_2nd_type = "";
				try {
					//log.info("PHN : " + rs.getString("PHN") + " / " + rs.getString("PHN").substring(0, 2));
					if(rs.getString("PHN").substring(0, 2).equals("82")) {
						phn = "0" + rs.getString("PHN").substring(2);
					} else {
						phn = rs.getString("PHN");
					}
				} catch (StringIndexOutOfBoundsException e) {
					
				}
				
				if(mem_p_invoice != null && !mem_p_invoice.isEmpty() && rs.getString("MESSAGE_TYPE").equals("ph")) {
					mem_resend = mem_p_invoice;
					switch(mem_resend) {
					case "GREEN_SHOT":
						mem_2nd_type = "gs";
						break;
					case "NASELF":
						mem_2nd_type = "ns";
						break;
					case "SMART":
						mem_2nd_type = "sm";
						break;
					}
				} else {
					mem_2nd_type = rs.getString("MESSAGE_TYPE");
				}
				
				// 사용자별 단가를 불러 옴.
				if(pre_mem_id != mem_id) {
					price = new Price_info(DB_URL, Integer.valueOf(mem_id));
					pre_mem_id = mem_id;
				}
				
				String insstr = "insert into cb_msg_"+userid+"(MSGID," + 
									        "AD_FLAG," + 
									        "BUTTON1," + 
									        "BUTTON2," + 
									        "BUTTON3," + 
									        "BUTTON4," + 
									        "BUTTON5," + 
									        "CODE," + 
									        "IMAGE_LINK," + 
									        "IMAGE_URL," + 
									        "KIND," + 
									        "MESSAGE," + 
									        "MESSAGE_TYPE," + 
									        "MSG," + 
									        "MSG_SMS," + 
									        "ONLY_SMS," + 
									        "P_COM," + 
									        "P_INVOICE," + 
									        "PHN," + 
									        "PROFILE," + 
									        "REG_DT," + 
									        "REMARK1," + 
									        "REMARK2," + 
									        "REMARK3," + 
									        "REMARK4," + 
									        "REMARK5," + 
									        "RES_DT," + 
									        "RESERVE_DT," + 
									        "RESULT," + 
									        "S_CODE," + 
									        "SMS_KIND," + 
									        "SMS_LMS_TIT," + 
									        "SMS_SENDER," + 
									        "SYNC," + 
									        "TMPL_ID," + 
									        "mem_userid," +
									        "wide)" + 
								"	  values(?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?," + 
									        "?)";
				
				PreparedStatement insSt = conn.prepareStatement(insstr);
				insSt.setString(1 , rs.getString("MSGID"));
				insSt.setString(2 , rs.getString("AD_FLAG"));
				insSt.setString(3 , rs.getString("BUTTON1"));
				insSt.setString(4 , rs.getString("BUTTON2"));
				insSt.setString(5 , rs.getString("BUTTON3"));
				insSt.setString(6 , rs.getString("BUTTON4"));
				insSt.setString(7 , rs.getString("BUTTON5"));
				insSt.setString(8 , rs.getString("CODE"));
				insSt.setString(9 , rs.getString("IMAGE_LINK"));
				insSt.setString(10, rs.getString("IMAGE_URL"));
				insSt.setString(11, rs.getString("KIND"));
				insSt.setString(12, rs.getString("MESSAGE"));
				insSt.setString(13, mem_2nd_type);
				insSt.setString(14, "");
				insSt.setString(15, "");
				insSt.setString(16, rs.getString("ONLY_SMS"));
				insSt.setString(17, rs.getString("P_COM"));
				insSt.setString(18, rs.getString("P_INVOICE"));
				insSt.setString(19, rs.getString("PHN"));
				insSt.setString(20, rs.getString("PROFILE"));
				insSt.setString(21, rs.getString("REG_DT"));
				insSt.setString(22, rs.getString("REMARK1"));
				insSt.setString(23, rs.getString("REMARK2"));
				insSt.setString(24, null);
				insSt.setString(25, rs.getString("REMARK4"));
				insSt.setString(26, rs.getString("REMARK5"));
				insSt.setString(27, rs.getString("RES_DT"));
				insSt.setString(28, rs.getString("RESERVE_DT"));
				insSt.setString(29, rs.getString("RESULT"));
				insSt.setString(30, rs.getString("S_CODE"));
				insSt.setString(31, rs.getString("SMS_KIND"));
				insSt.setString(32, rs.getString("SMS_LMS_TIT"));
				insSt.setString(33, rs.getString("SMS_SENDER"));
				insSt.setString(34, rs.getString("SYNC"));
				insSt.setString(35, rs.getString("TMPL_ID"));
				insSt.setString(36, rs.getString("mem_userid"));
				insSt.setString(37, rs.getString("WIDE"));
				
				//log.info("한글 : " +rs.getString("MSG")+ "Insert Qeury : " + insSt.toString());
				try {
					insSt.executeUpdate();
				} catch(Exception ex) {
					/*
					 * Insert 에러 발생은 MSG ID 중복에 의한 것 말고는 없음.
					 * 처리함.
					 * 1. 성공이면 과금 되었는지 확인 하고 안되었으면 그냥 흘린다.
					 * 1-1. 과금이 되었다면 완전히 빠져 나간다.
					 */
					isPass = true;
					log.info("TBL Process Error : " + msg_id + "( " + ex.toString() + " ) ");
						
				}
				
				insSt.close();
				
				if(!isPass) {
					if(rs.getString("RESULT") != null && rs.getString("RESULT").equals("Y")) {
					// 발신 성공이면 금액 차감
						String kind = "";
						float amount = 0;
						String memo = "";
						float payback = 0;
						float admin_amt = 0;
						Statement upd = conn.createStatement();
						String upstr = "update cb_wt_msg_sent " ;
						if(rs.getString("MESSAGE_TYPE").equals("ft")) {
							if(rs.getString("IMAGE_URL") == null || rs.getString("IMAGE_URL").isEmpty()) {
								upstr = upstr + "set mst_ft = ifnull(mst_ft, 0) + 1 ";
								kind = "F";
								amount = price.member_price.price_ft;
								payback = price.member_price.price_ft - price.parent_price.price_ft;
								admin_amt = price.base_price.price_ft;
								memo = "친구톡(텍스트)";
							} else {
								upstr = upstr + "set mst_ft_img = ifnull(mst_ft_img, 0) + 1 ";
								kind = "I";
								if(rs.getString("WIDE")!= null && rs.getString("WIDE").equals("Y")) {
									amount = price.member_price.price_ft_w_img;
									payback = price.member_price.price_ft_w_img - price.parent_price.price_ft_w_img;
									admin_amt = price.base_price.price_ft_w_img;
									memo = "친구톡(와이드이미지)";
								} else {
									amount = price.member_price.price_ft_img;
									payback = price.member_price.price_ft_img - price.parent_price.price_ft_img;
									admin_amt = price.base_price.price_ft_img;
									memo = "친구톡(이미지)";
								}
							}
							upstr = upstr + " where mst_id = " + sent_key;
							
							// 친구톡 성공 목록 저장용
							// 기존에 있으면 지우고 추가
							String fldelstr = "delete from cb_friend_list where mem_id = ? and phn = ? ";
							PreparedStatement fldel = conn.prepareStatement(fldelstr);
							fldel.setInt(1, Integer.valueOf(mem_id));
							fldel.setString(2, phn);
							fldel.executeUpdate();
							fldel.close();
							
							String flstr = "insert into cb_friend_list(mem_id, phn, last_send_date) values(?,?,?)";
							PreparedStatement flins = conn.prepareStatement(flstr);
							flins.setInt(1, Integer.valueOf(mem_id));
							flins.setString(2, phn);
							flins.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
							
							flins.executeUpdate();
							flins.close();
							
						}else if(rs.getString("MESSAGE_TYPE").equals("at")) {
							upstr = upstr + "set mst_at = ifnull(mst_at, 0) + 1 where mst_id = " + sent_key;
							kind = "A";
							amount = price.member_price.price_at;
							payback = price.member_price.price_at - price.parent_price.price_at;
							admin_amt = price.base_price.price_at;
							memo = "알림톡";
						}
						
						upd.execute(upstr);
						upd.close();
						
						String amtStr = "insert into cb_amt_" + userid + "(amt_datetime," +
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
						PreparedStatement amtins = conn.prepareStatement(amtStr);
						amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
						amtins.setString(2, kind); 
						amtins.setFloat(3, amount); 
						amtins.setString(4, memo); 
						amtins.setString(5, msg_id); 
						amtins.setFloat(6, payback); 
						amtins.setFloat(7, admin_amt); 
						
						amtins.executeUpdate();
						amtins.close();
	
					} else {
					// 실패 이면서 2차 발신 대상이면 2차 발신 Table 에 Insert	
						/*if(rs.getString("MESSAGE_TYPE") != null && rs.getString("MESSAGE_TYPE").equals("ft")) {
		 					// 친구톡 발송 실패시 친구톡 성공 목록에서 삭제
							String fldelstr = "delete from cb_friend_list where mem_id = ? and phn = ? ";
							PreparedStatement fldel = conn.prepareStatement(fldelstr);
							fldel.setInt(1, Integer.valueOf(mem_id));
							fldel.setString(2, phn);
							fldel.executeUpdate();
							fldel.close();
						}
						*/

						String wtudstr;
						String msgudstr;
						PreparedStatement wtud;
						PreparedStatement msgud; 
						
						if( !rs.getString("MESSAGE").equals("InvalidPhoneNumber") && mem_resend != null &&  !mem_resend.isEmpty() && msg_sms !=null && !msg_sms.isEmpty() && !rs.getString("SMS_SENDER").isEmpty() ) {

							// 080 수신거부 체크
							boolean is_Block = false;
							
							String van_check = "select count(1) as cnt from cb_block_lists cbl where cbl.phn = ? and cbl.sender = ?";
							PreparedStatement van_checkps = conn.prepareStatement(van_check);
							van_checkps.setString(1, phn);
							van_checkps.setString(2, rs.getString("SMS_SENDER"));
							ResultSet van_list = van_checkps.executeQuery();
							
							if(van_list.next()) {
								if(van_list.getInt("cnt") > 0) {
									is_Block = true;
									
									
									String mc_mms1, mc_mms2, mc_mms3;
									
									mc_mms1 = "";
									mc_mms2 = "";
									mc_mms3 = "";
									
									if(rs.getString("mms_id").length()>5) {
										String mmsinfostr = "select * from cb_mms_images cmi where cmi.mem_id = '" + mem_id + "' and mms_id = '" + rs.getString("mms_id") + "'";
										Statement mmsinfo = conn.createStatement();
										ResultSet mmsrs = mmsinfo.executeQuery(mmsinfostr);
										mmsrs.first();
										
										mc_mms1 = mmsrs.getString("origin1_path");
										mc_mms2 = mmsrs.getString("origin2_path");
										mc_mms3 = mmsrs.getString("origin3_path");
									}
									
									String msgcnt_cal = "";
									String msg_type1 = "";
									String msg_type2 = "";
									
									switch(mem_resend) {
									case "015":
											if(msgtype.equals("SMS")) {
												msgcnt_cal = " mst_err_015 = ifnull(mst_err_015, 0) + 1 ";
												msg_type1 = "15";
												msg_type2 = "015";
											} else if(msgtype.equals("LMS")) {
												if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
													msgcnt_cal = " mst_err_015 = ifnull(mst_err_015, 0) + 1 ";
													msg_type1 = "15";
													msg_type2 = "015";
												} else {
													msgcnt_cal = " mst_err_015 = ifnull(mst_err_015, 0) + 1 ";
													msg_type1 = "15";
													msg_type2 = "015";
												}
											}
										break;
									case "PHONE":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_phn = ifnull(mst_err_phn, 0) + 1 ";
											msg_type1 = "ph";
											msg_type2 = "PHN";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_phn = ifnull(mst_err_phn, 0) + 1 ";
												msg_type1 = "ph";
												msg_type2 = "PHN";
											} else {
												msgcnt_cal = " mst_err_phn = ifnull(mst_err_phn, 0) + 1 ";
												msg_type1 = "ph";
												msg_type2 = "PHN";
											}
										}
										break;
									case "BKG":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_bkg = ifnull(mst_err_bkg, 0) + 1 ";
											msg_type1 = "gs";
											msg_type2 = "GRS";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_bkg = ifnull(mst_err_bkg, 0) + 1 ";
												msg_type1 = "gs";
												msg_type2 = "GRS";
											} else {
												msgcnt_cal = " mst_err_bkg = ifnull(mst_err_bkg, 0) + 1 ";
												msg_type1 = "gs";
												msg_type2 = "GRS";
											}
										}
										break;
									case "SMART":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_smt = ifnull(mst_err_smt, 0) + 1 ";
											msg_type1 = "SM";
											msg_type2 = "SMT";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_smt = ifnull(mst_err_smt, 0) + 1 ";
												msg_type1 = "SM";
												msg_type2 = "SMT";
											} else {
												msgcnt_cal = " mst_err_smt = ifnull(mst_err_smt, 0) + 1 ";
												msg_type1 = "SM";
												msg_type2 = "SMT";
											}
										}
										break;
									case "GREEN_SHOT":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_grs_sms = ifnull(mst_err_grs_sms, 0) + 1 ";
											msg_type1 = "gs";
											msg_type2 = "GRS";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_grs = ifnull(mst_err_grs, 0) + 1 ";
												msg_type1 = "gs";
												msg_type2 = "GRS";
											} else {
												msgcnt_cal = " mst_err_grs_mms = ifnull(mst_err_grs_mms, 0) + 1 ";
												msg_type1 = "gs";
												msg_type2 = "GRS";
											}
										}
										break;
									case "IMC":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
											msg_type1 = "im";
											msg_type2 = "IMC";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
												msg_type1 = "im";
												msg_type2 = "IMC";
											} else {
												msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
												msg_type1 = "im";
												msg_type2 = "IMC";
											}
										}
										break;
									case "SMT_PHN" :
									case "SMT_PHN_DB" :
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
											msg_type1 = "SM";
											msg_type2 = "SMT";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
												msg_type1 = "SM";
												msg_type2 = "SMT";
											} else {
												msgcnt_cal = " mst_err_imc = ifnull(mst_err_imc, 0) + 1 ";
												msg_type1 = "SM";
												msg_type2 = "SMT";
											}
										}
										break;
									case "NASELF":
										if(msgtype.equals("SMS")) {
											msgcnt_cal = " mst_err_nas_sms = ifnull(mst_err_nas_sms, 0) + 1 ";
											msg_type1 = "ns";
											msg_type2 = "NAS";
										} else if(msgtype.equals("LMS")) {
											if(mc_mms1.isEmpty() && mc_mms2.isEmpty() && mc_mms3.isEmpty()) {
												msgcnt_cal = " mst_err_nas = ifnull(mst_err_nas, 0) + 1 ";
												msg_type1 = "ns";
												msg_type2 = "NAS";
											} else {
												msgcnt_cal = " mst_err_nas_mms = ifnull(mst_err_nas_mms, 0) + 1 ";
												msg_type1 = "ns";
												msg_type2 = "NAS";
											}
										}
										break;
										
									}
									
									wtudstr = "update cb_wt_msg_sent set " + msgcnt_cal + " where mst_id=?";
									wtud = conn.prepareStatement(wtudstr);
									wtud.setString(1, sent_key);
									wtud.executeUpdate();
									wtud.close();
									
									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='" +msg_type1+ "',CODE='"+msg_type2+"', MESSAGE = '수신거부', RESULT='N' where MSGID=?";
									msgud = conn.prepareStatement(msgudstr);
									msgud.setString(1, msg_id);
									msgud.executeUpdate();
									msgud.close();
									
								}
									
							}			
							
							if(!is_Block) {
								
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
								
								String nanoit;
								PreparedStatement nanoins;
								
								
								switch(mem_resend) {
								case "015":
									nanoit = "insert into cb_nanoit_msg(msg_type, remark4, phn, cb_msg_id) values(?, ?, ?, ?)";
									nanoins = conn.prepareStatement(nanoit);
									nanoins.setString(1, "015");
									nanoins.setString(2, sent_key);
									nanoins.setString(3, phn);
									nanoins.setString(4, rs.getString("MSGID"));
									nanoins.executeUpdate();
									nanoins.close();
									break;
								case "PHONE":
									nanoit = "insert into cb_nanoit_msg(msg_type, remark4, phn, cb_msg_id) values(?, ?, ?, ?)";
									nanoins = conn.prepareStatement(nanoit);
									nanoins.setString(1, "PHONE");
									nanoins.setString(2, sent_key);
									nanoins.setString(3, phn);
									nanoins.setString(4, rs.getString("MSGID"));
									nanoins.executeUpdate();
									nanoins.close();
									break;			
								case "BKG":
									String bkgstr ="insert into cb_mms_msg(SUBJECT"
											                          + ", PHONE"
											                          + ", CALLBACK"
											                          + ", STATUS"
											                          + ", MSG"
											                          + ", BILL_ID"
											                          + ", TYPE"
											                          + ", ETC1"
											                          + ", ETC2"
											                          + ", ETC4"
											                          + ", REQDATE) "
											                    + "values( ?"
											                          + ", ?"
											                          + ", ?"
											                          + ", ? "
											                          + ", ?"
											                          + ", ?"
											                          + ", ?"
											                          + ", ?"
											                          + ", ?"
											                          + ", ?"
											                          + ", ?)";
									PreparedStatement bkgins = conn.prepareStatement(bkgstr);
									bkgins.setString(1,  rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
									bkgins.setString(2, phn);
									bkgins.setString(3, rs.getString("SMS_SENDER"));
									bkgins.setString(4, "0");
									bkgins.setString(5, msg_sms);
									bkgins.setString(6, rs.getString("SMS_SENDER"));
									bkgins.setString(7, "0");
									bkgins.setString(8, msg_id);
									bkgins.setString(9, sent_key);
									bkgins.setString(10, mem_id);
									if(rs.getString("RESERVE_DT").equals("00000000000000")) {
										bkgins.setString(11, rd.format(reserve_dt));
									}else {
										bkgins.setString(11, rs.getString("RESERVE_DT"));
									}
									bkgins.executeUpdate();
									bkgins.close();
									
									wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
									wtud = conn.prepareStatement(wtudstr);
									wtud.setString(1, sent_key);
									wtud.executeUpdate();
									wtud.close();
									
									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs',CODE='GRS', MESSAGE = '결과 수신대기' where MSGID=?";
									msgud = conn.prepareStatement(msgudstr);
									msgud.setString(1, msg_id);
									msgud.executeUpdate();
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
									amtins.setFloat(3, amount); 
									amtins.setString(4, memo); 
									amtins.setString(5, msg_id); 
									amtins.setFloat(6, payback); 
									amtins.setFloat(7, admin_amt); 
									
									amtins.executeUpdate();
									amtins.close();
									
									break;
								case "SMART":
									
										Date month = new Date();
										SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
										String monthStr = transFormat.format(month);
										
										if(msgtype.equals("SMS")) {
											try {
												String smtquery = "insert into OShotSMS( Sender "          
																						+ ",Receiver "         
																						+ ",Msg "             
																						+ ",URL "             
																						+ ",ReserveDT "
																						+ ",TimeoutDT "       
																						+ ",SendResult "
																						+ ",mst_id "
																						+ ",cb_msg_id )"
																						+ "values( ? "           
																						+ ",? "      
																						+ ",? "      
																						+ ",? "      
																						+ ",? "      
																						+ ",? "      
																						+ ",? "      
																						+ ",? "      
																						+ ",? )"; 
												
												PreparedStatement smtins = conn.prepareStatement(smtquery, Statement.RETURN_GENERATED_KEYS);
												smtins.setString(1, rs.getString("SMS_SENDER"));
												smtins.setString(2, phn);
												smtins.setString(3, msg_sms);
												smtins.setString(4, null);
												
												if(rs.getString("RESERVE_DT").equals("00000000000000")) {
													smtins.setString(5, null);
												}else {
													smtins.setString(5, rs.getString("RESERVE_DT"));
												} 
												
												smtins.setString(6, null);
												smtins.setString(7, "0");
												smtins.setString(8, sent_key);
												smtins.setString(9, rs.getString("MSGID"));
			
												int sms_rows = smtins.executeUpdate();
												
												String sms_msg_id = "";
									            if(sms_rows == 1)
									            {
									                // get candidate id
									            	ResultSet sms_rstemp = null;
									            	sms_rstemp = smtins.getGeneratedKeys();
									                if(sms_rstemp.next())
									                	sms_msg_id = sms_rstemp.getString(1);
									                sms_rstemp.close();
									            }
												smtins.close();
												
												wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
												wtud = conn.prepareStatement(wtudstr);
												wtud.setString(1, sent_key);
												wtud.executeUpdate();
												wtud.close();
												
												msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='sm',CODE='SMT', MESSAGE = '결과 수신대기', SMS_KIND='S', remark3 = ? where MSGID=?";
												msgud = conn.prepareStatement(msgudstr);
												msgud.setString(1, sms_msg_id);
												msgud.setString(2, msg_id);
												msgud.executeUpdate();
												msgud.close();
																								
												kind = "P";
												amount = price.member_price.price_smt_sms;
												payback = price.member_price.price_smt_sms - price.parent_price.price_smt_sms;
												admin_amt = price.base_price.price_smt_sms;
												memo = "웹(C) SMS";
												if(amount == 0 || amount == 0.0f) {
													amount = admin_amt;
												}
				
												amtins = conn.prepareStatement(amtStr);
												amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
												amtins.setString(2, kind); 
												amtins.setFloat(3, amount); 
												amtins.setString(4, memo); 
												amtins.setString(5, msg_id); 
												amtins.setFloat(6, payback); 
												amtins.setFloat(7, admin_amt); 
												
												amtins.executeUpdate();
												amtins.close();
											} catch ( Exception ex ) {
												msgtype = "LMS";
												log.info("TBL REQUEST RESULT  " + div_str + " ( Smart SMS ) 처리 중 오류 : "+ex.toString());
												log.info("MSG TYPE : " + msgtype + "  /  MSG ( " + msg_sms.getBytes().length + " ) : " + msg_sms);
											}
										}
										
										if(msgtype.equals("LMS")) {
											try {
												String smtmmsquery = "insert into OShotMMS" 
																			+ "(MsgGroupID "     
										                                    + ",Sender "          
										                                    + ",Receiver "        
										                                    + ",Subject "         
										                                    + ",Msg "             
										                                    + ",ReserveDT "       
										                                    + ",TimeoutDT "       
										                                    + ",SendResult "      
										                                    + ",File_Path1 "      
										                                    + ",File_Path2 "     
										                                    + ",File_Path3 "      
										                                    + ",mst_id "      
										                                    + ",cb_msg_id )"      
																			+ "values"           
																			+ "(? "     
										                                    + ",? "          
										                                    + ",? "        
										                                    + ",? "         
										                                    + ",? "             
										                                    + ",? "       
										                                    + ",? "       
										                                    + ",? "          
										                                    + ",? "      
										                                    + ",? "      
										                                    + ",? "      
										                                    + ",? "      
										                                    + ",?) ";
												PreparedStatement smtmmsins = conn.prepareStatement(smtmmsquery, Statement.RETURN_GENERATED_KEYS);
												smtmmsins.setString(1, sent_key);
												smtmmsins.setString(2, rs.getString("SMS_SENDER"));
												smtmmsins.setString(3, phn);
												smtmmsins.setString(4, rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
												smtmmsins.setString(5, msg_sms);
												
												if(rs.getString("RESERVE_DT").equals("00000000000000")) {
													smtmmsins.setString(6, null);
												}else {
													smtmmsins.setString(6, rs.getString("RESERVE_DT"));
												} 
												
												smtmmsins.setString(7, null);
												smtmmsins.setString(8, "0");
												String mms1 = null;
												String mms2 = null;
												String mms3 = null;
												
												if(rs.getString("mms_id").length()>5) {
													String mmsinfostr = "select * from cb_mms_images cmi where cmi.mem_id = '" + mem_id + "' and mms_id = '" + rs.getString("mms_id") + "'";
													Statement mmsinfo = conn.createStatement();
													ResultSet mmsrs = mmsinfo.executeQuery(mmsinfostr);
													mmsrs.first();
													
													mms1 = mmsrs.getString("origin1_path");
													mms2 = mmsrs.getString("origin2_path");
													mms3 = mmsrs.getString("origin3_path");
				
													//file_cnt = 1;
												}
												
												smtmmsins.setString(9, mms1);
												smtmmsins.setString(10, mms2);
												smtmmsins.setString(11, mms3);
												smtmmsins.setString(12, sent_key);
												smtmmsins.setString(13, rs.getString("MSGID"));
												
												
												int mms_rows = smtmmsins.executeUpdate();
												
												String mms_msg_id = "";
									            if(mms_rows == 1)
									            {
									                // get candidate id
									            	ResultSet mms_rstemp = null;
									            	mms_rstemp = smtmmsins.getGeneratedKeys();
									                if(mms_rstemp.next())
									                	mms_msg_id = mms_rstemp.getString(1);
									                mms_rstemp.close();
									            }
									            
												smtmmsins.close();
												
												wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
												wtud = conn.prepareStatement(wtudstr);
												wtud.setString(1, sent_key);
												wtud.executeUpdate();
												wtud.close();
												
												msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='sm',CODE='SMT', MESSAGE = '결과 수신대기', SMS_KIND='L', remark3 = ? where MSGID=?";
												msgud = conn.prepareStatement(msgudstr);
												msgud.setString(1, mms_msg_id);
												msgud.setString(2, msg_id);
												msgud.executeUpdate();
												msgud.close();
																
												kind = "P";
			
												if(mms1 != null) {
													amount = price.member_price.price_smt_mms;
													payback = price.member_price.price_smt_mms - price.parent_price.price_smt_mms;
													admin_amt = price.base_price.price_smt_mms;
													memo = "웹(C) MMS";
													if(amount == 0 || amount == 0.0f) {
														amount = admin_amt;
													}
													
												} else {
													amount = price.member_price.price_smt;
													payback = price.member_price.price_smt - price.parent_price.price_smt;
													admin_amt = price.base_price.price_smt;
													memo = "웹(C) LMS";
													if(amount == 0 || amount == 0.0f) {
														amount = admin_amt;
													}
												}
				
												amtins = conn.prepareStatement(amtStr);
												amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
												amtins.setString(2, kind); 
												amtins.setFloat(3, amount); 
												amtins.setString(4, memo); 
												amtins.setString(5, msg_id); 
												amtins.setFloat(6, payback); 
												amtins.setFloat(7, admin_amt); 
												
												amtins.executeUpdate();
												amtins.close();	
											} catch(Exception ex) {
												log.info("TBL REQUEST RESULT " + div_str + " ( Smart LMS ) 처리 중 오류 : "+ex.toString());
												log.info("MSG TYPE : " + msgtype + "  /  MSG ( " + msg_sms.getBytes().length + " ) : " + msg_sms);
											}											
										}

									break;								
								case "GREEN_SHOT":
									if(msgtype.equals("SMS")) {
										String funsmsstr = "insert into cb_sms_msg(TR_PHONE"
												                               + ",TR_CALLBACK"
												                               + ",TR_ORG_CALLBACK"
												                               + ",TR_SENDSTAT"
												                               + ",TR_MSG"
												                               + ",TR_MSGTYPE"
												                               + ",TR_ETC1"
												                               + ",TR_ETC2"
												                               + ",TR_ETC4"
												                               + ",TR_SENDDATE)"
												                               + "values("
												                               + " ?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?"
												                               + ",?)";
										PreparedStatement funsmsins = conn.prepareStatement(funsmsstr);
										funsmsins.setString(1, phn);
										funsmsins.setString(2, rs.getString("SMS_SENDER"));
										funsmsins.setString(3, rs.getString("SMS_SENDER"));
										funsmsins.setString(4, "0");
										funsmsins.setString(5, msg_sms);
										funsmsins.setString(6, "0");
										funsmsins.setString(7, msg_id);
										funsmsins.setString(8, sent_key);
										funsmsins.setString(9, mem_id);
										if(rs.getString("RESERVE_DT").equals("00000000000000")) {
											funsmsins.setString(10, rd.format(reserve_dt));
										}else {
											funsmsins.setString(10, rs.getString("RESERVE_DT"));
										} 
										funsmsins.executeUpdate();
										funsmsins.close();
										
										wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
										wtud = conn.prepareStatement(wtudstr);
										wtud.setString(1, sent_key);
										wtud.executeUpdate();
										wtud.close();
										
										msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='gs',CODE='GRS', MESSAGE = '결과 수신대기', SMS_KIND='S' where MSGID=?";
										msgud = conn.prepareStatement(msgudstr);
										msgud.setString(1, msg_id);
										msgud.executeUpdate();
										msgud.close();
																						
										kind = "P";
										amount = price.member_price.price_grs_sms;
										payback = price.member_price.price_grs_sms - price.parent_price.price_grs_sms;
										admin_amt = price.base_price.price_grs_sms;
										memo = "웹(A) SMS";
										if(amount == 0 || amount == 0.0f) {
											amount = admin_amt;
										}
		
										amtins = conn.prepareStatement(amtStr);
										amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
										amtins.setString(2, kind); 
										amtins.setFloat(3, amount); 
										amtins.setString(4, memo); 
										amtins.setString(5, msg_id); 
										amtins.setFloat(6, payback); 
										amtins.setFloat(7, admin_amt); 
										
										amtins.executeUpdate();
										amtins.close();
										
									}else if(msgtype.equals("LMS")) {
										nanoit = "insert into cb_nanoit_msg(msg_type, remark4, phn, cb_msg_id) values(?, ?, ?, ?)";
										nanoins = conn.prepareStatement(nanoit);
										nanoins.setString(1, "GRS");
										nanoins.setString(2, sent_key);
										nanoins.setString(3, phn);
										nanoins.setString(4, rs.getString("MSGID"));
										nanoins.executeUpdate();
										nanoins.close();
										break;		
									}
										
									break;
								case "IMC":
	//                              동보 전송 필요시 								
	//								String imc;
	//								PreparedStatement imcins;
	//								imc = "insert into cb_imc_msg(msg_type, remark4, phn, cb_msg_id) values(?, ?, ?, ?)";
	//								imcins = conn.prepareStatement(imc);
	//								imcins.setString(1, "IMC");
	//								imcins.setString(2, sent_key);
	//								imcins.setString(3, phn);
	//								imcins.setString(4, rs.getString("MSGID"));
	//								imcins.executeUpdate();
	//								imcins.close();
	//								break;
									
									String imc_mms1="", imc_mms2="", imc_mms3="";
									String msg_type = "IMC";
									msgtype = "PMS";
									if(rs.getString("mms_id").length()>5) {
										String mmsinfostr = "select * from cb_mms_images cmi where cmi.mem_id = '" + mem_id + "' and mms_id = '" + rs.getString("mms_id") + "'";
										Statement mmsinfo = conn.createStatement();
										ResultSet mmsrs = mmsinfo.executeQuery(mmsinfostr);
										mmsrs.first();
										
										imc_mms1 = mmsrs.getString("origin1_path");
										imc_mms2 = mmsrs.getString("origin2_path");
										imc_mms3 = mmsrs.getString("origin3_path");
										
										msgtype = "MMS";
									}
									
									String req_query = "insert into IMC.imc_send_idx(req) values(0)";
									PreparedStatement req_id_st = conn.prepareStatement(req_query, Statement.RETURN_GENERATED_KEYS);
									int req_id_ins = req_id_st.executeUpdate();
									int req_id = 0;
						            if(req_id_ins == 1)
						            {
						                // get candidate id
						            	ResultSet imc_req = null;
						            	imc_req = req_id_st.getGeneratedKeys();
						                if(imc_req.next())
						                	req_id = imc_req.getInt(1);
						                imc_req.close();
						            }
						            req_id_st.close();
						            
									String imcstr = "insert into IMC.IMC_SEND(user_id"
																	  + ",sub_id"
																	  + ",send_type"
																	  + ",sender"
																	  + ",subject"
																	  + ",message"
																	  + ",file_url"
																	  + ",receivers"
																	  + ",reserve_yn"
																	  + ",reserve_dt"
																	  + ",request_id"
																	  + ",request_dt"
																	  + ",send_status)"
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
																	  + ",?"
																	  + ",?"
																	  + ",?)";
									PreparedStatement imcins = conn.prepareStatement(imcstr, Statement.RETURN_GENERATED_KEYS);
									imcins.setString(1,  "dhn7985" ); //rs.getString("user_id"));
									imcins.setString(2, null); //  폰문자 일때는 sub_id 는 Null 처리 함.   rs.getString("mem_id"));
									imcins.setString(3, msgtype);
									imcins.setString(4, rs.getString("SMS_SENDER") );
									if(length(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""))>36) {
										imcins.setString(5, substring(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""), 36));
									}else {
										imcins.setString(5, rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
									}
									imcins.setString(6, msg_sms );
									imcins.setString(7, "");
									imcins.setString(8, phn);
									if(rs.getString("RESERVE_DT").equals("00000000000000")) {
										imcins.setString(9, "N");
										imcins.setString(10, null);
									} else {
										imcins.setString(9, "Y");
										imcins.setString(10, rd.format(rs.getString("RESERVE_DT")));
									}
									imcins.setLong(11,  req_id );
									imcins.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
									imcins.setString(13, "READY");
									
									int rowgrs = imcins.executeUpdate();
									int imc_msg_id = 0;
						            if(rowgrs == 1)
						            {
						                // get candidate id
						            	ResultSet imc_rstemp = null;
						            	imc_rstemp = imcins.getGeneratedKeys();
						                if(imc_rstemp.next())
						                	imc_msg_id = imc_rstemp.getInt(1);
						                imc_rstemp.close();
						            }
									
						            imcins.close();
						            
						            //Thread.sleep(5);
	
						            String imcsub = "insert into IMC.IMC_MART_SUB" 
												            		+"(request_id" 
												            		+",user_id"    									
												            		+",sub_id"
												            		+ ",name)"
												            		+"values"
																	+"(?" 
																	+",?"    									
																	+",?"
																	+",?)";
								    
								    PreparedStatement imcmsins = conn.prepareStatement(imcsub );
								    imcmsins.setInt(1, imc_msg_id);
								    imcmsins.setString(2, userid );//rs.getString("user_id"));
								    imcmsins.setString(3, sent_key);
								    imcmsins.setString(4, msg_id);
								    imcmsins.executeUpdate();
								    imcmsins.close();
						            
									wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0) + 1 where mst_id=?";
									wtud = conn.prepareStatement(wtudstr);
									wtud.setString(1, sent_key);
									wtud.executeUpdate();
									wtud.close();
									
									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='im',CODE='IMC', MESSAGE = '결과 수신대기', remark3 = '" + imc_msg_id +"' where MSGID=?";
									msgud = conn.prepareStatement(msgudstr);
									msgud.setString(1, msg_id);
									msgud.executeUpdate();
									msgud.close();
														
									kind = "P";
									
									if( ( imc_mms1 == null || imc_mms1.isEmpty())  && ( imc_mms2 == null || imc_mms2.isEmpty()) && ( imc_mms3 == null || imc_mms3.isEmpty())) {
										amount = price.member_price.price_imc;
										payback = price.member_price.price_imc - price.parent_price.price_imc;
										admin_amt = price.base_price.price_imc;
										memo = "IMC";
									} else {
										amount = price.member_price.price_imc;
										payback = price.member_price.price_imc - price.parent_price.price_imc;
										admin_amt = price.base_price.price_imc;
										memo = "IMC MMS";
									}
									
									if(amount == 0 || amount == 0.0f) {
										amount = admin_amt;
									}
				
									amtins = conn.prepareStatement(amtStr);
									amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
									amtins.setString(2, kind); 
									amtins.setFloat(3, amount ); 
									amtins.setString(4, memo); 
									amtins.setString(5, msg_id); 
									amtins.setFloat(6, payback ); 
									amtins.setFloat(7, admin_amt ); 
									
									amtins.executeUpdate();
									amtins.close();
									
									break;
								case "SMT_PHN":
	//                              동보 전송 필요시 								
	//								String imc;
	//								PreparedStatement imcins;
	//								imc = "insert into cb_imc_msg(msg_type, remark4, phn, cb_msg_id) values(?, ?, ?, ?)";
	//								imcins = conn.prepareStatement(imc);
	//								imcins.setString(1, "IMC");
	//								imcins.setString(2, sent_key);
	//								imcins.setString(3, phn);
	//								imcins.setString(4, rs.getString("MSGID"));
	//								imcins.executeUpdate();
	//								imcins.close();
	//								break;
									
	//								String smt_req_query = "insert into IMC.imc_send_idx(req) values(0)";
	//								PreparedStatement smt_req_id_st = conn.prepareStatement(smt_req_query, Statement.RETURN_GENERATED_KEYS);
	//								int smt_req_id_ins = smt_req_id_st.executeUpdate();
	//								int smt_req_id = 0;
	//					            if(smt_req_id_ins == 1)
	//					            {
	//					                // get candidate id
	//					            	ResultSet smt_req = null;
	//					            	smt_req = smt_req_id_st.getGeneratedKeys();
	//					                if(smt_req.next())
	//					                	req_id = smt_req.getInt(1);
	//					                smt_req.close();
	//					            }
	//					            smt_req_id_st.close();
						            
									String smtstr = "insert into SMT_SEND(user_id"
																	  + ",sub_id"
																	  + ",send_type"
																	  + ",sender"
																	  + ",subject"
																	  + ",message"
																	  + ",file_url"
																	  + ",receivers"
																	  + ",reserve_yn"
																	  + ",reserve_dt"
																	  + ",request_id"
																	  + ",request_dt"
																	  + ",send_status)"
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
																	  + ",?"
																	  + ",?"
																	  + ",?)";
									PreparedStatement smtins = conn.prepareStatement(smtstr, Statement.RETURN_GENERATED_KEYS);
									smtins.setString(1,  "dhn7985" ); //rs.getString("user_id"));
									smtins.setString(2, null); //  폰문자 일때는 sub_id 는 Null 처리 함.   rs.getString("mem_id"));
									smtins.setString(3, msgtype);
									smtins.setString(4, rs.getString("SMS_SENDER") );
									if(length(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""))>30) {
										smtins.setString(5, substring(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""), 30));
									}else {
										smtins.setString(5, rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
									}
									smtins.setString(6, msg_sms );
									smtins.setString(7, "");
									smtins.setString(8, phn);
									//if(rs.getString("RESERVE_DT").equals("00000000000000")) {
									smtins.setString(9, "N");
									smtins.setString(10, null);
									//} else {
									//	smtins.setString(9, "Y");
									//	smtins.setString(10, rd.format(rs.getString("RESERVE_DT")));
									//}
									smtins.setString(11,  sent_key );
									smtins.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
									smtins.setString(13, "READY");
									
									smtins.executeUpdate();
						            smtins.close();
						            
									wtudstr = "update cb_wt_msg_sent set mst_imc=ifnull(mst_imc,0) + 1 where mst_id=?";
									wtud = conn.prepareStatement(wtudstr);
									wtud.setString(1, sent_key);
									wtud.executeUpdate();
									wtud.close();
									
									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='sm',CODE='SMT', MESSAGE = '폰 성공', RESULT='Y' where MSGID=?";
									msgud = conn.prepareStatement(msgudstr);
									msgud.setString(1, msg_id);
									msgud.executeUpdate();
									msgud.close();
														
									kind = "P";
									
									amount = price.member_price.price_imc;
									payback = price.member_price.price_imc - price.parent_price.price_imc;
									admin_amt = price.base_price.price_imc;
									memo = "SMT PHN";
									
									if(amount == 0 || amount == 0.0f) {
										amount = admin_amt;
									}
				
									amtins = conn.prepareStatement(amtStr);
									amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
									amtins.setString(2, kind); 
									amtins.setFloat(3, amount ); 
									amtins.setString(4, memo); 
									amtins.setString(5, msg_id); 
									amtins.setFloat(6, payback ); 
									amtins.setFloat(7, admin_amt ); 
									
									amtins.executeUpdate();
									amtins.close();
									
									break;	 									
								case "SMT_PHN_DB":
									String smtdbstr = "insert into SMT_SEND(user_id"
																	  + ",sub_id"
																	  + ",send_type"
																	  + ",sender"
																	  + ",subject"
																	  + ",message"
																	  + ",file_url"
																	  + ",receivers"
																	  + ",reserve_yn"
																	  + ",reserve_dt"
																	  + ",request_id"
																	  + ",request_dt"
																	  + ",send_status)"
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
																	  + ",?"
																	  + ",?"
																	  + ",?)";
									PreparedStatement smtdbins = conn.prepareStatement(smtdbstr, Statement.RETURN_GENERATED_KEYS);
									smtdbins.setString(1,  "dhn7985" ); //rs.getString("user_id"));
									smtdbins.setString(2, null); //  폰문자 일때는 sub_id 는 Null 처리 함.   rs.getString("mem_id"));
									smtdbins.setString(3, msgtype);
									smtdbins.setString(4, rs.getString("SMS_SENDER") );
									if(length(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""))>30) {
										smtdbins.setString(5, substring(rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""), 30));
									}else {
										smtdbins.setString(5, rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
									}
									smtdbins.setString(6, msg_sms );
									smtdbins.setString(7, "");
									smtdbins.setString(8, phn);
									//if(rs.getString("RESERVE_DT").equals("00000000000000")) {
									smtdbins.setString(9, "N");
									smtdbins.setString(10, null);
									//} else {
									//	smtins.setString(9, "Y");
									//	smtins.setString(10, rd.format(rs.getString("RESERVE_DT")));
									//}
									smtdbins.setString(11,  sent_key );
									smtdbins.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
									smtdbins.setString(13, "SMTDB");
									
									smtdbins.executeUpdate();
									smtdbins.close();
						            
									wtudstr = "update cb_wt_msg_sent set mst_imc=ifnull(mst_imc,0) + 1 where mst_id=?";
									wtud = conn.prepareStatement(wtudstr);
									wtud.setString(1, sent_key);
									wtud.executeUpdate();
									wtud.close();
									
									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='sm',CODE='SMT', MESSAGE = '폰 성공', RESULT='Y' where MSGID=?";
									msgud = conn.prepareStatement(msgudstr);
									msgud.setString(1, msg_id);
									msgud.executeUpdate();
									msgud.close();
														
									kind = "P";
									
									amount = price.member_price.price_imc;
									payback = price.member_price.price_imc - price.parent_price.price_imc;
									admin_amt = price.base_price.price_imc;
									memo = "SMT PHN";
									
									if(amount == 0 || amount == 0.0f) {
										amount = admin_amt;
									}
				
									amtins = conn.prepareStatement(amtStr);
									amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
									amtins.setString(2, kind); 
									amtins.setFloat(3, amount ); 
									amtins.setString(4, memo); 
									amtins.setString(5, msg_id); 
									amtins.setFloat(6, payback ); 
									amtins.setFloat(7, admin_amt ); 
									
									amtins.executeUpdate();
									amtins.close();
									
									break;	 									
								case "NASELF":
									//if(nconn == null) {
									//	nconn = DriverManager.getConnection(NURL, NUSER_NAME, NPASSWORD);
									//}
									
									// 나셀프 수신거부 List 조회 후 수신거부 처리
									//Statement nblock = nconn.createStatement();
									//String nblockstr = "select count(1) as cnt from sdk_block_hp where hp = '" + phn + "'";
									//ResultSet nrs = nblock.executeQuery(nblockstr);
									//nrs.first();
	//								if(nrs.getInt("cnt") > 0) {
	//									wtudstr = "update cb_wt_msg_sent set mst_err_nas=ifnull(mst_err_nas,0)+1 where mst_id=?";
	//									wtud = conn.prepareStatement(wtudstr);
	//									wtud.setString(1, sent_key);
	//									wtud.executeUpdate();
	//									wtud.close();
	//									
	//									msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ns',CODE='NAS', MESSAGE = '수신거부', RESULT = 'N' where MSGID=?";
	//									msgud = conn.prepareStatement(msgudstr);
	//									msgud.setString(1, msg_id);
	//									msgud.executeUpdate();
	//									msgud.close();
	//								} else {
										if(msgtype.equals("SMS")) {
											String nassmsstr = "insert into cb_nas_sms_msg(TR_PHONE"
													                               + ",TR_CALLBACK"
													                               + ",TR_ORG_CALLBACK"
													                               + ",TR_SENDSTAT"
													                               + ",TR_MSG"
													                               + ",TR_MSGTYPE"
													                               + ",TR_ETC1"
													                               + ",TR_ETC2"
													                               + ",TR_ETC4"
													                               + ",TR_SENDDATE)"
													                               + "values("
													                               + " ?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?"
													                               + ",?)";
											PreparedStatement nassmsins = conn.prepareStatement(nassmsstr);
											nassmsins.setString(1, phn);
											nassmsins.setString(2, rs.getString("SMS_SENDER"));
											nassmsins.setString(3, rs.getString("SMS_SENDER"));
											nassmsins.setString(4, "0");
											nassmsins.setString(5, msg_sms);
											nassmsins.setString(6, "0");
											nassmsins.setString(7, msg_id);
											nassmsins.setString(8, sent_key);
											nassmsins.setString(9, mem_id);
											if(rs.getString("RESERVE_DT").equals("00000000000000")) {
												nassmsins.setString(10, rd.format(reserve_dt));
											}else {
												nassmsins.setString(10, rs.getString("RESERVE_DT"));
											} 
											nassmsins.executeUpdate();
											nassmsins.close();
											
											wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
											wtud = conn.prepareStatement(wtudstr);
											wtud.setString(1, sent_key);
											wtud.executeUpdate();
											wtud.close();
											
											msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ns',CODE='NAS', MESSAGE = '결과 수신대기', SMS_KIND='S' where MSGID=?";
											msgud = conn.prepareStatement(msgudstr);
											msgud.setString(1, msg_id);
											msgud.executeUpdate();
											msgud.close();
																							
											kind = "P";
											amount = price.member_price.price_nas_sms;
											payback = price.member_price.price_nas_sms - price.parent_price.price_nas_sms;
											admin_amt = price.base_price.price_nas_sms;
											memo = "웹(B) SMS";
											if(amount == 0 || amount == 0.0f) {
												amount = admin_amt;
											}
									
											amtins = conn.prepareStatement(amtStr);
											amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
											amtins.setString(2, kind); 
											amtins.setFloat(3, amount); 
											amtins.setString(4, memo); 
											amtins.setString(5, msg_id); 
											amtins.setFloat(6, payback); 
											amtins.setFloat(7, admin_amt); 
											
											amtins.executeUpdate();
											amtins.close();
										}else if(msgtype.equals("LMS")) {
											
											String mms1="", mms2="", mms3="";
											int file_cnt = 0;
											
											if(rs.getString("mms_id").length()>5) {
												String mmsinfostr = "select * from cb_mms_images cmi where cmi.mem_id = '" + mem_id + "' and mms_id = '" + rs.getString("mms_id") + "'";
												Statement mmsinfo = conn.createStatement();
												ResultSet mmsrs = mmsinfo.executeQuery(mmsinfostr);
												mmsrs.first();
												
												mms1 = mmsrs.getString("origin1_path");
												mms2 = mmsrs.getString("origin2_path");
												mms3 = mmsrs.getString("origin3_path");
												
												if(!StringUtils.isBlank(mms1))
													file_cnt++;
												if(!StringUtils.isBlank(mms2))
													file_cnt++;
												if(!StringUtils.isBlank(mms3))
													file_cnt++;
												
												//file_cnt = 1;
											}
																					
											String nasstr ="insert into cb_nas_mms_msg(SUBJECT"
													                          + ", PHONE"
													                          + ", CALLBACK"
													                          + ", STATUS"
													                          + ", MSG"
													                          + ", BILL_ID"
													                          + ", TYPE"
													                          + ", FILE_PATH1"
													                          + ", FILE_PATH2"
													                          + ", FILE_PATH3"
													                          + ", ETC1"
													                          + ", ETC2"
													                          + ", ETC4"
													                          + ", REQDATE "
													                          + ", FILE_CNT) "
													                    + "values( ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ? "
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?"
													                          + ", ?)";
											PreparedStatement nasins = conn.prepareStatement(nasstr);
											nasins.setString(1,  rs.getString("SMS_LMS_TIT").replaceAll("\\r\\n|\\r|\\n", ""));
											nasins.setString(2, phn);
											nasins.setString(3, rs.getString("SMS_SENDER"));
											nasins.setString(4, "0");
											nasins.setString(5, msg_sms);
											nasins.setString(6, rs.getString("SMS_SENDER"));
											nasins.setString(7, "0");
											nasins.setString(8, mms1);
											nasins.setString(9, mms2);
											nasins.setString(10, mms3);
											nasins.setString(11, msg_id);
											nasins.setString(12, sent_key);
											nasins.setString(13, mem_id);
											if(rs.getString("RESERVE_DT").equals("00000000000000")) {
												nasins.setString(14, rd.format(reserve_dt));
											}else {
												nasins.setString(14, rs.getString("RESERVE_DT"));
											}
											nasins.setInt( 15, file_cnt  );
											nasins.executeUpdate();
											nasins.close();
											
											wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+1 where mst_id=?";
											wtud = conn.prepareStatement(wtudstr);
											wtud.setString(1, sent_key);
											wtud.executeUpdate();
											wtud.close();
											
											msgudstr = "update cb_msg_" + userid + " set MESSAGE_TYPE='ns',CODE='NAS', MESSAGE = '결과 수신대기' where MSGID=?";
											msgud = conn.prepareStatement(msgudstr);
											msgud.setString(1, msg_id);
											msgud.executeUpdate();
											msgud.close();
																
											kind = "P";
											
											if( ( mms1 == null || mms1.isEmpty())  && ( mms2 == null || mms2.isEmpty()) && ( mms3 == null || mms3.isEmpty())) {
												amount = price.member_price.price_nas;
												payback = price.member_price.price_nas - price.parent_price.price_nas;
												admin_amt = price.base_price.price_nas;
												memo = "웹(B)";
											} else {
												amount = price.member_price.price_nas_mms;
												payback = price.member_price.price_nas_mms - price.parent_price.price_nas_mms;
												admin_amt = price.base_price.price_nas_mms;
												memo = "웹(B) MMS";
											}
											if(amount == 0 || amount == 0.0f) {
												amount = admin_amt;
											}
								
											amtins = conn.prepareStatement(amtStr);
											amtins.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis())); 
											amtins.setString(2, kind); 
											amtins.setFloat(3, amount); 
											amtins.setString(4, memo); 
											amtins.setString(5, msg_id); 
											amtins.setFloat(6, payback); 
											amtins.setFloat(7, admin_amt); 
											
											amtins.executeUpdate();
											amtins.close();
										}
										 
									//}
		
									
									
								}
							}
						} else {
							// 2차 발신이 없는 경우 카카오 실패 건시 처리
							wtudstr = "";
							
							if(rs.getString("MESSAGE_TYPE") != null && rs.getString("MESSAGE_TYPE").equals("ft")) {
								if(rs.getString("IMAGE_URL") == null || rs.getString("IMAGE_URL").isEmpty() ) {
									wtudstr = "update cb_wt_msg_sent set mst_err_ft = ifnull(mst_err_ft,0)+1 where mst_id=?";
								} else {
									wtudstr = "update cb_wt_msg_sent set mst_err_ft_img = ifnull(mst_err_ft_img,0)+1 where mst_id=?";
								}
							} else if(rs.getString("MESSAGE_TYPE") != null && rs.getString("MESSAGE_TYPE").equals("at")) {
								wtudstr = "update cb_wt_msg_sent set mst_err_at=ifnull(mst_err_at,0)+1 where mst_id=?";
							}
							
							if(!wtudstr.isEmpty()) {
								wtud = conn.prepareStatement(wtudstr);
								wtud.setString(1, sent_key);
								wtud.executeUpdate();
								wtud.close();
							}
						}
						
					}
				}
				String trrdelstr = "delete from TBL_REQUEST_RESULT where MSGID= ?";
				PreparedStatement trrdel = conn.prepareStatement(trrdelstr);
				trrdel.setString(1, msg_id);
				trrdel.executeUpdate();
				trrdel.close();
				//break;
			}
			if(msgcnt > 0 ) {
				log.info("TBL REQUEST RESULT " + div_str + "  처리 : "+ msgcnt + " 건");
			}
			rs.close();
			
		} catch (Exception ex) {
			log.info("TBL REQUEST RESULT " + div_str + " 처리 중 오류 : "+ex.toString());
		}
		
		try {
			if(nconn!=null) {
				nconn.close();
			}
		} catch(Exception e) {}
		
		try {
			if(tbl_result!=null) {
				tbl_result.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
			
//		try {
//			//Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		TBLReqProcess.isRunning[div_str] = false;
		//log.info("TBL RESULT PROC 끝 : " + div_str);
	}

    public static String substring(String parameterName, int maxLength) {
        int DB_FIELD_LENGTH = maxLength;
 
        Charset utf8Charset = Charset.forName("UTF-8");
        CharsetDecoder cd = utf8Charset.newDecoder();
 
        try {
            byte[] sba = parameterName.getBytes("UTF-8");
            // Ensure truncating by having byte buffer = DB_FIELD_LENGTH
            ByteBuffer bb = ByteBuffer.wrap(sba, 0, DB_FIELD_LENGTH); // len in [B]
            CharBuffer cb = CharBuffer.allocate(DB_FIELD_LENGTH); // len in [char] <= # [B]
            // Ignore an incomplete character
            cd.onMalformedInput(CodingErrorAction.IGNORE);
            cd.decode(bb, cb, true);
            cd.flush(cb);
            parameterName = new String(cb.array(), 0, cb.position());
        } catch (UnsupportedEncodingException e) {
            System.err.println("### 지원하지 않는 인코딩입니다." + e);
        }
 
        return parameterName;
    }
 
    // 문자열 인코딩에 따라서 글자수 체크
    public static int length(CharSequence sequence) {
        int count = 0;
        for (int i = 0, len = sequence.length(); i < len; i++) {
            char ch = sequence.charAt(i);
 
            if (ch <= 0x7F) {
                count++;
            } else if (ch <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }
        return count;
    }
	 
}
