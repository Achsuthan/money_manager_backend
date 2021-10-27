package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.GroupConstraints;
import constants.TransactionConstrains;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class SingleTransaction extends DatabaseConnector {

	public SingleTransaction() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Pair<Integer, String> createTransaction(String userId, String transactionName, Double amount, String description, Date date, String transacionTo, String transactionType, String categoryId) {
		try {
			
			User user = new User();
			if(user.CheckUserExist(userId)) {
				
				Category category = new Category();
				if(category.isCategoryExist(categoryId)) {
					
					return addTransaction(userId, transactionName, amount, description, date, transacionTo, transactionType, categoryId);
				}else {
					
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, TransactionConstrains.categoryNotExist));
				}
				
			}else {
				
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.userNotFound));
			}
		}
		catch (Exception e) {
			
			System.out.println("error errror" + e);
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	public Pair<Integer, String> getOwnTransaction(String userId, String transactionType) {
		try {
			
			User user = new User();
			if(user.CheckUserExist(userId)) {
				
				return getAllOwnTransaction(userId, transactionType);
				
				
			}else {
				
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.userNotFound));
			}
		}
		catch (Exception e) {
			
			System.out.println("error errror" + e);
			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	public Pair<Integer, String> addTransaction(String userId,String transactionName, Double amount, 
			String description, Date date, String transacionTo, String transactionType, String categoryId) throws Exception{
		
		String transacitonId = getLastTransactionId();
		
		Double transactionAmount = 0.0;
		String transactionTypeUpperCase = transactionType.toUpperCase();
		if(transactionTypeUpperCase.equals(TransactionConstrains.expenses) || transactionTypeUpperCase.equals(TransactionConstrains.transfer)) {
			transactionAmount = -amount;
		}
		
		if(transacitonId.equals("")) {
			transacitonId = TransactionConstrains.firstTransactionId;
		}
		else {
			transacitonId = Helper.nextId(transacitonId, "TRA");
		}
		
		String sqlStatement = "insert into transaction(transactionId, name, description, amount, date, createdDate, updatedDate, categoryId, userId, transactionTo, transactionType) values (?, ?, ? ,? ,?, ?, ?, ?, ?, ?, ?);";

		Calendar cal = Calendar.getInstance();
		long timeNow = cal.getTimeInMillis();
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
		prepStmt.setString(1, transacitonId);
		prepStmt.setString(2, transactionName);
		prepStmt.setString(3, description);
		prepStmt.setDouble(4, transactionAmount);
		prepStmt.setDate(5, new java.sql.Date(date.getTime()));
		
		prepStmt.setTimestamp(6, ts);
		prepStmt.setTimestamp(7, ts);
		prepStmt.setString(8, categoryId);
		prepStmt.setString(9, userId);
		prepStmt.setString(10, transacionTo);
		prepStmt.setString(11, transactionType);

		int x = prepStmt.executeUpdate();

		if (x == 1) {

			prepStmt.close();
			remove();
			JSONObject obj = new JSONObject();
			obj.put("transactionId", transacitonId);
			return new Pair<Integer, String>(200,
					ApiResponseHandler.apiResponse(ResponseType.SUCCESS, TransactionConstrains.transactionCreatedSuccessfuly, obj));
		} else {

			prepStmt.close();
			remove();
			return new Pair<Integer, String>(400,
					ApiResponseHandler.apiResponse(ResponseType.FAILURE, GroupConstraints.failed));
		}
	}

	// Get Last transaction based on ID
	private String getLastTransactionId() throws Exception {

		String selectStatement = "select transactionId from transaction ORDER BY transactionId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String transactionId = rs.getString("transactionId");
			prepStmt.close();
			return transactionId;
		} else {
			return "";
		}
	}

	// get the single transaction based on the transactionId
	private Boolean isTransactionExist(String transactionId) throws Exception {

		String selectStatement = "select * from transaction where transactionId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, transactionId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			return true;
		}
		return false;

	}

	// get the single transaction based on the transactionId
	public ResultSet getSingleTransactionBasedOnTransactionId(String transactionId) throws Exception {

		String selectStatement = "select * from transaction where transactionId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, transactionId);

		ResultSet rs = prepStmt.executeQuery();

		return rs;

	}

	// Check the transaction for that user
	public Boolean isOwnTransaction(String userId, String transactionId) throws Exception {

		String selectStatement = "select * from transaction where transactionId=? AND userId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, transactionId);
		prepStmt.setNString(2, userId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {
			return true;
		}

		return false;
	}

	// Get all the user's transaction
	public ResultSet getAllTransactionBasedOnUser(String userId) throws Exception {

		String selectStatement = "select * from transaction where userId=?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, userId);

		ResultSet rs = prepStmt.executeQuery();
		
		return rs;
	}
	
	private Pair<Integer, String> getAllOwnTransaction(String userId, String type) throws Exception {
		
		String selectStatement = "select * from transaction inner join category on(category.categoryId = transaction.categoryId) where transaction.userId= ? AND transaction.transactionTo=? order by transaction.date DESC;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setNString(1, userId);
		prepStmt.setString(2, type);

		ResultSet rs = prepStmt.executeQuery();
		JSONArray returnResult = new JSONArray();
		Double income = 0.0;
		Double expenses = 0.0;
		
		while(rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("transactionId", rs.getString("transactionId"));
			obj.put("transactionName", rs.getString("name"));
			obj.put("description", rs.getString("description"));
			obj.put("date", rs.getDate("date"));
			obj.put("transactionTo", rs.getString("transactionTo"));
			obj.put("transactionType", rs.getString("transactionType"));
			obj.put("amount", rs.getDouble("amount") < 0 ? -1 * rs.getDouble("amount") : rs.getDouble("amount"));
			obj.put("isOwn", true);
			
			String transferType = rs.getString("transactionType").toLowerCase();
			if(transferType.equals("income")) {
				income += rs.getDouble("amount") < 0 ? -1 * rs.getDouble("amount") : rs.getDouble("amount");
			}
			else {
				expenses += rs.getDouble("amount") < 0 ? -1 * rs.getDouble("amount") : rs.getDouble("amount");
			}
			
			JSONObject cat = new JSONObject();
			cat.put("categoryId", rs.getString("categoryId"));
			cat.put("categoryName", rs.getString("categoryName"));
			cat.put("imageName", rs.getString("imageName"));
			cat.put("color", rs.getString("color"));
			
			obj.put("category", cat);
			
			
			returnResult.put(obj);
		}
		
		JSONObject obj = new JSONObject();
		obj.put("transactions", returnResult);
		obj.put("income", income);
		obj.put("expenses", expenses);
		remove();
		return new Pair<Integer, String>(200,
				ApiResponseHandler.apiResponse(ResponseType.SUCCESS, obj));
	}

}
