import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Smt_Proc implements Runnable {

	private String DB_URL;
	
	public Logger log;
	public static boolean isRunning ;
	
	public Smt_Proc(String _db_url, Logger _log) {
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
		Statement smt_msg = null;
		int stotalcnt = 0;
		int ftotalcnt = 0;

		try {

			conn = BizDBCPInit.getConnection();
			
			URL url = new URL("http://66.232.143.52/apis/pms/send");
			
			if(isRunning) {
				// 성공 처리
				smt_msg = conn.createStatement();
				String smtmsg_str = "select (message_id div 1000) as part, " + 
								    "       max(message_id) as msg_id," + 
									"       group_concat( receivers) as PHN," + 
									"       group_concat(message_id) as msg_ids," + 
									"       max(request_id) as REMARK4, " +
									"       max(message) as msg, " +
									"       max(sender) as sender " +
									"  from SMT_SEND " + 
									" where SEND_STATUS = 'READY' " + 
									" group by request_id, (message_id div 1000)";
				
				ResultSet rs = smt_msg.executeQuery(smtmsg_str);
				String inputline;
				
				while(rs.next()) {
					int ins_cnt = rs.getString("PHN").split(",").length;
					stotalcnt = stotalcnt + ins_cnt;

					Map<String, Object> params = new LinkedHashMap<>();
					params.put("cnt", ins_cnt);

					JSONObject msg = new JSONObject();
					msg.put("cnt", ins_cnt);
					
					JSONObject list = new JSONObject();
					
					list.put("msgid", rs.getString("msg_id"));
					list.put("sender", rs.getString("sender"));
					list.put("receiver", rs.getString("PHN"));
					list.put("msg", rs.getString("msg"));
					
					JSONArray lists = new JSONArray();
					
					lists.add(list);
					
					msg.put("list", lists);
					
					HttpURLConnection postconn = (HttpURLConnection) url.openConnection();
					postconn.setDoOutput(true);
					postconn.setRequestMethod("POST");
					postconn.setRequestProperty("Authorization", "FE227003022D124978D41FFA0C3F71CA");
					postconn.setRequestProperty("Content-Type", "application/json");
					postconn.setConnectTimeout(10000);
					postconn.setReadTimeout(10000);
					
					OutputStream os = postconn.getOutputStream();
					os.write(msg.toJSONString().getBytes("UTF-8"));
					os.flush();

					BufferedReader in = new BufferedReader(new InputStreamReader(postconn.getInputStream(), "UTF-8"));
					StringBuffer outRes = new StringBuffer();
					while((inputline = in.readLine()) != null ) {
						outRes.append(inputline);
					}
					
					postconn.disconnect();
					
					log.info("결과 : " + outRes.toString() + " / " + rs.getString("msg_ids"));
					
					String smt_udt = "update SMT_SEND set SEND_STATUS = 'SUCCESS' where message_id in (" + rs.getString("msg_ids") + ") ";
					PreparedStatement st_smt_udt = conn.prepareStatement(smt_udt);
					st_smt_udt.executeUpdate();
					st_smt_udt.close();
 
				}
				
				rs.close();
			}
			
 

		}catch(Exception ex) {
			log.info("SMT 처리 오류 - " + ex.toString());
		}
		
		if(stotalcnt > 0 || ftotalcnt > 0) {
			log.info("SMT 처리 - 성공 : " + stotalcnt);
		}
		
		try {
			if(smt_msg!=null) {
				smt_msg.close();
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
