package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.InviteConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class GroupInvite extends DatabaseConnector {

	public GroupInvite() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	// Create group Invite link
	public Pair<Integer, String> createGroupInvite(String userId, Integer accessLevel, String reciverId,
			String groupId) {

		try {

			// User check
			User user = new User();
			if (user.CheckUserExist(userId)) {

				if (user.CheckUserExist(reciverId)) {

					Group group = new Group();
					if (group.checkGroupExist(groupId)) {

						FriendRequest friendRequest = new FriendRequest();
						if (friendRequest.isFriendsWithUserId(userId, reciverId)) {

							if (group.checkOwerner(userId, groupId)) {

								if (group.checkUserAlreadyHaveAccessToSameGroup(reciverId, groupId)) {

									// User already in the same group
									remove();
									return new Pair<Integer, String>(400, ApiResponseHandler
											.apiResponse(ResponseType.FAILURE, InviteConstants.userAlreadyInGroup));
								} else {

									// Handle invite link
									return handleCreateInviteLink(userId, reciverId, accessLevel, groupId);
								}
							} else {

								// User already in the same group
								remove();
								return new Pair<Integer, String>(400,
										ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.noAccess));
							}
						} else {

							// User already in the same group
							remove();
							return new Pair<Integer, String>(400,
									ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.noFriends));
						}

					} else {

						// Group not exist
						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.groupNotExist));
					}
				} else {

					// user not found
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.reciverUserNotFound));
				}

			} else {

				// user not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		} catch (Exception e) {

			System.out.println("error error " + e);
			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Create group Invite link
	/*
	 * 1. Check user 2. Check the groupInvite 3. User in the group invite 4. Check
	 * the expire time 5. Create the Access Level 6. Delete the group invite
	 */
	public Pair<Integer, String> acceptGroupInvite(String userId, String groupInviteId) {

		try {

			// User check
			User user = new User();
			if (user.CheckUserExist(userId)) {

				ResultSet result = getSingleInvite(groupInviteId);

				if (result.next()) {

					String receiverUserId = result.getString("reciveruserId");

					if (userId.equals(receiverUserId)) {

						Calendar cal = Calendar.getInstance();
						long timeNow = cal.getTimeInMillis();
						java.sql.Timestamp currentTime = new java.sql.Timestamp(timeNow);

						java.sql.Timestamp expiryTime = result.getTimestamp("expiryTime");

						if (expiryTime.compareTo(currentTime) > 0) {

							Integer accessLevel = result.getInt("accessLevel");
							String groupId = result.getString("inviteGroupId");

							if (deleteSingleInvite(groupInviteId)) {

								Group group = new Group();
								return group.createAccessLevel(userId, groupId, accessLevel, true);
							} else {

								// user not found
								remove();
								return new Pair<Integer, String>(400, ApiResponseHandler
										.apiResponse(ResponseType.FAILURE, InviteConstants.deleteGroupInviteFailed));
							}
						} else {

							// user not found
							remove();
							return new Pair<Integer, String>(400,
									ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.linkExpired));
						}

					} else {

						// user not found
						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.noAccess));
					}
				} else {

					// user not found
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.iviteNotExist));
				}

			} else {

				// user not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		} catch (Exception e) {

			System.out.println("error error " + e);
			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Create group
	public Pair<Integer, String> getAllGroupInviteByGroupId(String userId, String groupId) {

		try {

			// Check user
			User user = new User();
			if (user.CheckUserExist(userId)) {

				String selectStatement = "select * from group_invite where inviteGroupId=? AND userId=?;";

				PreparedStatement prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setNString(1, groupId);
				prepStmt.setNString(2, userId);

				ResultSet rs = prepStmt.executeQuery();

				JSONArray returnArray = new JSONArray();

				while (rs.next()) {

					JSONObject obj = new JSONObject();

					obj.put("groupInviteId", rs.getString("groupInviteId"));
					obj.put("inviteLink", Helper.createInviteLink(rs.getString("groupInviteId")));
					obj.put("receiver", user.getSingleUserDetails(rs.getString("reciveruserId")));
					returnArray.put(obj);
				}
				JSONObject obj = new JSONObject();
				obj.put("groupInvites", returnArray);

				remove();
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS, obj));

			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.userNotFound));
			}
		} catch (Exception e) {
			System.out.println("error errror" + e);
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	private Boolean deleteSingleInvite(String groupInviteId) throws Exception {

		String selectStatement = "delete from group_invite where groupInviteId=?;";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, groupInviteId);

		prepStmt.executeUpdate();

		return true;
	}

	public Pair<Integer, String> getAllGroupInivite(String userId) {
		try {

			// User check
			User user = new User();
			if (user.CheckUserExist(userId)) {
				JSONObject obj = new JSONObject();
				obj.put("sendRequest", getSendRequest(userId));
				obj.put("receiveRequest", getReceiveRequest(userId));

				remove();
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS, obj));

			} else {

				// user not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		} catch (Exception e) {

			System.out.println("error error " + e);
			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Get send request
	private JSONArray getSendRequest(String userId) throws Exception {

		JSONArray request = new JSONArray();

		String selectStatement = "select * from group_invite where userId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		prepStmt.setNString(1, userId);
		ResultSet rs = prepStmt.executeQuery();

		while (rs.next()) {

			String groupInviteId = rs.getString("groupInviteId");
			JSONObject obj = new JSONObject();
			obj.put("accessLevel", rs.getString("accessLevel"));
			obj.put("groupInviteId", groupInviteId);
			obj.put("inviteLink", Helper.createInviteLink(groupInviteId));
			obj.put("groupName", "");

			String gorupId = rs.getString("inviteGroupId");
			Group grp = new Group();
			ResultSet groupRs = grp.getsingleGroup(gorupId);
			if (groupRs.next()) {
				System.out.println(groupRs.getString("name"));
				obj.put("groupName", groupRs.getString("name"));
			}

			String receiverUserId = rs.getString("reciverUserId");
			User user = new User();
			obj.put("receiver", user.getSingleUserDetails(receiverUserId));

			request.put(obj);
		}
		return request;
	}

	// Get send request
	private JSONArray getReceiveRequest(String userId) throws Exception {

		JSONArray request = new JSONArray();

		String selectStatement = "select * from group_invite where reciverUserId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		prepStmt.setNString(1, userId);
		ResultSet rs = prepStmt.executeQuery();

		while (rs.next()) {

			String groupInviteId = rs.getString("groupInviteId");
			JSONObject obj = new JSONObject();
			obj.put("accessLevel", rs.getString("accessLevel"));
			obj.put("groupInviteId", groupInviteId);
			obj.put("inviteLink", Helper.createInviteLink(groupInviteId));
			obj.put("groupName", "");

			String gorupId = rs.getString("inviteGroupId");
			Group grp = new Group();
			ResultSet groupRs = grp.getsingleGroup(gorupId);
			if (groupRs.next()) {
				obj.put("groupName", groupRs.getString("name"));
			}

			User user = new User();
			obj.put("sender", user.getSingleUserDetails(rs.getString("userId")));
			request.put(obj);
		}
		return request;
	}

	// Handle invite link
	private Pair<Integer, String> handleCreateInviteLink(String userId, String reciverUserId, Integer accessLevel,
			String groupId) {

		try {

			Pair<Integer, String> emailStatus = checkReceiverExist(reciverUserId, groupId);

			switch (emailStatus.getKey()) {
			case 200: {

				// Already invite exist
				remove();
				JSONObject obj = new JSONObject();
				obj.put("link", emailStatus.getValue());
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						InviteConstants.inviteCreatedSuccessfully, obj));
			}
			case 500: {

				// Server error
				remove();
				return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
			}
			case 400: {

				// Invite is not found
				String lastId = getLastGroupInviteId();

				if (!lastId.isEmpty()) {

					// Add invite
					Pair<Integer, String> res = addInvite(Helper.nextId(lastId, "GIV"), userId, reciverUserId,
							accessLevel, groupId);
					remove();
					return res;
				} else {

					// New invite create
					Pair<Integer, String> res = addInvite(InviteConstants.firstGroupInviteId, userId, reciverUserId,
							accessLevel, groupId);
					remove();
					return res;
				}
			}
			default:

				// Default error
				return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
			}
		} catch (Exception e) {

			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Add invite
	private Pair<Integer, String> addInvite(String inviteId, String userId, String receiverUserId, Integer accessLevel,
			String groupId) {

		try {

			String sqlStatement = "insert into group_invite (groupInviteId, reciveruserId, expiryTime, isDeleted, createdDate, updatedDate, userId, accessLevel, inviteGroupId) values (?, ?, ?, ? ,? ,? ,?, ?, ?);";

			Calendar cal = Calendar.getInstance();
			long timeNow = cal.getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

			cal.add(Calendar.DAY_OF_MONTH, InviteConstants.linkExpiryDays);
			long expiryTime = cal.getTimeInMillis();
			java.sql.Timestamp expiryts = new java.sql.Timestamp(expiryTime);

			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			prepStmt.setString(1, inviteId);
			prepStmt.setString(2, receiverUserId);
			prepStmt.setTimestamp(3, expiryts);
			prepStmt.setBoolean(4, false);
			prepStmt.setTimestamp(5, ts);
			prepStmt.setTimestamp(6, ts);
			prepStmt.setString(7, userId);
			prepStmt.setInt(8, accessLevel);
			prepStmt.setString(9, groupId);

			int x = prepStmt.executeUpdate();

			if (x == 1) {

				prepStmt.close();
				ResultSet singleInvite = getSingleInvite(inviteId);

				if (singleInvite.next()) {

					JSONObject obj = new JSONObject();
					String link = Helper.createInviteLink(singleInvite.getString("groupInviteId"));
					obj.put("Link", link);

					// TODO: Can send the email here
					User user = new User();
					Group group = new Group();
					ResultSet singleGroup = group.getsingleGroup(groupId);
					String groupName = "";
					if (singleGroup.next()) {
						groupName = singleGroup.getNString("name");
					}

					JSONObject senderUser = user.getSingleUserDetails(userId);
					JSONObject receiverUser = user.getSingleUserDetails(receiverUserId);

					String senderUsername = "";
					String receiverEmail = "";

					if (senderUser.get("name") != null && receiverUser.get("email") != null) {

						senderUsername = (String) senderUser.get("name");
						receiverEmail = (String) receiverUser.get("email");
					}

					Helper.sendMail(
							senderUsername + " invite you to the " + groupName + " Group"
									+ "\n By clicking the link you can join to the group link: " + link,
							"Group Invite", receiverEmail);

					remove();
					return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
							InviteConstants.inviteCreatedSuccessfully, obj));

				} else {

					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
				}
			} else {

				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
			}
		} catch (Exception e) {

			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	// Check receiverId exist
	private Pair<Integer, String> checkReceiverExist(String reciverUserId, String groupId) throws Exception {

		String selectStatement = "select * from group_invite where reciveruserId = ? AND inviteGroupId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, reciverUserId);
		prepStmt.setString(2, groupId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			Calendar cal = Calendar.getInstance();
			long timeNow = cal.getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

			java.sql.Timestamp expiryTs = rs.getTimestamp("expiryTime");

			String linkId = rs.getString("groupInviteId");

			if (expiryTs.compareTo(ts) > 0) {

				prepStmt.close();
				return new Pair<Integer, String>(200, Helper.createInviteLink(linkId));
			} else {

				prepStmt.close();
				if (updateExpiryDate(linkId)) {

					return new Pair<Integer, String>(200, Helper.createInviteLink(linkId));
				} else {
					return new Pair<Integer, String>(500, "");
				}

			}

		} else {

			prepStmt.close();
			return new Pair<Integer, String>(400, "");
		}

	}

	// update expire time for link
	private Boolean updateExpiryDate(String inviteId) throws Exception {

		String sqlStatement = "update group_invite set expiryTime = ?, updatedDate = ? where groupInviteId = ?;";

		Calendar cal = Calendar.getInstance();
		long timeNow = cal.getTimeInMillis();
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		cal.add(Calendar.DAY_OF_MONTH, InviteConstants.linkExpiryDays);
		long expiryTime = cal.getTimeInMillis();
		java.sql.Timestamp expiryts = new java.sql.Timestamp(expiryTime);

		PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
		prepStmt.setTimestamp(1, expiryts);
		prepStmt.setTimestamp(2, ts);
		prepStmt.setString(3, inviteId);

		int x = prepStmt.executeUpdate();

		if (x == 1) {

			// Send email
			prepStmt.close();
			remove();
			return true;
		} else {

			prepStmt.close();
			remove();
			return false;
		}
	}

	// Get the last inviteId
	private String getLastGroupInviteId() throws Exception {
		String selectStatement = "select groupInviteId from group_invite ORDER BY groupInviteId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String groupInviteId = rs.getString("groupInviteId");
			prepStmt.close();
			return groupInviteId;
		} else {
			return "";
		}
	}

	// Get the single invite
	private ResultSet getSingleInvite(String inviteId) throws Exception {

		String selectStatement = "select * from group_invite where groupInviteId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, inviteId);

		ResultSet rs = prepStmt.executeQuery();
		return rs;
	}

	public Pair<Integer, String> deleteGroupInvite(String userId, String groupInviteId) throws Exception {

		try {

			// User check
			User user = new User();
			if (user.CheckUserExist(userId)) {

				ResultSet result = getSingleInvite(groupInviteId);

				if (result.next()) {

					String receiverUserId = result.getString("reciveruserId");
					String senderId = result.getString("userId");

					if (userId.equals(receiverUserId) || userId.equals(senderId)) {

						deleteSingleInvite(groupInviteId);
						remove();
						return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
								InviteConstants.inviteDeletedSuccess));

					} else {

						// user not found
						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.noAccess));
					}
				} else {

					// user not found
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.iviteNotExist));
				}

			} else {

				// user not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		} catch (Exception e) {

			System.out.println("error error " + e);
			// Exception
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

}