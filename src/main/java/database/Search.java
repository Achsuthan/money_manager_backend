package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.SearchConstraints;
import unitls.ApiResponseHandler;
import unitls.Pair;
import unitls.ResponseType;

public class Search extends DatabaseConnector {

	public Search() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Pair<Integer, String> searchFriends(String keyword, String userId) {
		
		try {
			String sqlStatement = """
					select userId, name, email  
					from user 
					WHERE (
						userId in (
							select recevierUserId 
							from friends 
							where (senderUserId = ? AND isFriends = true)
						) 
						OR 
						(
						userId in (
							select senderUserId 
							from friends 
							where recevierUserId = ? AND isFriends = true)
						)
					)
					AND 
					(
						name LIKE ? OR email LIKE ?
					) 
					AND userId != ?
					""";
			
			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			
			prepStmt.setString(1, userId);
			prepStmt.setString(2, userId);
			prepStmt.setString(3, '%'+keyword+'%');
			prepStmt.setString(4, '%'+keyword+'%');
			prepStmt.setString(5, userId);

			ResultSet rs = prepStmt.executeQuery();

			System.out.println("statement" + rs);

			JSONArray friendsArray = new JSONArray();

			while (rs.next()) {
				JSONObject user = new JSONObject();
				user.put("userId", rs.getString("userId"));
				user.put("name", rs.getString("name"));
				user.put("email", rs.getString("email"));
				friendsArray.put(user);
			}
			JSONObject obj = new JSONObject();
			obj.put("friends", friendsArray);
			remove();
			return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, SearchConstraints.success, obj));
		}
		catch (Exception e) {
			remove();
			System.out.println("Error Error "+ e);
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	public Pair<Integer, String> searchAllUsers(String keyword, String userId) {
		
		try {
			String sqlStatement = "select userId, name, email from user where (name LIKE ? OR email LIKE ?) AND userId != ?";
			
			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			
			prepStmt.setString(1, '%'+keyword+'%');
			prepStmt.setString(2, '%'+keyword+'%');
			prepStmt.setString(3, userId);

			ResultSet rs = prepStmt.executeQuery();

			System.out.println("statement" + rs);

			JSONArray friendsArray = new JSONArray();

			while (rs.next()) {
				JSONObject user = new JSONObject();
				user.put("userId", rs.getString("userId"));
				user.put("name", rs.getString("name"));
				user.put("email", rs.getString("email"));
				friendsArray.put(user);
			}
			JSONObject obj = new JSONObject();
			obj.put("user", friendsArray);
			remove();
			return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, SearchConstraints.success, obj));
		}
		catch (Exception e) {
			remove();
			System.out.println("Error Error "+ e);
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	public Pair<Integer, String> searchUserByAllAndFriends(String keyword, String userId) {
		
		try {
			String sqlStatementGetAllUserWithoutFriends = """
					select userId, name, email  
					from user 
					WHERE (
						userId not in (
							select recevierUserId 
							from friends 
							where (senderUserId = ? AND isFriends = true)
						) 
						AND 
						(
						userId not in (
							select senderUserId 
							from friends 
							where recevierUserId = ? AND isFriends = true)
						)
					)
					AND 
					(
						name LIKE ? OR email LIKE ?
					) 
					AND userId != ?
					""";
			
			PreparedStatement prepStmtGetAllUserWithoutFriends = con.prepareStatement(sqlStatementGetAllUserWithoutFriends);
			
			prepStmtGetAllUserWithoutFriends.setString(1, userId);
			prepStmtGetAllUserWithoutFriends.setString(2, userId);
			prepStmtGetAllUserWithoutFriends.setString(3, '%'+keyword+'%');
			prepStmtGetAllUserWithoutFriends.setString(4, '%'+keyword+'%');
			prepStmtGetAllUserWithoutFriends.setString(5, userId);

			ResultSet rsAllWithoutFriends = prepStmtGetAllUserWithoutFriends.executeQuery();

			System.out.println("statement" + rsAllWithoutFriends);

			JSONArray userArray = new JSONArray();

			while (rsAllWithoutFriends.next()) {
				JSONObject user = new JSONObject();
				user.put("userId", rsAllWithoutFriends.getString("userId"));
				user.put("name", rsAllWithoutFriends.getString("name"));
				user.put("email", rsAllWithoutFriends.getString("email"));
				userArray.put(user);
			}
			JSONObject obj = new JSONObject();
			obj.put("users", userArray);
			
			
			String sqlStatementFriends = """
					select userId, name, email  
					from user 
					WHERE (
						userId in (
							select recevierUserId 
							from friends 
							where (senderUserId = ? AND isFriends = true)
						) 
						OR 
						(
						userId in (
							select senderUserId 
							from friends 
							where recevierUserId = ? AND isFriends = true)
						)
					)
					AND 
					(
						name LIKE ? OR email LIKE ?
					) 
					AND userId != ?
					""";
			
			PreparedStatement prepStmtFriends = con.prepareStatement(sqlStatementFriends);
			
			prepStmtFriends.setString(1, userId);
			prepStmtFriends.setString(2, userId);
			prepStmtFriends.setString(3, '%'+keyword+'%');
			prepStmtFriends.setString(4, '%'+keyword+'%');
			prepStmtFriends.setString(5, userId);

			ResultSet rsFriends = prepStmtFriends.executeQuery();

			System.out.println("statement" + rsFriends);
			JSONArray friendsArray = new JSONArray();;

			while (rsFriends.next()) {
				JSONObject user = new JSONObject();
				user.put("userId", rsFriends.getString("userId"));
				user.put("name", rsFriends.getString("name"));
				user.put("email", rsFriends.getString("email"));
				friendsArray.put(user);
			}
			obj.put("friends", friendsArray);
			
			return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, SearchConstraints.success, obj));
			
		}
		catch (Exception e) {
			remove();
			System.out.println("Error Error "+ e);
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

}
