package excelupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;

import com.mysql.jdbc.Driver;
import java.util.Date;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class main {
		private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	    private static String DB_URL = "jdbc:mysql://210.114.225.53/dhn?characterEncoding=utf8";
		private static String USER_NAME = "root";
		private static String PASSWORD = "sjk4556!!22";
		
		public static void main(String[] args) {
			
			Connection conn = null;
			Statement grs_msg = null;

			Properties p = new Properties();
		 
			try {
//				p.load(new FileInputStream("/root/BizAgent/conf/db.properties"));
		        p.load(new FileInputStream("E:\\Git\\BizAgent\\conf\\db.properties")); 

				DB_URL = p.getProperty("DB_URL");
				USER_NAME = p.getProperty("USER_NAME");
				PASSWORD = p.getProperty("PASSWORD");

				Class.forName(JDBC_DRIVER);
				conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);

				Workbook wb = WorkbookFactory.create(new File("e:\\biz\\bbb.xls"/*args[0]*/)); 
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter dataFormatter = new DataFormatter();

				int r = 0;
				StringBuffer sb = new StringBuffer();
				sb.append("ab_id,ab_tel,ab_name,ab_kind,ab_datetime,ab_status"+"\n");
				
//				String ins = "insert into cb_ab_upload_temp(ab_id, ab_tel, ab_name, ab_kind, ab_datetime, ab_status) value(?, ?, ?, ?, ?, ?)";
//				PreparedStatement prest = conn.prepareStatement(ins);
				for(Row row:sheet) {
					r++;
					
					Cell ab_tel = row.getCell(0);
					Cell ab_name = row.getCell(1);
					Cell ab_kind = row.getCell(2);
					Cell del = row.getCell(3);

					String tel_value = dataFormatter.formatCellValue(ab_tel);
					tel_value = tel_value.replaceAll("[^0-9]", "");
					System.out.println("" + r + " : " + tel_value);
					if(tel_value != null && tel_value.length() > 5 ) {
						String name_value = dataFormatter.formatCellValue(ab_name);
						String kind_value = dataFormatter.formatCellValue(ab_kind);
						String del_value = dataFormatter.formatCellValue(del);

						sb.append("11111" + "," + tel_value + "," + name_value+ "," + kind_value+ ","+ "," +del_value + new java.sql.Timestamp(System.currentTimeMillis()) +"\n");
						}
					}
					
				wb.close();
				
				FileWriter fw=new FileWriter("E:\\biz\\a.txt",false);
				fw.write(sb.toString());
				fw.flush();
				fw.close();
				
				Statement st = conn.createStatement();
				String ststr = "LOAD DATA LOCAL INFILE 'E:/biz/a.txt' " + 
								"REPLACE INTO TABLE cb_ab_upload_temp " + 
								"CHARACTER SET utf8mb4 " + 
								"FIELDS " + 
								"    TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'" + 
								"LINES " + 
								"    TERMINATED BY '\n'" + 
								"IGNORE 1 LINES";
				st.execute(ststr);
				st.close();
				
			}catch(Exception ex) {
				ex.printStackTrace(); 
			}
			 
			try {
				if(conn!=null) {
					conn.close();
				}
			} catch(Exception ex) {}
			
		}
		 
}
