package poc.icici.tess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class DbOps {

	private volatile static Connection con;
	public static String db = "mysql";
	public static String user = "root";
	public static String pass = "root";
	public static String machineip = "localhost";
	public static String port = "3306";

	// fetch Connection
	public static Connection connectDB() {

		try {
			if (con == null || con.isClosed() || !con.isValid(0)) {
				Class.forName("com.mysql.jdbc.Driver");
				synchronized (DbOps.class) {
					if (con == null || !con.isClosed() || !con.isValid(0)) {
						con = DriverManager.getConnection("jdbc:mysql://" + machineip + ":" + port + "/" + db, user,
								pass);
					}
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		return con;
	}

	// get details from the master table.
	public LinkedList<MasterTableObject> getMasterFileDetails() {

		LinkedList<MasterTableObject> list = new LinkedList<MasterTableObject>();
		Connection conn = DbOps.connectDB();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from MASTERFILE_TABLE");
			while (rs.next()) {
				MasterTableObject obj = new MasterTableObject();
				obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
				obj.setMasterFileName(rs.getString("MASTER_FILENAME"));
				obj.setMasterFileLocation(rs.getString("MASTER_FILELOCATION"));
				obj.setCreatedDate(rs.getString("CREATED_DATE"));
				list.add(obj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;

	}

	// get details from the child table.
	public LinkedList<ChildTableObject> getChildFileDetails() {

		LinkedList<ChildTableObject> list = new LinkedList<ChildTableObject>();
		Connection conn = DbOps.connectDB();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from CHILDFILE_TABLE");
			while (rs.next()) {
				ChildTableObject obj = new ChildTableObject();
				obj.setFileId(rs.getInt("FILEID"));
				obj.setFileName(rs.getString("FILENAME"));
				obj.setFileLocation(rs.getString("FILE_LOCATION"));
				obj.setFileSize(rs.getString("FILE_SIZE"));
				obj.setIsuesList(rs.getString("ISSUES_LIST"));
				obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
				obj.setRuleid(rs.getInt("RULE_ID"));
				obj.setProcessedTime(rs.getString("PROCESSED_TIME"));
				obj.setCreatedDate(rs.getString("CREATED_DATE"));
				list.add(obj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	

	// get details from the business table.
	public LinkedList<BusinessRuleObject> getBusinessRuleDetails() {
		LinkedList<BusinessRuleObject> list = new LinkedList<BusinessRuleObject>();
		Connection conn = DbOps.connectDB();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from BUSINESSRULE_TABLE");
			while (rs.next()) {
				BusinessRuleObject obj = new BusinessRuleObject();
				obj.setRuleId(rs.getInt("RULE_ID"));
				obj.setSetOfRules(rs.getString("SET_OF_RULES"));
				obj.setCreatedDate(rs.getString("CREATED_DATE"));
				obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
				list.add(obj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	// get details from the business table w.r.t to masterFileId.
		public LinkedList<BusinessRuleObject> getBusinessDetails(int masterFileId) {
			LinkedList<BusinessRuleObject> list = new LinkedList<BusinessRuleObject>();
			Connection conn = DbOps.connectDB();
			Statement stmt;
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("select * from BUSINESSRULE_TABLE where  MASTER_FILEID ='"+masterFileId +"'");
				while (rs.next()) {
					BusinessRuleObject obj = new BusinessRuleObject();
					obj.setRuleId(rs.getInt("RULE_ID"));
					obj.setSetOfRules(rs.getString("SET_OF_RULES"));
					obj.setCreatedDate(rs.getString("CREATED_DATE"));
					obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
					list.add(obj);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}
		
		// get details from the child table w.r.t masterFileId.
		public LinkedList<ChildTableObject> getChildDetails(int masterFileId) {

			LinkedList<ChildTableObject> list = new LinkedList<ChildTableObject>();
			Connection conn = DbOps.connectDB();
			Statement stmt;
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt
						.executeQuery("select * from CHILDFILE_TABLE where MASTER_FILEID ='" + masterFileId + "'");
				while (rs.next()) {
					ChildTableObject obj = new ChildTableObject();
					obj.setFileId(rs.getInt("FILEID"));
					obj.setFileName(rs.getString("FILENAME"));
					obj.setFileLocation(rs.getString("FILE_LOCATION"));
					obj.setFileSize(rs.getString("FILE_SIZE"));
					obj.setIsuesList(rs.getString("ISSUES_LIST"));
					obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
					obj.setRuleid(rs.getInt("RULE_ID"));
					obj.setProcessedTime(rs.getString("PROCESSED_TIME"));
					obj.setCreatedDate(rs.getString("CREATED_DATE"));
					list.add(obj);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		// get details from the master table w.r.t masterFile name.
		public LinkedList<MasterTableObject> getMasterDetails(String masterFileName) {

			LinkedList<MasterTableObject> list = new LinkedList<MasterTableObject>();
			Connection conn = DbOps.connectDB();
			Statement stmt;
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("select * from MASTERFILE_TABLE where  MASTER_FILENAME ='" + masterFileName + "'");
				while (rs.next()) {
					MasterTableObject obj = new MasterTableObject();
					obj.setMasterFileId(rs.getInt("MASTER_FILEID"));
					obj.setMasterFileName(rs.getString("MASTER_FILENAME"));
					obj.setMasterFileLocation(rs.getString("MASTER_FILELOCATION"));
					obj.setCreatedDate(rs.getString("CREATED_DATE"));
					list.add(obj);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;

		}
		
		// Updating Status, Issues and processed time.
		public void updateChildDetails(String status,String issues,String time,int fileId) {

			Connection conn = DbOps.connectDB();
					Statement stmt;
					try {
						stmt = conn.createStatement();
						String sql = "UPDATE CHILDFILE_TABLE SET STATUS='"+status+"',ISSUES_LIST='"+issues+"',PROCESSED_TIME='"+time+"' WHERE FILEID = '"+fileId+"'";
						
						System.out.println(sql);
						
						stmt.executeUpdate(sql);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
		
}
