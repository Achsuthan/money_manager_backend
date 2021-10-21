package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.TransactionConstrains;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class GroupTransaction extends DatabaseConnector {

	public GroupTransaction() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * 1. Check the percentage
	 * 2. Check all the people are friends 
	 * 3. Check everyone is in the same group
	 */
	public Pair<Integer, String> handleGroupTransaction(String userId, String groupId ,JSONArray friendsArray, String transactionName,
			Double amount, String description, Date date, String transacionTo, String transactionType,
			String categoryId) {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {
				try {
					JSONArray friendsDetailsArray = friendsArray;
					Boolean isFriends = true;
					Boolean isOwn = false;
					Boolean isFriendsInGroup = true;
					Double totalPersentage = 0.0;
					for (int i = 0; i < friendsDetailsArray.length(); i++) {
						JSONObject singleFriend = friendsDetailsArray.getJSONObject(i);
						totalPersentage += singleFriend.getDouble("persentage");
						String friendId = singleFriend.getString("friendId");

						if (friendId.equals(userId)) {
							isOwn = true;
							break;
						}

						FriendRequest friendRequest = new FriendRequest();
						if (!friendRequest.isFriendsWithUserId(userId, friendId)) {
							isFriends = false;
							break;
						}
						
						Group group = new Group();
						if (!group.checkUserAlreadyHaveAccessToSameGroup(friendId, groupId)) {
							isFriendsInGroup = false;
							break;
						}
						
					}

					if (totalPersentage <= 0 || totalPersentage > 100) {
						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								TransactionConstrains.splitPersentageWrong));
					} else if (isOwn) {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.noOwnTransaction));
					} else if (!isFriends) {

						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.notFriends));
					} else if (!isFriendsInGroup) {
						
						remove();
						return new Pair<Integer, String>(400,
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.oneOrMoreUserNotInGroup));
					}
					else {

						if (totalPersentage != 100) {
							JSONObject userObject = new JSONObject();
							userObject.put("friendId", userId);
							userObject.put("persentage", (double) (100 - totalPersentage));
							friendsDetailsArray.put(userObject);
						}

						return addGroupTransaction(userId, groupId ,friendsDetailsArray, transactionName, amount, description,
								date, transacionTo, transactionType, categoryId);
					}
				} catch (JSONException e) {

					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				} catch (ClassCastException ee) {

					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}
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
	
	/*
	 * 1. add the transaction 
	 * 2. Add the shared transaction 
	 * 3. Add the group transaction 
	 */
	private Pair<Integer, String> addGroupTransaction(String userId, String groupId ,JSONArray friendsArray,  String transactionName, Double amount, String description, Date date, String transacionTo, String transactionType, String categoryId) throws Exception {
		
		SingleTransaction singleTransaction = new SingleTransaction();
		Pair<Integer, String> result = singleTransaction.addTransaction(userId, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
		
		if(result.getKey() == 200) {
			
			
			JSONObject transactionObject = new JSONObject(result.getValue());
			
			String transactionId = (String) ((JSONObject) transactionObject.get("body")).getString("transactionId");
			
			for (int i = 0; i < friendsArray.length(); i++) {
				SharedTransaction sharedTransaction = new SharedTransaction();
				String singleSharedTransaction = sharedTransaction.addSingleSharedTransaction(friendsArray.getJSONObject(i), transactionId, userId, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
				System.out.println(singleSharedTransaction);
				String groupTransactionId = getLastTransactionId();
				if(groupTransactionId.equals("")) {
					
					groupTransactionId = TransactionConstrains.firstGroupTransactionId;
				}
				else {
					groupTransactionId = Helper.nextId(groupTransactionId, "SHT");
				}
				
				String sqlStatement = "insert into group_transaction(groupTransactionId, isDeleted, createdDate, updatedDate, userId, transactionId, groupId, sharedTransactionId) values (?, ?, ? ,? ,?, ?, ?, ?);";

				Calendar cal = Calendar.getInstance();
				long timeNow = cal.getTimeInMillis();
				java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

				PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
				prepStmt.setString(1, groupTransactionId);
				prepStmt.setBoolean(2, false);
				prepStmt.setTimestamp(3, ts);
				prepStmt.setTimestamp(4, ts);
				prepStmt.setString(5, userId);
				prepStmt.setString(6, transactionId);
				prepStmt.setString(7, groupId);
				prepStmt.setString(8, singleSharedTransaction);
				
				int x = prepStmt.executeUpdate();
				prepStmt.close();
				
			}
			
			remove();
			return new Pair<Integer, String>(200,
					ApiResponseHandler.apiResponse(ResponseType.SUCCESS, TransactionConstrains.transactionCreatedSuccessfuly));
		}
		else {
			return result;
		}
	}
	
	// Get Last transaction based on ID
		private String getLastTransactionId() throws Exception {

			String selectStatement = "select groupTransactionId from group_transaction ORDER BY groupTransactionId DESC LIMIT 1;";

			PreparedStatement prepStmt = con.prepareStatement(selectStatement);

			ResultSet rs = prepStmt.executeQuery();

			if (rs.next()) {

				String transactionId = rs.getString("groupTransactionId");
				prepStmt.close();
				return transactionId;
			} else {
				return "";
			}
		}

}
