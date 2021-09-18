package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


import constants.FriendRequestConstraints;
import constants.InviteConstants;
import constants.UserConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class FriendRequest extends DatabaseConnector {

	public FriendRequest() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	public Pair<Integer, String> sendFriendRequest(String userId, String friendId) {

		try {

			User user = new User();
			if (user.CheckUserExist(userId)) {

				if (user.CheckUserExist(friendId)) {

					if (!isAlreadyRequested(userId, friendId)) {
						String friendsId = getLastFriendRequest();
						if (friendsId != "") {
							return createRequest(Helper.nextId(friendsId, "FRI"), userId, friendId);

						} else {
							return createRequest(FriendRequestConstraints.firstRequestId, userId, friendId);
						}
					} else {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.requestExists));
					}
				} else {

					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.requestedUserNotFound));
				}
			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}
		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	public Pair<Integer, String> acceptFriendRequest(String friendRequestId) {

		try {

			if (isRequestIdExist(friendRequestId)) {
				if (isFriends(friendRequestId)) {
					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
							FriendRequestConstraints.alreayFriends));
				} else {
					return acceptRequest(friendRequestId);
				}

			} else {

				remove();
				return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
						FriendRequestConstraints.requestNotExisit));
			}

		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	public Pair<Integer, String> getAllRequest(String userId) {

		try {

			User user = new User();
			if (user.CheckUserExist(userId)) {

				JSONObject obj = new JSONObject();
				JSONArray friends = getFriends(userId);
				JSONArray sentRequest = sentRequest(userId);
				JSONArray reciveRequest = receiverRequest(userId);

				obj.put("friends", friends);
				obj.put("sentRequest", sentRequest);
				obj.put("receiveRequest", reciveRequest);
				remove();
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						FriendRequestConstraints.getFriendsSuccess, obj));

			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}
		} catch (Exception e) {
			System.out.println("exc exc" + e);
			remove();
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	private JSONArray getFriends(String userId) throws Exception {
		
		String selectStatement = "select * from friends where (senderUserId = ? OR recevierUserId = ?) AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, userId);
		prepStmt.setBoolean(3, true);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		while (rs.next()) {
			
			User user = new User();
			String senderId = rs.getString("senderUserId");
			String receiverId = rs.getString("recevierUserId");
			arr.put(user.getSingleUserDetails(userId.equals(senderId) ? receiverId : senderId));
		}
		prepStmt.close();
		return arr;

	}

	private JSONArray sentRequest(String userId) throws Exception {

		String selectStatement = "select * from friends where senderUserId = ? AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setBoolean(2, false);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		while (rs.next()) {
			User user = new User();
			arr.put(user.getSingleUserDetails(rs.getString("recevierUserId")));
		}
		prepStmt.close();
		return arr;
	}

	private JSONArray receiverRequest(String userId) throws Exception {

		String selectStatement = "select * from friends where recevierUserId = ? AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setBoolean(2, false);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		while (rs.next()) {
			User user = new User();
			arr.put(user.getSingleUserDetails(rs.getString("senderUserId")));
		}

		prepStmt.close();

		return arr;
	}

	private Boolean isRequestIdExist(String requestId) throws Exception {

		String selectStatement = "select * from friends where friendsId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, requestId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			System.out.println("true");
			prepStmt.close();
			return true;
		} else {

			System.out.println("false");
			prepStmt.close();
			return false;
		}

	}

	private Boolean isFriends(String requestId) throws Exception {

		String selectStatement = "select * from friends where friendsId=? AND isFriends=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, requestId);
		prepStmt.setBoolean(2, true);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			System.out.println("true");
			prepStmt.close();
			return true;
		} else {

			System.out.println("false");
			prepStmt.close();
			return false;
		}

	}

	private Boolean isAlreadyRequested(String userId, String friendId) throws Exception {

		String selectStatement = "select * from friends where (senderUserId = ? AND recevierUserId = ?) OR (senderUserId = ? AND recevierUserId = ?);";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, friendId);
		prepStmt.setString(3, friendId);
		prepStmt.setString(4, userId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			System.out.println("true");
			prepStmt.close();
			return true;
		} else {

			System.out.println("false");
			prepStmt.close();
			return false;
		}

	}

	private String getLastFriendRequest() throws Exception {

		String selectStatement = "select friendsId from friends ORDER BY friendsId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String userId = rs.getString("friendsId");
			prepStmt.close();
			return userId;
		} else {
			return "";
		}

	}

	private Pair<Integer, String> createRequest(String friendsId, String userId, String friendId) {
		try {

			String sqlStatement = "insert into friends(friendsId, requestedDate, isDeleted, createdDate, updateDate, senderUserId, recevierUserId, isFriends) values (?, ?, ?, ? ,? ,? ,?, ?);";

			long timeNow = Calendar.getInstance().getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			prepStmt.setString(1, friendsId);
			prepStmt.setTimestamp(2, ts);
			prepStmt.setBoolean(3, false);
			prepStmt.setTimestamp(4, ts);
			prepStmt.setTimestamp(5, ts);
			prepStmt.setString(6, userId);
			prepStmt.setString(7, friendId);
			prepStmt.setBoolean(8, false);

			System.out.println("Testing 3");

			int x = prepStmt.executeUpdate();

			if (x == 1) {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(200,
						ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.userRegisterSuccessfully));
			} else {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.sqlRegisterError));
			}

		} catch (Exception e) {

			System.out.println(" friend request request" + e);
			remove();
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	private Pair<Integer, String> acceptRequest(String friendsId) {

		try {
			String sqlStatement = "update friends set updateDate = ?, isFriends = ? where friendsId = ?;";

			Calendar cal = Calendar.getInstance();
			long timeNow = cal.getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			prepStmt.setTimestamp(1, ts);
			prepStmt.setBoolean(2, true);
			prepStmt.setString(3, friendsId);

			int x = prepStmt.executeUpdate();

			if (x == 1) {

				// Send email
				prepStmt.close();
				remove();
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						FriendRequestConstraints.acceptedSuccessfully));
			} else {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE));
			}
		} catch (Exception e) {
			remove();
			System.out.println("exception ex" + e);
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

}
