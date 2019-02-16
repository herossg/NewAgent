import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;

public class Nano_it_summary implements Runnable {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private String DB_URL;
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	public static boolean isRunning = false;
	public Logger log;
	
	public Nano_it_summary(String _db_url, Logger _log) {
		DB_URL = _db_url;
		log = _log;
	}
	
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
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();

			nano_msg = conn.createStatement();
			String nanomsg_str = "select SQL_NO_CACHE cnm.remark4" + 
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
									"     ,wms.mst_mms_content" + 
									"     ,group_concat(cnm.cb_msg_id) as cb_msg_id" + 
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
					price = new Price_info(DB_URL, Integer.valueOf(mem_id));
					pre_mem_id = mem_id;
				}
				
				String nanoDelstr = "delete from cb_nanoit_msg where sn in (" + rs.getString("sn") + ")";
				PreparedStatement nanodel = conn.prepareStatement(nanoDelstr);
				nanodel.executeUpdate();
				nanodel.close();
				
				/*
				 * 나노 동보전송에 대한 개별 전화 번호 저장
				 */
				String nano_bc_ins = "insert into cb_nano_broadcast_list(msg_id, "
						                                              + "type, "
						                                              + "rcv_phone, "
						                                              + "mst_id, "
						                                              + "mem_id,"
						                                              + "max_sn,"
						                                              + "FILE_PATH1,"
						                                              + "FILE_PATH2,"
						                                              + "FILE_PATH3,"
						                                              + "cb_msg_id)"
						                                       + "values(?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?,"
						                                              + "?)";
				
				int msg_id = 0;
				String msg_type = "";
				String mms1="", mms2="", mms3="";
				
				switch(mem_resend) {
				case "015":
					msg_type = "015";
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
					PreparedStatement _015ins = conn.prepareStatement(_015, Statement.RETURN_GENERATED_KEYS);
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
					//_015ins.executeUpdate();
					
					int rowAffected = _015ins.executeUpdate();
		            if(rowAffected == 1)
		            {
		                // get candidate id
		            	ResultSet rstemp = null;
		            	rstemp = _015ins.getGeneratedKeys();
		                if(rstemp.next())
		                	msg_id = rstemp.getInt(1);
		            }

		            _015ins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+" + ins_cnt + " where mst_id=?";
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
					msg_type = "PHN";
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
					PreparedStatement Phoneins = conn.prepareStatement(phonestr, Statement.RETURN_GENERATED_KEYS);
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
					//Phoneins.executeUpdate();
					
					int rowa = Phoneins.executeUpdate();
		            if(rowa == 1)
		            {
		                // get candidate id
		            	ResultSet rstemp = null;
		            	rstemp = Phoneins.getGeneratedKeys();
		                if(rstemp.next())
		                	msg_id = rstemp.getInt(1);
		            }
					Phoneins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+" + ins_cnt + " where mst_id=?";
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
					msg_type = "GRS";
					String msgtype = "LMS";
					String mms_content = rs.getString("mst_mms_content");
					if(mms_content != null && mms_content.length()>5) {
						String mmsinfostr = "select * from cb_mms_images cmi where cmi.mem_id = '" + mem_id + "' and mms_id = '" + mms_content + "'";
						Statement mmsinfo = conn.createStatement();
						ResultSet mmsrs = mmsinfo.executeQuery(mmsinfostr);
						mmsrs.first();
						
						mms1 = mmsrs.getString("origin1_path");
						mms2 = mmsrs.getString("origin2_path");
						mms3 = mmsrs.getString("origin3_path");
						
						msgtype = "MMS";
					}
					
					String grsstr = "insert into cb_grs_msg(msg_gb"
													  + ",msg_st"
													  + ",msg_snd_phn"
													  + ",msg_rcv_phn"
													  + ",subject"
													  + ",text"
													  + ",cb_msg_id"
													  + ",file_path1"
													  + ",file_path2"
													  + ",file_path3"
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
													  + ",?"
													  + ",?"
													  + ",?"
													  + ",?)";
					PreparedStatement grsins = conn.prepareStatement(grsstr, Statement.RETURN_GENERATED_KEYS);
					grsins.setString(1, msgtype);
					grsins.setString(2, "0");
					grsins.setString(3, rs.getString("SMS_SENDER") );
					grsins.setString(4, rs.getString("PHN"));
					if(length(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""))>36) {
						grsins.setString(5, substring(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""), 36));
					}else {
						grsins.setString(5, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""));
					}
					grsins.setString(6, rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " ") );
					grsins.setString(7, mem_id);
					grsins.setString(8, mms1);
					grsins.setString(9, mms2);
					grsins.setString(10, mms3);
					grsins.setString(11, sent_key);
					grsins.setString(12, rs.getString("max_sn"));
					grsins.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis())); 
					grsins.setTimestamp(14, new java.sql.Timestamp(System.currentTimeMillis())); 
					//grsins.executeUpdate();
					
					int rowgrs = grsins.executeUpdate();
		            if(rowgrs == 1)
		            {
		                // get candidate id
		            	ResultSet rstemp = null;
		            	rstemp = grsins.getGeneratedKeys();
		                if(rstemp.next())
		                	msg_id = rstemp.getInt(1);
		            }
					
		            grsins.close();
					
					wtudstr = "update cb_wt_msg_sent set mst_wait=ifnull(mst_wait,0)+" + ins_cnt + " where mst_id=?";
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
					
					if( ( mms1 == null || mms1.isEmpty())  && ( mms2 == null || mms2.isEmpty()) && ( mms3 == null || mms3.isEmpty())) {
						amount = price.member_price.price_grs;
						payback = price.member_price.price_grs - price.parent_price.price_grs;
						admin_amt = price.base_price.price_grs;
						memo = "웹(A)";
					} else {
						amount = price.member_price.price_grs_mms;
						payback = price.member_price.price_grs_mms - price.parent_price.price_grs_mms;
						admin_amt = price.base_price.price_grs_mms;
						memo = "웹(A) MMS";
					}
					
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
				
				String[] rcv_phn = rs.getString("PHN").split(",");
				String[] cb_msg_id = rs.getString("cb_msg_id").split(",");
				int idx=0;
				for(String rp : rcv_phn) {
					
					PreparedStatement nanomsgbc = conn.prepareStatement(nano_bc_ins);
					nanomsgbc.setInt(1, msg_id);
					nanomsgbc.setString(2, msg_type);
					nanomsgbc.setString(3, rp);
					nanomsgbc.setString(4, rs.getString("remark4"));
					nanomsgbc.setString(5, rs.getString("mem_id"));
					nanomsgbc.setString(6, rs.getString("max_sn"));
					nanomsgbc.setString(7, mms1);
					nanomsgbc.setString(8, mms2);
					nanomsgbc.setString(9, mms3);
					nanomsgbc.setString(10, cb_msg_id[idx]);
					nanomsgbc.executeUpdate();
					nanomsgbc.close();
					idx++;
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
