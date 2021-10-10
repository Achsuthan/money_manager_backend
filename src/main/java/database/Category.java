package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Category extends DatabaseConnector {

	public Category() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Boolean isCategoryExist(String cateogoryId) throws Exception {
		
		String selectStatement = "select * from category where categoryId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, cateogoryId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			return true;
		}
		return false;
	}

}
