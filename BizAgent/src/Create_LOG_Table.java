import java.sql.*;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;
import java.util.Date;

public class Create_LOG_Table implements Runnable {
	
	public static boolean isRunning = false;
	public Logger log;
	public String monthStr;
	//private Connection conn = null;
	
	public Create_LOG_Table(String _db_url, Logger _log) {
		log = _log;
	}
	
	public void run() {
		if(!isRunning) {
			if(monthStr == null || monthStr.isEmpty()) {
				Date month = new Date();
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
				monthStr = transFormat.format(month);
			}
			
			Proc();
		} 
	}
	
	private synchronized  void Proc() {
		isRunning = true;	
		//log.info("Nano it summary 실행");  수정 테스트...
		
		Connection conn = null;
		Statement ShowTable = null;
		Statement CRTTable = null;
		String CreateSTR = "";
		ResultSet rs = null;
		
		try {
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();

			DatabaseMetaData md = conn.getMetaData();
			
			String[] types = {"TABLE"};
			rs = md.getTables(null, "dhn", "CB_PMS_BROADCAST_LOG_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE IF NOT EXISTS `CB_PMS_BROADCAST_LOG_" + monthStr + "` (" + 
						"								`MSG_ID` INT(11) NULL DEFAULT NULL," + 
						"								`MSG_WRT_CNT` INT(11) NULL DEFAULT NULL," + 
						"								`BC_MSG_ID` BIGINT(20) UNSIGNED NOT NULL," + 
						"								`BC_RCV_PHONE` VARCHAR(20) NULL DEFAULT NULL," + 
						"								`BC_SND_ST` INT(11) NULL DEFAULT NULL," + 
						"								`BC_RSLT_NO` VARCHAR(5) NULL DEFAULT NULL," + 
						"								`BC_RSLT_TEXT` VARCHAR(100) NULL DEFAULT NULL," + 
						"								`REQ_SND_DTTM` DATETIME NULL DEFAULT NULL," + 
						"								`REQ_RCV_DTTM` DATETIME NULL DEFAULT NULL," + 
						"								PRIMARY KEY (`BC_MSG_ID`)," + 
						"                                INDEX `idx_CB_PMS_BROADCAST_" + monthStr + "_MSG_ID` (`MSG_ID`)" + 
						"							)" + 
						"							COLLATE='utf8_general_ci'" + 
						"							ENGINE=InnoDB";
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();

			rs = md.getTables(null, "dhn", "cb_grs_broadcast_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE IF NOT EXISTS `cb_grs_broadcast_" + monthStr + "` (" + 
						"								`MSG_ID` INT(11) NOT NULL," + 
						"								`MSG_GB` VARCHAR(5) NOT NULL," + 
						"								`BC_MSG_ID` BIGINT(20) UNSIGNED NOT NULL," + 
						"								`BC_SND_ST` CHAR(1) NULL DEFAULT NULL," + 
						"								`BC_SND_PHN` VARCHAR(20) NOT NULL," + 
						"								`BC_RCV_PHN` VARCHAR(20) NOT NULL," + 
						"								`BC_RSLT_NO` VARCHAR(5) NULL DEFAULT NULL," + 
						"								`BC_RSLT_TEXT` VARCHAR(255) NULL DEFAULT NULL," + 
						"								`BC_SND_DTTM` DATETIME NULL DEFAULT NULL," + 
						"								`BC_RCV_DTTM` DATETIME NULL DEFAULT NULL," + 
						"								PRIMARY KEY (`BC_MSG_ID`)," + 
						"                                INDEX `idx_cb_grs_broadcast_" + monthStr + "_MSG_ID` (`MSG_ID`, `BC_SND_ST`), " + 
						"                                INDEX `idx_cb_grs_broadcast_" + monthStr + "_ST` (`BC_SND_ST`) " + 
						"							)" + 
						"							COLLATE='utf8_general_ci'" + 
						"							ENGINE=InnoDB";
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();			
			
			rs = md.getTables(null, "dhn", "cb_sms_log_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE IF NOT EXISTS `cb_sms_log_" + monthStr + "` (" + 
						"								`TR_NUM` BIGINT(20) NOT NULL," + 
						"								`TR_SENDDATE` DATETIME NULL DEFAULT NULL," + 
						"								`TR_SERIALNUM` INT(10) NULL DEFAULT NULL," + 
						"								`TR_ID` VARCHAR(16) NULL DEFAULT NULL," + 
						"								`TR_SENDSTAT` VARCHAR(1) NOT NULL DEFAULT '0'," + 
						"								`TR_RSLTSTAT` VARCHAR(10) NULL DEFAULT '00'," + 
						"								`TR_MSGTYPE` VARCHAR(1) NOT NULL DEFAULT '0'," + 
						"								`TR_PHONE` VARCHAR(20) NOT NULL DEFAULT ''," + 
						"								`TR_CALLBACK` VARCHAR(20) NULL DEFAULT NULL," + 
						"								`TR_ORG_CALLBACK` VARCHAR(20) NULL DEFAULT ''," + 
						"								`TR_BILL_ID` VARCHAR(20) NULL DEFAULT ''," + 
						"								`TR_RSLTDATE` DATETIME NULL DEFAULT NULL," + 
						"								`TR_MODIFIED` DATETIME NULL DEFAULT NULL," + 
						"								`TR_MSG` VARCHAR(160) NULL DEFAULT NULL," + 
						"								`TR_NET` VARCHAR(4) NULL DEFAULT NULL," + 
						"								`TR_ETC1` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC2` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC3` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC4` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC5` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC6` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC7` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC8` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC9` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC10` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_REALSENDDATE` DATETIME NULL DEFAULT NULL," + 
						"								INDEX `TR_SENDDATE_" + monthStr + "` (`TR_SENDDATE`)," + 
						"								INDEX `TR_SENDSTAT_" + monthStr + "` (`TR_SENDSTAT`)," + 
						"								INDEX `TR_PHONE_" + monthStr + "` (`TR_PHONE`)," + 
						"								INDEX `cb_sms_log_" + monthStr + "_IDX1` (`TR_SENDDATE`)," + 
						"								INDEX `cb_sms_log_" + monthStr + "_IDX2` (`TR_SENDSTAT`)," + 
						"								INDEX `cb_sms_log_" + monthStr + "_IDX3` (`TR_PHONE`)" + 
						"							)" + 
						"							COLLATE='utf8_general_ci'" + 
						"							ENGINE=InnoDB";
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();			

			rs = md.getTables(null, "dhn", "cb_mms_log_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE `cb_mms_log_" + monthStr + "` (" + 
						"                                `MSGKEY` INT(11) NOT NULL," + 
						"                                `SUBJECT` VARCHAR(120) NULL DEFAULT NULL," + 
						"                                `PHONE` VARCHAR(15) NOT NULL," + 
						"                                `CALLBACK` VARCHAR(15) NOT NULL," + 
						"                                `ORG_CALLBACK` VARCHAR(20) NULL DEFAULT ''," + 
						"                                `BILL_ID` VARCHAR(20) NULL DEFAULT ''," + 
						"                                `STATUS` VARCHAR(2) NOT NULL DEFAULT '0'," + 
						"                                `REQDATE` DATETIME NOT NULL," + 
						"                                `MSG` VARCHAR(4000) NULL DEFAULT NULL," + 
						"                                `FILE_CNT` INT(10) NULL DEFAULT '0'," + 
						"                                `FILE_CNT_REAL` INT(10) NULL DEFAULT '0'," + 
						"                                `FILE_PATH1` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH1_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH2` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH2_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH3` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH3_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH4` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH4_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH5` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH5_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `EXPIRETIME` VARCHAR(10) NULL DEFAULT NULL," + 
						"                                `SENTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `RSLTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `REPORTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `TERMINATEDDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `RSLT` VARCHAR(10) NULL DEFAULT NULL," + 
						"                                `REPCNT` INT(10) NULL DEFAULT NULL," + 
						"                                `TYPE` VARCHAR(2) NOT NULL," + 
						"                                `TELCOINFO` VARCHAR(12) NULL DEFAULT NULL," + 
						"                                `ID` VARCHAR(22) NULL DEFAULT NULL," + 
						"                                `POST` VARCHAR(22) NULL DEFAULT NULL," + 
						"                                `ETC1` VARCHAR(68) NULL DEFAULT NULL," + 
						"                                `ETC2` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC3` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC4` INT(10) NULL DEFAULT NULL," + 
						"                                `ETC5` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC6` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC7` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC8` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC9` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC10` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                INDEX `cb_mms_log_" + monthStr + "_IDX1` (`REQDATE`)," + 
						"                                INDEX `cb_mms_log_" + monthStr + "_IDX2` (`REPORTDATE`)," + 
						"                                INDEX `cb_mms_log_" + monthStr + "_IDX3` (`EXPIRETIME`)," + 
						"                                INDEX `cb_mms_log_" + monthStr + "_IDX4` (`SENTDATE`)," + 
						"                                INDEX `cb_mms_log_" + monthStr + "_IDX5` (`TERMINATEDDATE`)" + 
						"                                )" + 
						"                                COLLATE='utf8_general_ci'" + 
						"                                ENGINE=InnoDB";
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();			

			 
			rs = md.getTables(null, "dhn", "cb_nas_sms_msg_log_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE IF NOT EXISTS `cb_nas_sms_msg_log_" + monthStr + "` (" + 
						"								`TR_NUM` BIGINT(20) NOT NULL," + 
						"								`TR_SENDDATE` DATETIME NULL DEFAULT NULL," + 
						"								`TR_SERIALNUM` INT(10) NULL DEFAULT NULL," + 
						"								`TR_ID` VARCHAR(16) NULL DEFAULT NULL," + 
						"								`TR_SENDSTAT` VARCHAR(1) NOT NULL DEFAULT '0'," + 
						"								`TR_RSLTSTAT` VARCHAR(10) NULL DEFAULT '00'," + 
						"								`TR_MSGTYPE` VARCHAR(1) NOT NULL DEFAULT '0'," + 
						"								`TR_PHONE` VARCHAR(20) NOT NULL DEFAULT ''," + 
						"								`TR_CALLBACK` VARCHAR(20) NULL DEFAULT NULL," + 
						"								`TR_ORG_CALLBACK` VARCHAR(20) NULL DEFAULT ''," + 
						"								`TR_BILL_ID` VARCHAR(20) NULL DEFAULT ''," + 
						"								`TR_RSLTDATE` DATETIME NULL DEFAULT NULL," + 
						"								`TR_MODIFIED` DATETIME NULL DEFAULT NULL," + 
						"								`TR_MSG` VARCHAR(160) NULL DEFAULT NULL," + 
						"								`TR_NET` VARCHAR(4) NULL DEFAULT NULL," + 
						"								`TR_ETC1` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC2` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC3` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC4` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC5` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC6` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC7` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC8` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC9` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_ETC10` VARCHAR(34) NULL DEFAULT NULL," + 
						"								`TR_REALSENDDATE` DATETIME NULL DEFAULT NULL," + 
						"								INDEX `NAS_TR_SENDDATE_" + monthStr + "` (`TR_SENDDATE`)," + 
						"								INDEX `NAS_TR_SENDSTAT_" + monthStr + "` (`TR_SENDSTAT`)," + 
						"								INDEX `NAS_TR_PHONE_" + monthStr + "` (`TR_PHONE`)," + 
						"								INDEX `cb_nas_sms_msg_log_" + monthStr + "_IDX1` (`TR_SENDDATE`)," + 
						"								INDEX `cb_nas_sms_msg_log_" + monthStr + "_IDX2` (`TR_SENDSTAT`)," + 
						"								INDEX `cb_nas_sms_msg_log_" + monthStr + "_IDX3` (`TR_PHONE`)" + 
						"							)" + 
						"							COLLATE='utf8_general_ci'" + 
						"							ENGINE=InnoDB";
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();			

			//log.info("Nas Log 생성 시작");
			rs = md.getTables(null, "dhn", "cb_nas_mms_msg_log_"+ monthStr, types);
			
			if(!rs.next()) {
				CRTTable = conn.createStatement();
				CreateSTR = "CREATE TABLE `cb_nas_mms_msg_log_" + monthStr + "` (" + 
						"                                `MSGKEY` INT(11) NOT NULL," + 
						"                                `SUBJECT` VARCHAR(120) NULL DEFAULT NULL," + 
						"                                `PHONE` VARCHAR(15) NOT NULL," + 
						"                                `CALLBACK` VARCHAR(15) NOT NULL," + 
						"                                `ORG_CALLBACK` VARCHAR(20) NULL DEFAULT ''," + 
						"                                `BILL_ID` VARCHAR(20) NULL DEFAULT ''," + 
						"                                `STATUS` VARCHAR(2) NOT NULL DEFAULT '0'," + 
						"                                `REQDATE` DATETIME NOT NULL," + 
						"                                `MSG` VARCHAR(4000) NULL DEFAULT NULL," + 
						"                                `FILE_CNT` INT(10) NULL DEFAULT '0'," + 
						"                                `FILE_CNT_REAL` INT(10) NULL DEFAULT '0'," + 
						"                                `FILE_PATH1` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH1_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH2` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH2_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH3` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH3_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH4` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH4_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `FILE_PATH5` VARCHAR(128) NULL DEFAULT NULL," + 
						"                                `FILE_PATH5_SIZ` INT(10) NULL DEFAULT NULL," + 
						"                                `EXPIRETIME` VARCHAR(10) NULL DEFAULT NULL," + 
						"                                `SENTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `RSLTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `REPORTDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `TERMINATEDDATE` DATETIME NULL DEFAULT NULL," + 
						"                                `RSLT` VARCHAR(10) NULL DEFAULT NULL," + 
						"                                `REPCNT` INT(10) NULL DEFAULT NULL," + 
						"                                `TYPE` VARCHAR(2) NOT NULL," + 
						"                                `TELCOINFO` VARCHAR(12) NULL DEFAULT NULL," + 
						"                                `ID` VARCHAR(22) NULL DEFAULT NULL," + 
						"                                `POST` VARCHAR(22) NULL DEFAULT NULL," + 
						"                                `ETC1` VARCHAR(68) NULL DEFAULT NULL," + 
						"                                `ETC2` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC3` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC4` INT(10) NULL DEFAULT NULL," + 
						"                                `ETC5` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC6` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC7` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC8` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC9` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                `ETC10` VARCHAR(34) NULL DEFAULT NULL," + 
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX1` (`REQDATE`)," + 
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX2` (`REPORTDATE`)," + 
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX3` (`EXPIRETIME`)," + 
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX4` (`SENTDATE`)," + 
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX5` (`TERMINATEDDATE`)," +  
						"                                INDEX `cb_nas_mms_msg_log_" + monthStr + "_IDX6` (`STATUS`,`MSGKEY`)" +
						"                                )" + 
						"                                COLLATE='utf8_general_ci'" + 
						"                                ENGINE=InnoDB";
				
				log.info("Log Table Create : " + CreateSTR);
				CRTTable.executeUpdate(CreateSTR);
				CRTTable.close();
			}
			rs.close();			

		}catch(Exception ex) {
			log.info("Create Log Table 오류 - " + ex.toString() );
		}
		
		try {
			if(CRTTable!=null) {
				CRTTable.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		
		isRunning = false;
		//log.info("Nano it summary 끝");
	}
	
	 
}
