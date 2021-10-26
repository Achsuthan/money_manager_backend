package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.InviteConstants;
import unitls.ApiResponseHandler;
import unitls.Pair;
import unitls.ResponseType;

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

	public Pair<Integer, String>  getAllCategories() throws Exception {

		String selectStatement = "select * from category;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("categoryID", rs.getString("categoryId"));
			obj.put("categoryName", rs.getString("categoryName"));
			obj.put("imageName", rs.getString("imageName"));
			obj.put("color", rs.getString("color"));
			arr.put(obj);
		}
		
		JSONObject returnObject = new JSONObject();
		returnObject.put("categories", arr);
		
		return new Pair<Integer, String>(200,
				ApiResponseHandler.apiResponse(ResponseType.SUCCESS, returnObject)); 
	}

}
