import java.sql.*;
import org.apache.log4j.Logger;
import com.mysql.jdbc.Driver;

public class Price_info  {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private String DB_URL;
	private final String USER_NAME = "root";
	private final String PASSWORD = "sjk4556!!22";
	
	public Logger log;
	public Price base_price = new Price();
	public Price parent_price = new Price();
	public Price member_price = new Price();
	
	public Price_info(String db_url, int mem_id ) {
		Connection conn = null;
		Statement bstate = null;
		Statement pstate = null;
		DB_URL = db_url;

		try {
			//Class.forName(JDBC_DRIVER);
			//conn =  DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			conn = BizDBCPInit.getConnection();
			bstate = conn.createStatement();
			
			String sql;
			
			sql = "select SQL_NO_CACHE * from cb_wt_setting limit 1";
			
			ResultSet rs = bstate.executeQuery(sql);
			if(rs.first()) {
				base_price.set_price(
								rs.getFloat("wst_price_ft"),
								rs.getFloat("wst_price_ft_img"),
								rs.getFloat("wst_price_at"),
								rs.getFloat("wst_price_sms"),
								rs.getFloat("wst_price_lms"),
								rs.getFloat("wst_price_mms"),
								rs.getFloat("wst_price_phn"),
								rs.getFloat("wst_price_015"),
								rs.getFloat("wst_price_grs"),
								rs.getFloat("wst_price_grs_sms"),
								rs.getFloat("wst_price_nas"),
								rs.getFloat("wst_price_nas_sms"),
								0.0f,
								rs.getFloat("wst_price_grs_mms"),
								rs.getFloat("wst_price_nas_mms"),
								rs.getFloat("wst_price_smt"),
								rs.getFloat("wst_price_smt_sms"),
								rs.getFloat("wst_price_smt_mms"),
								rs.getFloat("wst_price_imc")
						    );
				
			}
		} catch (Exception ex) {
			System.out.println("cb_wt_setting 조회 오류"+ex.toString());
		}		
		
		try {
			if(bstate!=null) {
				bstate.close();
			}
		} catch(Exception e) {}
		
		
		try {
			pstate = conn.createStatement();
			
			String sql;
			sql = "			select SQL_NO_CACHE b.mem_userid, b.mem_level  " + 
					"				,i.mad_price_at     as c_mad_price_at    " + 
					"				,i.mad_price_ft     as c_mad_price_ft    " + 
					"				,i.mad_price_ft_img as c_mad_price_ft_img" + 
					"				,i.mad_price_grs    as c_mad_price_grs   " + 
					"				,i.mad_price_nas    as c_mad_price_nas   " + 
					"				,i.mad_price_grs_sms as c_mad_price_grs_sms   " + 
					"				,i.mad_price_nas_sms as c_mad_price_nas_sms   " + 
					"				,i.mad_price_015    as c_mad_price_015   " + 
					"				,i.mad_price_phn    as c_mad_price_phn   " + 
					"				,i.mad_price_sms    as c_mad_price_sms   " + 
					"				,i.mad_price_lms    as c_mad_price_lms   " + 
					"				,i.mad_price_mms    as c_mad_price_mms   " + 
					"				,i.mad_price_grs_mms    as c_mad_price_grs_mms   " + 
					"				,i.mad_price_nas_mms    as c_mad_price_nas_mms   " + 
					"				,i.mad_price_smt    as c_mad_price_smt   " + 
					"				,i.mad_price_smt_sms    as c_mad_price_smt_sms   " + 
					"				,i.mad_price_smt_mms    as c_mad_price_smt_mms   " + 
					"				,i.mad_price_imc    as c_mad_price_imc   " + 
					"				,a.mad_price_at     as p_mad_price_at    " + 
					"				,a.mad_price_ft     as p_mad_price_ft    " + 
					"				,a.mad_price_ft_img as p_mad_price_ft_img" + 
					"				,a.mad_price_grs    as p_mad_price_grs   " + 
					"				,a.mad_price_nas    as p_mad_price_nas   " + 
					"				,a.mad_price_grs_sms as p_mad_price_grs_sms   " + 
					"				,a.mad_price_nas_sms as p_mad_price_nas_sms   " + 
					"				,a.mad_price_015    as p_mad_price_015   " + 
					"				,a.mad_price_phn    as p_mad_price_phn   " + 
					"				,a.mad_price_sms    as p_mad_price_sms   " + 
					"				,a.mad_price_lms    as p_mad_price_lms   " + 
					"				,a.mad_price_mms    as p_mad_price_mms   " + 
					"				,a.mad_price_grs_mms    as p_mad_price_grs_mms   " + 
					"				,a.mad_price_nas_mms    as p_mad_price_nas_mms   " + 
					"				,a.mad_price_smt    as p_mad_price_smt   " + 
					"				,a.mad_price_smt_sms    as p_mad_price_smt_sms   " + 
					"				,a.mad_price_smt_mms    as p_mad_price_smt_mms   " + 
					"				,a.mad_price_imc    as p_mad_price_imc   " + 
					"			from" + 
					"				cb_wt_member_addon i left join" + 
					"				cb_wt_member_addon a on 1=1 inner join" + 
					"				cb_member b on a.mad_mem_id=b.mem_id inner join" + 
					"				(" + 
					"					SELECT distinct @r AS _id, (SELECT  @r := mrg_recommend_mem_id FROM cb_member_register WHERE mem_id = _id ) AS mrg_recommend_mem_id" + 
					"					FROM" + 
					"						(SELECT  @r := "+ mem_id +", @cl := 0) vars,	cb_member_register h" + 
					"					WHERE    @r <> 0" + 
					"				) c on a.mad_mem_id=c.mrg_recommend_mem_id" + 
					"			where i.mad_mem_id="+ mem_id + 
					"			order by b.mem_level desc";
			 
			ResultSet rs1 = pstate.executeQuery(sql);
			if(rs1.first()) {
				 
				parent_price.set_price(
								rs1.getFloat("p_mad_price_ft"),
								rs1.getFloat("p_mad_price_ft_img"),
								rs1.getFloat("p_mad_price_at"),
								rs1.getFloat("p_mad_price_sms"),
								rs1.getFloat("p_mad_price_lms"),
								rs1.getFloat("p_mad_price_mms"),
								rs1.getFloat("p_mad_price_phn"),
								rs1.getFloat("p_mad_price_015"),
								rs1.getFloat("p_mad_price_grs"),
								rs1.getFloat("p_mad_price_grs_sms"),
								rs1.getFloat("p_mad_price_nas"),
								rs1.getFloat("p_mad_price_nas_sms"),
								0.0f,
								rs1.getFloat("p_mad_price_grs_mms"),
								rs1.getFloat("p_mad_price_nas_mms"),
								rs1.getFloat("p_mad_price_smt"),
								rs1.getFloat("p_mad_price_smt_sms"),
								rs1.getFloat("p_mad_price_smt_mms"),
								rs1.getFloat("p_mad_price_imc")
						    );
				
				member_price.set_price(
						rs1.getFloat("c_mad_price_ft"),
						rs1.getFloat("c_mad_price_ft_img"),
						rs1.getFloat("c_mad_price_at"),
						rs1.getFloat("c_mad_price_sms"),
						rs1.getFloat("c_mad_price_lms"),
						rs1.getFloat("c_mad_price_mms"),
						rs1.getFloat("c_mad_price_phn"),
						rs1.getFloat("c_mad_price_015"),
						rs1.getFloat("c_mad_price_grs"),
						rs1.getFloat("c_mad_price_grs_sms"),
						rs1.getFloat("c_mad_price_nas"),
						rs1.getFloat("c_mad_price_nas_sms"),
						0.0f,
						rs1.getFloat("c_mad_price_grs_mms"),
						rs1.getFloat("c_mad_price_nas_mms"),
						rs1.getFloat("c_mad_price_smt"),
						rs1.getFloat("c_mad_price_smt_sms"),
						rs1.getFloat("c_mad_price_smt_mms"),
						rs1.getFloat("c_mad_price_imc")
				    );				
				
			}
			
		} catch (Exception ex) {
			System.out.println("cb_wt_member_addon 조회 오류"+ex.toString());
		}
		
		try {
			if(bstate!=null) {
				pstate.close();
			}
		} catch(Exception e) {}

		try {
			if(conn!=null) {
				conn.close();
			}
		} catch(Exception e) {}
		//System.out.println("연결 종료!!!");
	}
	
}
