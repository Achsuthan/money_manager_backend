package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.InviteConstants;
import constants.SearchConstraints;
import constants.UserConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class FriendInvite extends DatabaseConnector {

	public FriendInvite() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	//Create invite starting point
	public Pair<Integer, String> createInvite(String email, String userId) {

		try {
			
			User user = new User();
			if (user.CheckUserExist(userId)) {

				if (user.checkEmailExisit(email)) {
					
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userExistForInvite));
				} else {
					return handleCreateInviteLink(email, userId);
				}
			} else {

				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	
	// Create invite details
	public Pair<Integer, String> getSingleFriendInviteDetails(String inviteId) {

		try {

			ResultSet rs = getSingleInvite(inviteId);
			
			if(rs.next()) {
				JSONObject obj = new JSONObject();
				obj.put("email", rs.getString("email"));
				obj.put("inviteId", rs.getString("inviteId"));
				
				remove();
				return new Pair<Integer, String>(200,
						ApiResponseHandler.apiResponse(ResponseType.SUCCESS, obj));
			}
			else {
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.iviteNotExist));
			}
		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//Database handler for create invite
	private Pair<Integer, String> handleCreateInviteLink(String email, String userId) {

		try {

			Pair<Integer, String> emailStatus = getInviteLink(email);

			switch (emailStatus.getKey()) {
			
			//Invite already exist
			case 200: {

				remove();
				JSONObject obj = new JSONObject();
				obj.put("link", emailStatus.getValue());
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						InviteConstants.inviteCreatedSuccessfully, obj));
			}
			
			//Server failure
			case 500: {

				remove();
				return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
			}
			
			//Invite is not exist
			case 400: {

				String lastId = getLastInvite();
				
				User user = new User();
				String requestUserName = "";
				JSONObject singleUser  = user.getSingleUserDetails(userId);
				if(singleUser.get("name") != null) {
					requestUserName = (String) singleUser.get("name");
				}

				if (!lastId.isEmpty()) {
					
					//First invite
					Pair<Integer, String> res = addInvite(Helper.nextId(lastId, "INV"), userId, email, requestUserName);
					remove();
					return res;
				} else {

					Pair<Integer, String> res = addInvite(InviteConstants.firstInviteId, userId, email, requestUserName);
					remove();
					return res;
				}
			}
			default:
				
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.EmailAlreadyExist));
			}
		} catch (Exception e) {
			
			//Exception handler
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//Delete invite
	public Pair<Integer, String> deleteInvite(String inviteId, String userId) {

		try {

			//User check
			User user = new User();
			if (user.CheckUserExist(userId)) {

				//Check invite 
				if (checkInviteIdExist(inviteId)) {

					if(checkUserExistForInvite(userId, inviteId)) {
						
						String selectStatement = "delete from invite where inviteId=?;";
						PreparedStatement prepStmt = con.prepareStatement(selectStatement);
						prepStmt.setString(1, inviteId);

						prepStmt.executeUpdate();
						
						//Invite deleted successfully
						return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
								InviteConstants.inviteDeletedSuccessfully));
					}
					else {
						
						//Invite deleted successfully
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								InviteConstants.noAccess));
					}
					
				} else {
					
					//Invite Id not found
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.iviteNotExist));
				}
			} else {
				
				//User not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}

		} catch (Exception e) {

			//Exception
			System.out.println("Yoo yooo" + e);
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	//user exist for the invite
	public Boolean checkUserExistForInvite(String userId, String inviteId) throws Exception {
		
		String selectStatement = "select * from invite where inviteId = ? AND userId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, inviteId);
		prepStmt.setString(2, userId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			return true;
		} else {
			return false;
		}
	}
	
	public Pair<Integer, String> getAllInvites(String userId) {
		
		try {

			//User check
			User user = new User();
			if (user.CheckUserExist(userId)) {
				
				
				String sqlStatement = "select * from invite where userId = ?;";
				
				PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
				
				prepStmt.setString(1, userId);

				ResultSet rs = prepStmt.executeQuery();

				JSONArray invitesArray = new JSONArray();

				while (rs.next()) {
					JSONObject singleInvite = new JSONObject();
					singleInvite.put("inviteId", rs.getString("inviteId"));
					singleInvite.put("email", rs.getString("email"));
					singleInvite.put("link", Helper.createInviteLink(rs.getString("inviteId")));
					invitesArray.put(singleInvite);
				}
				
				JSONObject obj = new JSONObject();
				obj.put("invites", invitesArray);
				remove();
				return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, SearchConstraints.success, obj));
				
			} else {
				
				//User not found
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}

		} catch (Exception e) {

			//Exception
			System.out.println("Yoo yooo" + e);
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//accept invite link
	public Pair<Integer, String> acceptInvite(String email, String name, String password, String inviteId) {

		try {
			
			if(!checkEmailAndInviteId(email, inviteId)) {
				
				remove();
				return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
						InviteConstants.emailOrInviteIdNotExist));
			}else {
				
				//Register user first in the system 
				User user = new User();
				Pair<Integer, String> registerOutput = user.register(email, name, password, false);
				
				if (registerOutput.getKey() == 200) {
					
					ResultSet singleInvite = getSingleInvite(inviteId);

					if (singleInvite.next()) {

						String userId = singleInvite.getString("userId");
						ResultSet singleUserDetails = user.getSingleUserDetailsByEmail(email);
						
						if (singleUserDetails.next()) {
							
							//Create the friends once the invite is accepted and created the account
							String friendId = singleUserDetails.getString("userId");
							FriendRequest friend = new FriendRequest();
							Pair<Integer, String> friedResult = friend.createFriends(userId, friendId);
							
							if (friedResult.getKey() == 200) {
								
								Pair<Integer, String> inviteResult = deleteInvite(inviteId, userId);
								
								if (inviteResult.getKey() == 200) {
									
									remove();
									return new Pair<Integer, String>(200, ApiResponseHandler
											.apiResponse(ResponseType.SUCCESS, UserConstants.userRegisterSuccessfully));
								} else {
									
									//Delete invite failed
									remove();
									return new Pair<Integer, String>(400, ApiResponseHandler
											.apiResponse(ResponseType.FAILURE, InviteConstants.acceptInviteFailed));
								}
								
							} else {
											
								//create friends failed
								remove();
								return friedResult;
							}
						} else {
							
							//Get single user get failed
							remove();
							return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
									InviteConstants.acceptInviteFailed));
						}
					} else {
						
						//Get single invite get failed
						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.acceptInviteFailed));
					}
				} else {
					
					//Register error message
					return registerOutput;
				}
			}

		} catch (Exception e) {

			//Exception
			System.out.println("Yoo yooo" + e);
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//Invite ID check
	public Boolean checkInviteIdExist(String inviteId) throws Exception {

		String selectStatement = "select * from invite where inviteId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, inviteId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			return true;
		} else {
			return false;
		}
	}
	
	//Check the email address is exist
	private Boolean checkEmailAndInviteId(String email, String inviteId) throws Exception {
		
		String selectStatement = "select * from invite where email = ? AND inviteId=? ;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, email);
		prepStmt.setString(2, inviteId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;

		} else {

			prepStmt.close();
			return false;
		}

	}
	
	//Get the invite link
	private Pair<Integer, String> getInviteLink(String email) throws Exception {
		
		String selectStatement = "select * from invite where email = ?;";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, email);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			Calendar cal = Calendar.getInstance();
			long timeNow = cal.getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
			java.sql.Timestamp expiryTs = rs.getTimestamp("expiryTime");

			String linkId = rs.getString("inviteId");

			if (expiryTs.compareTo(ts) > 0) {
				
				prepStmt.close();
				return new Pair<Integer, String>(200, Helper.createInviteLink(linkId));
			} else {
				
				prepStmt.close();
				if (updateExpiryDate(linkId)) {
					//TODO: need to send the actual invite link
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

	//Create invite 
	private Pair<Integer, String> addInvite(String inviteId, String userId, String email, String requestUserName) {

		try {

			String sqlStatement = "insert into invite(inviteId, email, expiryTime, isDeleted, createdDate, updateDate, userId) values (?, ?, ?, ? ,? ,? ,?);";

			Calendar cal = Calendar.getInstance();
			long timeNow = cal.getTimeInMillis();
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

			cal.add(Calendar.DAY_OF_MONTH, InviteConstants.linkExpiryDays);
			long expiryTime = cal.getTimeInMillis();
			java.sql.Timestamp expiryts = new java.sql.Timestamp(expiryTime);

			PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
			prepStmt.setString(1, inviteId);
			prepStmt.setString(2, email);
			prepStmt.setTimestamp(3, expiryts);
			prepStmt.setBoolean(4, false);
			prepStmt.setTimestamp(5, ts);
			prepStmt.setTimestamp(6, ts);
			prepStmt.setString(7, userId);


			int x = prepStmt.executeUpdate();

			if (x == 1) {

				//Invite create success
				prepStmt.close();
				ResultSet singleInvite = getSingleInvite(inviteId);

				if (singleInvite.next()) {

					//Get single invite success
					JSONObject obj = new JSONObject();
					String link = Helper.createInviteLink(singleInvite.getString("inviteId"));
					obj.put("Link", link);
					
					Helper.sendMail(requestUserName + " send you the invitation link to join to Money Manger \nlink: "+ InviteConstants.inviteBaseURL + link, "Mony Manager Invitation" , email);
					remove();
					return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
							InviteConstants.inviteCreatedSuccessfully, obj));

				} else {
					
					//Invite details not exits
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
				}
			} else {

				//Creation of invite failed
				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
			}
		} catch (Exception e) {

			//Exception handler
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//Get the last user id
	private String getLastInvite() throws Exception {
		
		String selectStatement = "select inviteId from invite ORDER BY inviteId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String inviteId = rs.getString("inviteId");
			prepStmt.close();
			return inviteId;
		} else {
			return "";
		}

	}

	//Single Invite
	private ResultSet getSingleInvite(String inviteId) throws Exception {

		String selectStatement = "select * from invite where inviteId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, inviteId);

		ResultSet rs = prepStmt.executeQuery();
		return rs;
	}

	//Update the invite expire time
	private Boolean updateExpiryDate(String inviteId) throws Exception {

		String sqlStatement = "update invite set expiryTime = ?, updateDate = ? where inviteId = ?;";

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

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			
			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}

}
