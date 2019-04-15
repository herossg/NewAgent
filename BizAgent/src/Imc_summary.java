import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;

public class Imc_summary implements Runnable {
	public static boolean isRunning = false;
	public Logger log;
	
	public Imc_summary(String _db_url, Logger _log) {
		log = _log;
	}
	
	public void run() {
		if(!Imc_summary.isRunning) {
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
		Imc_summary.isRunning = true;	
		//log.info("Nano it summary 실행");  수정 테스트...
		
		Connection conn = null;
		Connection nconn = null;
		Statement imc_msg = null;
		SimpleDateFormat rd = new SimpleDateFormat("yyyyMMddHHmmss");
		
		try {
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();

			imc_msg = conn.createStatement();
			String nanomsg_str = "select SQL_NO_CACHE cnm.remark4" + 
									"     ,group_concat(cnm.phn) as PHN" + 
									"     ,group_concat(concat('''','82', right(phn, length(phn)-1)), '''') as msg_phn" + 
									"     ,group_concat(cnm.sn) as sn" + 
									"     ,wms.mst_lms_content as MSG_SMS" + 
									"     ,wms.mst_sms_callback as SMS_SENDER" + 
									"     ,cnm.msg_type" + 
									"     ,cm.mem_userid as user_id" + 
									"     ,cm.mem_id" + 
									"     ,cm.mem_username" + 
									"     ,cm.mem_level" + 
									"     ,wms.mst_reserved_dt as RESERVE_DT" + 
									"     ,max(sn) as max_sn" + 
									"     ,wms.mst_mms_content" + 
									"     ,group_concat(cnm.cb_msg_id) as cb_msg_id" + 
									" from cb_imc_msg cnm " + 
									"inner join cb_wt_msg_sent wms" + 
									"   on cnm.remark4 = wms.mst_id " + 
									"inner join cb_member cm" + 
									"   on wms.mst_mem_id = cm.mem_id " + 
									"group by cnm.remark4" + 
									"        ,wms.mst_lms_content" + 
									"     ,wms.mst_sms_callback" + 
									"     ,cnm.msg_type " + 
									"order by remark4" + 
									"        ,sn";
			ResultSet rs = imc_msg.executeQuery(nanomsg_str);
			
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
					price = new Price_info("", Integer.valueOf(mem_id));
					pre_mem_id = mem_id;
				}
				
				String imcDelstr = "delete from cb_imc_msg where sn in (" + rs.getString("sn") + ")";
				PreparedStatement imcdel = conn.prepareStatement(imcDelstr);
				imcdel.executeUpdate();
				imcdel.close();
				
				/*
				 * 나노 동보전송에 대한 개별 전화 번호 저장
				 */
				String imc_bc_ins = "insert into cb_imc_broadcast_list(msg_id, "
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
					case "IMC":
						msg_type = "IMC";
						String msgtype = "PMS";
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
						if(length(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""))>36) {
							imcins.setString(5, substring(rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""), 36));
						}else {
							imcins.setString(5, rs.getString("MSG_SMS").replaceAll("\\r\\n|\\r|\\n", ""));
						}
						imcins.setString(6, rs.getString("MSG_SMS").replaceAll("\\xC2\\xA0", " ") );
						imcins.setString(7, "");
						imcins.setString(8, rs.getString("PHN"));
						if(rs.getString("RESERVE_DT").equals("00000000000000")) {
							imcins.setString(9, "N");
							imcins.setString(10, null);
						} else {
							imcins.setString(9, "Y");
							imcins.setString(10, rd.format(rs.getString("RESERVE_DT")));
						}
						imcins.setLong(11,  System.currentTimeMillis() );
						imcins.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));
						imcins.setString(13, "READY");
						
						int rowgrs = imcins.executeUpdate();
			            if(rowgrs == 1)
			            {
			                // get candidate id
			            	ResultSet rstemp = null;
			            	rstemp = imcins.getGeneratedKeys();
			                if(rstemp.next())
			                	msg_id = rstemp.getInt(1);
			            }
						
			            imcins.close();

//			            String imcsub = "insert into IMC.IMC_MART_SUB" 
//								            		+"(request_id" 
//								            		+",user_id"    									
//								            		+",sub_id"   
//								            		+",name"       
//								            		+",phone"      
//								            		+",action"     
//								            		+",sync_status)"
//								            		+"values"
//													+"(?" 
//													+",?"    									
//													+",?"   
//													+",?"       
//													+",?"      
//													+",?"     
//													+",?)";
//			            
//			            PreparedStatement imcmsins = conn.prepareStatement(imcsub, Statement.RETURN_GENERATED_KEYS);
//			            imcmsins.setInt(1, msg_id);
//			            imcmsins.setString(2, "dhn7985" );//rs.getString("user_id"));
//			            imcmsins.setString(3, rs.getString("mem_id"));
//			            imcmsins.setString(4, rs.getString("mem_username"));
//			            imcmsins.setString(5, rs.getString("SMS_SENDER") );
//			            imcmsins.setString(6, "INSERT");
//			            imcmsins.setString(7, "READY");
//			            imcmsins.executeUpdate();
//			            imcmsins.close();
			            
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
							memo = "IMC";
						} else {
							amount = price.member_price.price_grs_mms;
							payback = price.member_price.price_grs_mms - price.parent_price.price_grs_mms;
							admin_amt = price.base_price.price_grs_mms;
							memo = "IMC MMS";
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
					
					PreparedStatement imcmsgbc = conn.prepareStatement(imc_bc_ins);
					imcmsgbc.setInt(1, msg_id);
					imcmsgbc.setString(2, msg_type);
					imcmsgbc.setString(3, rp);
					imcmsgbc.setString(4, rs.getString("remark4"));
					imcmsgbc.setString(5, rs.getString("mem_id"));
					imcmsgbc.setString(6, rs.getString("max_sn"));
					imcmsgbc.setString(7, mms1);
					imcmsgbc.setString(8, mms2);
					imcmsgbc.setString(9, mms3);
					imcmsgbc.setString(10, cb_msg_id[idx]);
					imcmsgbc.executeUpdate();
					imcmsgbc.close();
					idx++;
				}
				
			}
			
			rs.close();
			
		}catch(Exception ex) {
			log.info("IMC Summary 오류 - " + ex.toString());
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
		
		Imc_summary.isRunning = false;
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
