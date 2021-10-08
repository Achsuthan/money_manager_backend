package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.SearchConstraints;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class Group extends DatabaseConnector {

	public Group() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	//Create group
	public Pair<Integer, String> createGroup(String userId, String groupName) {
		
		try {
			
			//Check user
			User user = new User();
			if(user.CheckUserExist(userId)) {
				
				//Last group Id
				String lastId = getLastGroupId();
				if(!lastId.isEmpty()) {
					
					lastId = Helper.nextId(lastId, "GRP");
					System.out.println("last id"+ lastId);
					return addGroupInfo(lastId, groupName, userId);
				}
				else {
					
					//First group creation
					return addGroupInfo(GroupConstraints.firstGroupId, groupName, userId); 
						
					
				}
			}
			else {
				
				remove();
	            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.userNotFound));
			}
		}
		catch(Exception e) {
			System.out.println("error errror" + e);
			remove();
            return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	//Get all the groups 
	public Pair<Integer, String> getAllGroup(String userId) {
		
		try {
			
			//Check user
			User user = new User();
			if(user.CheckUserExist(userId)) {
				
				ResultSet accessLevel = getAllAccessLevelBasedOnUser(userId);
				
				JSONArray groupArray = new JSONArray();
				while(accessLevel.next()) {
					
					Boolean isAccepted = accessLevel.getBoolean("isAccepted");
					if(isAccepted) {
						
						String groupId = accessLevel.getString("groupId");
						ResultSet groupInfo = getGroupInfoBasedOnID(groupId);
						
						if(groupInfo.next()) {
							String groupName = groupInfo.getString("name"); 
							String groupUserId = groupInfo.getString("createrId");
							
							JSONObject singleGroup = new JSONObject();
							singleGroup.put("groupId", groupId);
							singleGroup.put("groupName", groupName);
							singleGroup.put("isOwner", false);
							if(userId.equals(groupUserId)) {
								singleGroup.put("isOwner", true);
							}
							
							groupArray.put(singleGroup);
						}
					}
				}
				//TODO: Need to get the transactions details here
				JSONObject retunOBject = new JSONObject();
				retunOBject.put("group", groupArray);
				remove();
				return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, SearchConstraints.success, retunOBject));
				
			}
			else {
				
				remove();
	            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.userNotFound));
			}
		}
		catch(Exception e) {
			
			System.out.println("error errror" + e);
			remove();
            return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	//Get the last group Id
	private String getLastGroupId() throws Exception{

		String selectStatement = "select groupId from spending_group ORDER BY groupId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String inviteId = rs.getString("groupId");
			prepStmt.close();
			return inviteId;
		} else {
			return "";
		}
	}
	
	
	// Get all Access Level based on the user
	private ResultSet getGroupInfoBasedOnID(String groupId) throws Exception {

		String selectStatement = "select * from spending_group where groupId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, groupId);
		ResultSet rs = prepStmt.executeQuery();

		return rs;
	}
	
	// Check user already exist in the access level
	public Boolean checkUserAlreadyHaveAccessToSameGroup(String userId, String groupId) throws Exception {

		String selectStatement = "select * from access_level where userId=? AND groupId=? AND isAccepted = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, groupId);
		prepStmt.setBoolean(3, true);
		ResultSet rs = prepStmt.executeQuery();

		if(rs.next()) {
			
			prepStmt.close();
			return true;
		}
		
		return false;
	}
	
	// Get all Access Level based on the user
	private ResultSet getAllAccessLevelBasedOnUser(String userId) throws Exception {

		String selectStatement = "select * from access_level where userId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		ResultSet rs = prepStmt.executeQuery();

		return rs;
	}
	
	//Get Last Access Level Id
	private String getLastAccessLevel() throws Exception{

		String selectStatement = "select accessLevelId from access_level ORDER BY accessLevelId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String inviteId = rs.getString("accessLevelId");
			prepStmt.close();
			return inviteId;
		} else {
			return "";
		}
	}
	
	public Boolean checkOwerner(String userId, String groupId) throws Exception {
		
		String selectStatement = "select * from access_level where groupId = ? AND userId=? AND level=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, groupId);
		prepStmt.setString(2, userId);
		prepStmt.setInt(3, 0);
		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	// Group check
	public Boolean checkGroupExist(String groupId) throws Exception {

		String selectStatement = "select * from spending_group where groupId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, groupId);
		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}
	
	//Create group
	private Pair<Integer, String> addGroupInfo(String groupId, String groupName, String userId) throws Exception {
		
		String sqlStatement = "insert into spending_group(groupId, name, createdDate, updatedDate, isDeleted, createrId) values (?, ?, ? ,? ,?, ?);";

		Calendar cal = Calendar.getInstance();
		long timeNow = cal.getTimeInMillis();
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
		prepStmt.setString(1, groupId);
		prepStmt.setString(2, groupName);
		prepStmt.setTimestamp(3, ts);
		prepStmt.setTimestamp(4, ts);
		prepStmt.setBoolean(5, false);
		prepStmt.setString(6, userId);

		int x = prepStmt.executeUpdate();

		if (x == 1) {
			
			prepStmt.close();
			return createAccessLevel(userId, groupId, 0, true);
		} else {
			
			prepStmt.close();
			remove();
            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
		}
		
	}
	
	//Create access level
	public Pair<Integer, String> createAccessLevel(String userId, String groupId, Integer accessLevel, Boolean isAccepted) throws Exception {
		
		//Last Access Level Id
		String accessLevelId = getLastAccessLevel();
		if(accessLevelId.isEmpty()) {
			accessLevelId = GroupConstraints.firstAccessLevelId;
		}
		else {
			accessLevelId = Helper.nextId(accessLevelId, "GAL");
		}
		
		String sqlStatement = "insert into access_level(accessLevelId, level, isAccepted, isDeleted ,createdDate, updatedDate, groupId, userId) values (?, ?, ? ,? ,?, ?, ?, ?);";

		Calendar cal = Calendar.getInstance();
		long timeNow = cal.getTimeInMillis();
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
		prepStmt.setString(1, accessLevelId);
		prepStmt.setInt(2, accessLevel);
		prepStmt.setBoolean(3, isAccepted);
		prepStmt.setBoolean(4, false);
		prepStmt.setTimestamp(5, ts);
		prepStmt.setTimestamp(6, ts);
		prepStmt.setString(7, groupId);
		prepStmt.setString(8, userId);

		System.out.println("Testing 3");

		int x = prepStmt.executeUpdate();

		if (x == 1) {
			//Success
			prepStmt.close();
			remove();
            return new Pair<Integer, String>(200 ,ApiResponseHandler.apiResponse(ResponseType.SUCCESS, GroupConstraints.success));
		} else {
			//Failure
			prepStmt.close();
			remove();
            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
		}
	}

}
