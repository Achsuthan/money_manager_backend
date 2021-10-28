package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.TransactionConstrains;
import models.Category;
import models.Transaction;
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
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.noOwnTransaction));
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

					System.out.println("Error error "+ e);
					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				} catch (ClassCastException ee) {

					System.out.println("Error error 2"+ ee);
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
				addSingleSharedTransaction(friendsArray.getJSONObject(i), transactionId, userId, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
			}
			
			remove();
			return new Pair<Integer, String>(200,
					ApiResponseHandler.apiResponse(ResponseType.SUCCESS, TransactionConstrains.transactionCreatedSuccessfuly));
		}
		else {
			return result;
		}
	}
	
	public String addSingleSharedTransaction(JSONObject singleFriend, String transactionId ,String userId,  String transactionName, Double amount, String description, Date date, String transacionTo, String transactionType, String categoryId) throws Exception {
		
		String sharedTransactionId = getLastTransactionId();
		if(sharedTransactionId.equals("")) {
			
			sharedTransactionId = TransactionConstrains.firstSharedTransactionId;
		}
		else {
			sharedTransactionId = Helper.nextId(sharedTransactionId, "SHT");
		}
		
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
		prepStmt.close();
		return sharedTransactionId;
		
	}
	
	public Pair<Integer, String> getUsersByTransactionsId(String userId, String TransacitonId) {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {

				String selectStatement = "select transaction.amount, shared_transaction.persentage, shared_transaction.receiverUserId as userId, user.email, user.name as userName from transaction inner join shared_transaction  on (shared_transaction.transactionId = transaction.transactionId AND shared_transaction.transactionId=? AND transaction.transactionTo='friend') inner join user on(user.userId = shared_transaction.receiverUserId);";

				PreparedStatement prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setNString(1, TransacitonId);

				ResultSet rs = prepStmt.executeQuery();
				
				JSONArray returnArray = new JSONArray();
				
				while (rs.next()) {
					
					JSONObject obj = new JSONObject();
					
					Double persentage = rs.getDouble("persentage");
					Double amount = rs.getDouble("amount");
					amount = amount < 0 ? -1 * amount : amount;
					
					obj.put("userId", rs.getString("userId"));
					obj.put("email", rs.getString("email"));
					obj.put("userName", rs.getString("userName"));
					obj.put("persentage", persentage);
					obj.put("amount", persentage * amount/ 100);
					returnArray.put(obj);
				}
				JSONObject obj = new JSONObject();
				obj.put("users", returnArray);
				
				remove();
				return new Pair<Integer, String>(200,
						ApiResponseHandler.apiResponse(ResponseType.SUCCESS, obj));
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
	
	public Pair<Integer, String> getAllSharedTransactions(String userId) throws Exception {
		
		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {
				
				String selectStatement = "select transaction.transactionId as transactionId, transaction.name as transactionName, transaction.description, transaction.amount, transaction.date, transaction.categoryId, transaction.userId as senderId, transaction.transactionTo, transaction.transactionType, shared_transaction.persentage, shared_transaction.receiverUserId as userId, shared_transaction.sennderUserId as senderUserId, user.email, user.name as userName, category.categoryName, category.imageName, category.color from transaction inner join shared_transaction on (shared_transaction.transactionId = transaction.transactionId AND (shared_transaction.sennderUserId=? || shared_transaction.receiverUserId=?)AND transaction.transactionTo='friend') inner join category on(category.categoryId = transaction.categoryId) inner join user on(user.userId = if (shared_transaction.receiverUserId = ?, shared_transaction.sennderUserId, shared_transaction.receiverUserId)) order by transaction.createdDate desc;"
						+ "";

				PreparedStatement prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setNString(1, userId);
				prepStmt.setNString(2, userId);
				prepStmt.setNString(3, userId);

				ResultSet rs = prepStmt.executeQuery();
				
				Double youSpent = 0.0;
				Double youReceived = 0.0; 
				
				models.SharedTransaction sharedTransaction = new models.SharedTransaction();
				
				while (rs.next()) {
					Double persentage = rs.getDouble("persentage");
					Double amount = rs.getDouble("amount");
					String senderId = rs.getString("senderUserId");
					amount = amount < 0 ? -1 * amount : amount;
					
					if(senderId.equals(userId)) {
						youSpent += persentage * amount/ 100;
					}
					else {
						youReceived += persentage * amount/ 100;
					}
					
					models.User friend = new models.User();
					friend.setUserId(senderId.equals(userId) ? rs.getString("userId"): senderId);
					friend.setEmail(rs.getString("email"));
					friend.setName(rs.getString("userName"));
					friend.setPersentage(persentage);
					friend.setAmount(persentage * amount/ 100);
					
					Category cat = new Category();
					cat.setCategoryId(rs.getString("categoryId"));
					cat.setCategoryName(rs.getString("categoryName"));
					cat.setColor(rs.getString("color"));
					cat.setImageName(rs.getString("imageName"));
					
					Transaction trans = new Transaction();
					trans.setCategory(cat);
					
					trans.setAmount(amount);
					trans.setDate(rs.getDate("date"));
					trans.setDescription(rs.getString("description"));
					trans.setIsOwn(senderId.equals(userId) ? true : false);
					trans.setTransactionId(rs.getString("transactionId"));
					trans.setTransactionName(rs.getNString("transactionName"));
					
					
					sharedTransaction.addTransaction(trans, friend);
				}
				
				JSONObject obj = new JSONObject();
				obj.put("transactions", sharedTransaction.getSharedTransactionObject());
				obj.put("youSpent", youSpent);
				obj.put("youReceive", youReceived);
				return new Pair<Integer, String>(200,
						ApiResponseHandler.apiResponse(ResponseType.SUCCESS, TransactionConstrains.transactionCreatedSuccessfuly, obj));
				
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

}
