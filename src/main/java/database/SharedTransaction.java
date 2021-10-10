package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.TransactionConstrains;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class SharedTransaction extends DatabaseConnector {

	public SharedTransaction() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	public Pair<Integer, String> handleSharedTransaction(String userId, JSONArray friendsArray,  String transactionName, Double amount, String description, Date date, String transacionTo, String transactionType, String categoryId) {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {
				try {
					JSONArray friendsDetailsArray  = friendsArray;
					Boolean isFriends = true;
					Boolean isOwn = false;
					Double totalPersentage = 0.0;
					for (int i = 0; i < friendsDetailsArray.length(); i++) {
						JSONObject singleFriend = friendsDetailsArray.getJSONObject(i);
						totalPersentage += singleFriend.getDouble("persentage");
						String friendId = singleFriend.getString("friendId");
						
						if(friendId.equals(userId)) {
							isOwn = true;
						}
						
						FriendRequest friendRequest = new FriendRequest();
						if(!friendRequest.isFriendsWithUserId(userId, friendId)) {
							isFriends = false;
							break;
						}
					}
					
					if(totalPersentage <= 0 || totalPersentage >100) {
						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.splitPersentageWrong));
					}
					else if (isOwn) {
						
						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
					}
					else if (!isFriends) {
						
						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.notFriends));
					}
					else {
						
						if(totalPersentage != 100) {
							JSONObject userObject = new JSONObject();
							userObject.put("friendId", userId);
							userObject.put("persentage", (double)(100 - totalPersentage));
							friendsDetailsArray.put(userObject);
						}
						
						return addSharedTransaction(userId, friendsDetailsArray, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
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
	 */
	private Pair<Integer, String> addSharedTransaction(String userId ,JSONArray friendsArray,  String transactionName, Double amount, String description, Date date, String transacionTo, String transactionType, String categoryId) throws Exception {
		
		SingleTransaction singleTransaction = new SingleTransaction();
		Pair<Integer, String> result = singleTransaction.addTransaction(userId, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
		
		if(result.getKey() == 200) {
			
			
			
			JSONObject transactionObject = new JSONObject(result.getValue());
			
			String transactionId = (String) ((JSONObject) transactionObject.get("body")).getString("transactionId");
			
			for (int i = 0; i < friendsArray.length(); i++) {
				
				String sharedTransactionId = getLastTransactionId();
				if(sharedTransactionId.equals("")) {
					
					sharedTransactionId = TransactionConstrains.firstSharedTransactionId;
				}
				else {
					sharedTransactionId = Helper.nextId(sharedTransactionId, "SHT");
				}
				
				JSONObject singleFriend = friendsArray.getJSONObject(i);
				
				String friendId = singleFriend.getString("friendId");
				
				String sqlStatement = "insert into shared_transaction(sharedTransactionId, persentage, createdDate, updatedDate, transactionId, sennderUserId, receiverUserId) values (?, ?, ? ,? ,?, ?, ?);";

				Calendar cal = Calendar.getInstance();
				long timeNow = cal.getTimeInMillis();
				java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

				PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
				prepStmt.setString(1, sharedTransactionId);
				prepStmt.setDouble(2, singleFriend.getDouble("persentage"));
				prepStmt.setTimestamp(3, ts);
				prepStmt.setTimestamp(4, ts);
				prepStmt.setString(5, transactionId);
				prepStmt.setString(6, userId);
				prepStmt.setString(7, friendId);
				
				int x = prepStmt.executeUpdate();

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

		String selectStatement = "select sharedTransactionId from shared_transaction ORDER BY sharedTransactionId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String transactionId = rs.getString("sharedTransactionId");
			prepStmt.close();
			return transactionId;
		} else {
			return "";
		}
	}

}
