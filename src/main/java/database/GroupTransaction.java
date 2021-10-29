package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.InviteConstants;
import constants.TransactionConstrains;
import models.Category;
import models.Transaction;
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
	 * 1. Check the percentage 2. Check all the people are friends 3. Check everyone
	 * is in the same group
	 */
	public Pair<Integer, String> handleGroupTransaction(String userId, String groupId, JSONArray friendsArray,
			String transactionName, Double amount, String description, Date date, String transacionTo,
			String transactionType, String categoryId) {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {
				
				Group group = new Group();
				if (!group.checkUserAlreadyHaveAccessToSameGroup(userId, groupId)) {
					
					remove();
					return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
							InviteConstants.noAccess));
				}else {
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
							return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING,
									TransactionConstrains.noOwnTransaction));
						} else if (!isFriends) {

							remove();
							return new Pair<Integer, String>(400,
									ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.notFriends));
						} else if (!isFriendsInGroup) {

							remove();
							return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE,
									TransactionConstrains.oneOrMoreUserNotInGroup));
						} else {

							if (totalPersentage != 100) {
								JSONObject userObject = new JSONObject();
								userObject.put("friendId", userId);
								userObject.put("persentage", (double) (100 - totalPersentage));
								friendsDetailsArray.put(userObject);
							}

							return addGroupTransaction(userId, groupId, friendsDetailsArray, transactionName, amount,
									description, date, transacionTo, transactionType, categoryId);
						}
					} catch (JSONException e) {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
					} catch (ClassCastException ee) {

						remove();
						return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
					}
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
	 * 1. add the transaction 2. Add the shared transaction 3. Add the group
	 * transaction
	 */
	private Pair<Integer, String> addGroupTransaction(String userId, String groupId, JSONArray friendsArray,
			String transactionName, Double amount, String description, Date date, String transacionTo,
			String transactionType, String categoryId) throws Exception {

		SingleTransaction singleTransaction = new SingleTransaction();
		Pair<Integer, String> result = singleTransaction.addTransaction(userId, transactionName, amount, description,
				date, transacionTo, transactionType, categoryId);

		if (result.getKey() == 200) {

			JSONObject transactionObject = new JSONObject(result.getValue());

			String transactionId = (String) ((JSONObject) transactionObject.get("body")).getString("transactionId");

			for (int i = 0; i < friendsArray.length(); i++) {
				SharedTransaction sharedTransaction = new SharedTransaction();
				String singleSharedTransaction = sharedTransaction.addSingleSharedTransaction(
						friendsArray.getJSONObject(i), transactionId, userId, transactionName, amount, description,
						date, transacionTo, transactionType, categoryId);
				System.out.println(singleSharedTransaction);
				String groupTransactionId = getLastTransactionId();
				if (groupTransactionId.equals("")) {

					groupTransactionId = TransactionConstrains.firstGroupTransactionId;
				} else {
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
			return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
					TransactionConstrains.transactionCreatedSuccessfuly));
		} else {
			return result;
		}
	}
	
	

	public Pair<Integer, String> getUsersByTransactionsIdGroupId(String userId, String TransacitonId, String groupId) {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {

				String selectStatement = "select transaction.amount, shared_transaction.persentage, shared_transaction.receiverUserId as userId, user.email, user.name as userName from transaction inner join shared_transaction  on (shared_transaction.transactionId = transaction.transactionId  AND shared_transaction.transactionId=? AND transaction.transactionTo='group') inner join group_transaction on (shared_transaction.sharedTransactionId = group_transaction.sharedTransactionId) inner join spending_group on (spending_group.groupId = group_transaction.groupId AND spending_group.groupId=?) inner join user on(user.userId = shared_transaction.receiverUserId);";

				PreparedStatement prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setNString(1, TransacitonId);
				prepStmt.setNString(2, groupId);

				ResultSet rs = prepStmt.executeQuery();
				
				models.SharedTransaction sharedTransaction = new models.SharedTransaction();
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

	public Pair<Integer, String> getAllGroupTransactions(String userId, String groupId) throws Exception {

		try {
			User user = new User();
			if (user.CheckUserExist(userId)) {
				
				String selectStatement = "select transaction.transactionId as transactionId, transaction.name as transactionName, transaction.description, transaction.amount, transaction.date, transaction.categoryId, transaction.userId as senderId, transaction.transactionTo, transaction.transactionType, shared_transaction.persentage, shared_transaction.receiverUserId as userId, shared_transaction.sennderUserId as senderUserId, user.email, user.name as userName, category.categoryName, category.imageName, category.color, spending_group.name as groupName, spending_group.groupId from transaction inner join shared_transaction on (shared_transaction.transactionId = transaction.transactionId AND (shared_transaction.sennderUserId=? || shared_transaction.receiverUserId=?)AND transaction.transactionTo='group') inner join group_transaction on (shared_transaction.sharedTransactionId = group_transaction.sharedTransactionId) inner join spending_group on (spending_group.groupId = group_transaction.groupId AND group_transaction.groupId=?) inner join category on(category.categoryId = transaction.categoryId) inner join user on(user.userId = if (shared_transaction.receiverUserId = ?, shared_transaction.sennderUserId, shared_transaction.receiverUserId)) order by transaction.createdDate desc;";

				PreparedStatement prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setNString(1, userId);
				prepStmt.setNString(2, userId);
				prepStmt.setNString(3, groupId);
				prepStmt.setNString(4, userId);

				ResultSet rs = prepStmt.executeQuery();

				models.SharedTransaction sharedTransaction = new models.SharedTransaction();
				
				Double youSpent = 0.0;
				Double youReceived = 0.0; 

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
					friend.setUserId(senderId.equals(userId) ? rs.getString("userId") : senderId);
					friend.setEmail(rs.getString("email"));
					friend.setName(rs.getString("userName"));
					friend.setPersentage(persentage);
					friend.setAmount(persentage * amount / 100);

					Category cat = new Category();
					cat.setCategoryId(rs.getString("categoryId"));
					cat.setCategoryName(rs.getString("categoryName"));
					cat.setColor(rs.getString("color"));
					cat.setImageName(rs.getString("imageName"));

					models.Group grp = new models.Group();
					grp.setGroupId(rs.getString("groupId"));
					grp.setGroupName(rs.getString("groupName"));

					Transaction trans = new Transaction();
					trans.setCategory(cat);

					trans.setAmount(amount);
					trans.setDate(rs.getDate("date"));
					trans.setDescription(rs.getString("description"));
					trans.setIsOwn(senderId.equals(userId) ? true : false);
					trans.setTransactionId(rs.getString("transactionId"));
					trans.setTransactionName(rs.getNString("transactionName"));
					trans.setGroup(grp);

					sharedTransaction.addTransaction(trans, friend);
				}

				JSONObject obj = new JSONObject();
				obj.put("transactions", sharedTransaction.getSharedTransactionObject());
				
				obj.put("youSpent", youSpent);
				obj.put("youReceive", youReceived);
				
				String groupName = "";
				String groupUserId = "";
				Group grp = new Group();
				ResultSet grpRs = grp.getsingleGroup(groupId);
				if(grpRs.next()) {
					groupName = grpRs.getString("name");
					groupUserId = grpRs.getString("createrId");
				}
				
				obj.put("groupName", groupName);
				obj.put("groupOwnerId", groupUserId);
				
				
				return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
						TransactionConstrains.transactionCreatedSuccessfuly, obj));
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
