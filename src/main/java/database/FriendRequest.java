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

	// Create the friend request
	public Pair<Integer, String> sendFriendRequest(String userId, String friendId) {

		try {

			// user check
			User user = new User();
			if (user.CheckUserExist(userId)) {

				// user check
				if (user.CheckUserExist(friendId)) {

					if (!isAlreadyRequested(userId, friendId)) {

						String friendsId = getLastFriendRequest();
						if (!friendsId.isEmpty()) {

							// First friends
							return createRequest(Helper.nextId(friendsId, "FRI"), userId, friendId);

						} else {
							return createRequest(FriendRequestConstraints.firstRequestId, userId, friendId);
						}
					} else {

						// Already request sent
						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.requestExists));
					}
				} else {

					// User not found
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.requestedUserNotFound));
				}
			} else {

				// user not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}
		} catch (Exception e) {

			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Accept fried request
	public Pair<Integer, String> acceptFriendRequest(String friendRequestId, String userId) {

		try {

			// check user
			User user = new User();
			if (user.CheckUserExist(userId)) {

				// check friends id
				if (isRequestIdExist(friendRequestId)) {

					// Check is friends
					if (isFriends(friendRequestId)) {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.alreayFriends));
					} else {

						// Check user exist in the same friendRequest
						if (checkUserExist(userId, friendRequestId)) {
							
							// Check user exist in the same friendRequest
							if (acceptCheckUserExist(userId, friendRequestId)) {
								
								// Accept friend request
								return acceptRequest(friendRequestId);
							}
							else {
								
								remove();
								return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
										FriendRequestConstraints.senderAccessNotAvailable));
							}
							
						} else {

							remove();
							return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
									FriendRequestConstraints.noAccess));
						}

					}

				} else {

					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
							FriendRequestConstraints.requestNotExisit));
				}
			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}

		} catch (Exception e) {

			System.out.println("error error "+ e);
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// user exist in the sender or receiver
	private Boolean checkUserExist(String userId, String requestId) throws Exception {

		String selectStatement = "select * from friends where (senderUserId = ? OR receiverUserId = ?) AND friendsId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, userId);
		prepStmt.setString(3, requestId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}
	}
	
	
	// user exist in the sender or receiver
		private Boolean acceptCheckUserExist(String userId, String requestId) throws Exception {

			String selectStatement = "select * from friends where receiverUserId = ? AND friendsId=?;";

			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, userId);
			prepStmt.setString(2, requestId);

			ResultSet rs = prepStmt.executeQuery();

			if (rs.next()) {

				prepStmt.close();
				return true;
			} else {

				prepStmt.close();
				return false;
			}
		}

	public Pair<Integer, String> deleteFriendRequest(String userId, String friendRequestId) {
		try {

			// check user
			User user = new User();
			if (user.CheckUserExist(userId)) {

				// check friends id
				if (isRequestIdExist(friendRequestId)) {

					System.out.println("request check");
					// Check user exist in the same friendRequest
					if (checkUserExist(userId, friendRequestId)) {
						
						//Delete request
						String selectStatement = "delete from friends where friendsId=?;";
						PreparedStatement prepStmt = con.prepareStatement(selectStatement);
						prepStmt.setString(1, friendRequestId);

						prepStmt.executeUpdate();
						
						remove();
						//Invite deleted successfully
						return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.deletedSuccess));
					} else {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.noAccess));
					}

				} else {

					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
							FriendRequestConstraints.requestNotExisit));
				}
			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}

		} catch (Exception e) {

			System.out.println("exception exception "+ e);
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Get all friends request handle
	public Pair<Integer, String> getAllRequest(String userId) {

		try {

			// user check
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
				// Add all the requests
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						FriendRequestConstraints.getFriendsSuccess, obj));

			} else {

				// User not found handle
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.userNotFound));
			}
		} catch (Exception e) {

			// Exception
			remove();
			return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Get the friends users
	private JSONArray getFriends(String userId) throws Exception {

		String selectStatement = "select * from friends where (senderUserId = ? OR receiverUserId = ?) AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, userId);
		prepStmt.setBoolean(3, true);

		ResultSet rs = prepStmt.executeQuery();
		JSONArray arr = new JSONArray();
		while (rs.next()) {

			User user = new User();
			String senderId = rs.getString("senderUserId");
			String receiverId = rs.getString("receiverUserId");
			JSONObject obj = new JSONObject();
			obj = user.getSingleUserDetails(userId.equals(senderId) ? receiverId : senderId);
			obj.put("friendsId", rs.getString("friendsId"));
			arr.put(obj);
		}

		prepStmt.close();
		return arr;

	}

	// Get sent request fiends
	private JSONArray sentRequest(String userId) throws Exception {

		String selectStatement = "select * from friends where senderUserId = ? AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setBoolean(2, false);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		while (rs.next()) {
			
			User user = new User();
			JSONObject obj = new JSONObject();
			obj = user.getSingleUserDetails(rs.getString("receiverUserId"));
			obj.put("friendsId", rs.getString("friendsId"));
			arr.put(obj);
		}

		prepStmt.close();
		return arr;
	}

	// Get all Receive friends
	private JSONArray receiverRequest(String userId) throws Exception {

		String selectStatement = "select * from friends where receiverUserId = ? AND isFriends = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setBoolean(2, false);

		ResultSet rs = prepStmt.executeQuery();

		JSONArray arr = new JSONArray();
		while (rs.next()) {
			User user = new User();
			
			JSONObject obj = new JSONObject();
			obj = user.getSingleUserDetails(rs.getString("senderUserId"));
			obj.put("friendsId", rs.getString("friendsId"));
			arr.put(obj);
			
		}

		prepStmt.close();

		return arr;
	}

	// Request ID exist
	private Boolean isRequestIdExist(String requestId) throws Exception {

		String selectStatement = "select * from friends where friendsId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, requestId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}

	// Is friends
	private Boolean isFriends(String requestId) throws Exception {

		String selectStatement = "select * from friends where friendsId=? AND isFriends=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, requestId);
		prepStmt.setBoolean(2, true);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}
	
	
	// Is friends
	public Boolean isFriendsWithUserId(String userId1, String userId2) throws Exception {

		String selectStatement = "select * from friends where ((senderuserid=? OR receiverUserId = ?) AND (senderuserid=? OR receiverUserId = ?)) AND isFriends=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId1);
		prepStmt.setNString(2, userId1);
		prepStmt.setString(3, userId2);
		prepStmt.setNString(4, userId2);
		prepStmt.setBoolean(5, true);

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

	// Check request already exist
	private Boolean isAlreadyRequested(String userId, String friendId) throws Exception {

		String selectStatement = "select * from friends where (senderUserId = ? AND receiverUserId = ?) OR (senderUserId = ? AND receiverUserId = ?);";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, userId);
		prepStmt.setString(2, friendId);
		prepStmt.setString(3, friendId);
		prepStmt.setString(4, userId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}

	// Last friends ID
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

	// Create the Request
	private Pair<Integer, String> createRequest(String friendsId, String userId, String friendId) throws Exception {

		String sqlStatement = "insert into friends(friendsId, requestedDate, isDeleted, createdDate, updateDate, senderUserId, receiverUserId, isFriends) values (?, ?, ?, ? ,? ,? ,?, ?);";

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
			// TODO: If required can send email
			return new Pair<Integer, String>(200,
					ApiResponseHandler.apiResponse(ResponseType.SUCCESS, FriendRequestConstraints.friendsCreatedSuccessfully));
		} else {

			prepStmt.close();
			remove();
			return new Pair<Integer, String>(400,
					ApiResponseHandler.apiResponse(ResponseType.FAILURE, FriendRequestConstraints.friendsCreationFailed));
		}
	}

	// Create friends
	public Pair<Integer, String> createFriends(String userId, String friendId) {
		try {

			String friendsId = getLastFriendRequest();
			if (!friendsId.isEmpty()) {
				friendsId = Helper.nextId(friendsId, "FRI");

			} else {
				friendsId = FriendRequestConstraints.firstRequestId;
			}

			String sqlStatement = "insert into friends(friendsId, requestedDate, isDeleted, createdDate, updateDate, senderUserId, receiverUserId, isFriends) values (?, ?, ?, ? ,? ,? ,?, ?);";

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
			prepStmt.setBoolean(8, true);

			int x = prepStmt.executeUpdate();

			if (x == 1) {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						FriendRequestConstraints.friendsCreatedSuccessfully));
			} else {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
						FriendRequestConstraints.friendsCreationFailed));
			}

		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Accept friends
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

				// TODO: Can send the mail if required
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
