package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import constants.GroupConstraints;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class Group extends DatabaseConnector {

	public Group() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Pair<Integer, String> createGroup(String userId, String groupName) {
		try {
			User user = new User();
			
			if(user.CheckUserExist(userId)) {
				String lastId = getLastGroup();
				System.out.println("last id"+ lastId);
				if(!lastId.isEmpty()) {
					lastId = Helper.nextId(lastId, "GRP");
					System.out.println("last id"+ lastId);
					if(addGroupInfo(lastId, groupName)) {
						return createAccessLevel(userId);
					}
					else {
						remove();
			            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
					}
				}
				else {
					if(addGroupInfo(GroupConstraints.firstGroupId, groupName)) {
						return createAccessLevel(userId);
					}
					else {
						remove();
			            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
					}
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
	
	private String getLastGroup() throws Exception{

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
	
	
	public Boolean checkGroupExist(String groupId) throws Exception{

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
	
	private Boolean addGroupInfo(String groupId, String groupName) throws Exception {
	
		String sqlStatement = "insert into spending_group(groupId, name, createdDate, updatedDate, isDeleted) values (?, ?, ? ,? ,?);";

		Calendar cal = Calendar.getInstance();
		long timeNow = cal.getTimeInMillis();
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
		prepStmt.setString(1, groupId);
		prepStmt.setString(2, groupName);
		prepStmt.setTimestamp(3, ts);
		prepStmt.setTimestamp(4, ts);
		prepStmt.setBoolean(5, false);

		System.out.println("Testing 3");

		int x = prepStmt.executeUpdate();

		if (x == 1) {
			prepStmt.close();
			return true;
		} else {
			prepStmt.close();
			return false;
		}
	}
	
	private Pair<Integer, String> createAccessLevel(String userId) throws Exception {
		
		
		String groupId = getLastGroup();
		
		if(groupId.isEmpty()) {
			remove();
            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
		}
		else {
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
			prepStmt.setInt(2, 0);
			prepStmt.setBoolean(3, true);
			prepStmt.setBoolean(4, false);
			prepStmt.setTimestamp(5, ts);
			prepStmt.setTimestamp(6, ts);
			prepStmt.setString(7, groupId);
			prepStmt.setString(8, userId);

			System.out.println("Testing 3");

			int x = prepStmt.executeUpdate();

			if (x == 1) {
				prepStmt.close();
				remove();
	            return new Pair<Integer, String>(200 ,ApiResponseHandler.apiResponse(ResponseType.SUCCESS, GroupConstraints.success));
			} else {
				prepStmt.close();
				remove();
	            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
			}
		}
		
		
	}

}
