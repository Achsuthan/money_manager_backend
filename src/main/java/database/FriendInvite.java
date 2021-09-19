package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import constants.InviteConstants;
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
	
	public Pair<Integer, String> createInvite(String email, String userId){
		
		try {
			User user = new User();
			
			if(user.CheckUserExist(userId)) {
				
				System.out.println("user exist");
				
				if (user.checkEmailExisit(email)) {
					return handleCreateInviteLink(email, userId);
				}
				else {
					return handleCreateInviteLink(email, userId);
				}
			}
			else {
				
				remove();
	            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
		}
		catch(Exception e) {
			
			remove();
            return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	private Pair<Integer, String> handleCreateInviteLink(String email, String userId){
		
		try {
			System.out.println("email exist");
			
			
			Pair<Integer, String> emailStatus = checkEmailExist(email);
			System.out.println("status "+ emailStatus.getKey());
			
			switch (emailStatus.getKey()) {
			case 200: {
				
				remove();
				JSONObject obj = new JSONObject();
				obj.put("link", emailStatus.getValue());
	            return new Pair<Integer, String>(200 ,ApiResponseHandler.apiResponse(ResponseType.SUCCESS,  InviteConstants.inviteCreatedSuccessfully ,obj));
			}
			case 500: {
				
				remove();
				return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
			}
			case 400: {
				
				String lastId = getLastInvite();
				
				System.out.println("last id " + lastId);
				
				if(!lastId.isEmpty()) {
					
					System.out.println("last id available");
					
					Pair<Integer, String> res = addInvite(Helper.nextId(lastId, "INV"), userId ,email);
					remove();
					return res;
				}
				else {
					
					System.out.println("last id not available");
					Pair<Integer, String> res = addInvite(InviteConstants.firstInviteId, userId ,email) ;
					remove();
					return res;
				}
			}
			default:
				return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.EmailAlreadyExist));
			}
		}
		catch (Exception e) {
			remove();
            return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
	public Pair<Integer, String> deleteInvite(String inviteId, String userId) {
		
		try {
			
			User user = new User();
			if(user.CheckUserExist(userId)) {
				
				if(checkInviteIdExist(inviteId)) {
					
					String selectStatement = "delete from invite where inviteId=?;";
			         
			         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
			         prepStmt.setString(1, inviteId);
			         
			         prepStmt.executeUpdate();
			         
			         return new Pair<Integer, String>(200 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteDeletedSuccessfully));
				}
				else {
					remove();
		            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.iviteNotExist));
				}
			}
			else {
				remove();
	            return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.userNotExist));
			}
			 
		 }
		 catch(Exception e) {
			 
			 System.out.println("Yoo yooo"+ e);
			 return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		 }
	}
	
	public Pair<Integer, String> acceptInvite(String email, String name, String password, String inviteId) {
		
		try {
			
			User user = new User();
			Pair<Integer, String> registerOutput = user.register(email, name, password, false);
			if(registerOutput.getKey() == 200) {
				ResultSet singleInvite = getSingleInvite(inviteId);
				
				if (singleInvite.next()) {
		        	 
		        	 String userId = singleInvite.getString("userId");
		        	 
		        	 ResultSet singleUserDetails = user.getSingleUserDetailsByEmail(email);
		        	 if(singleUserDetails.next()) {
		        		 String friendId = singleUserDetails.getString("userId");
		        		 FriendRequest friend = new FriendRequest();
		        		 Pair<Integer, String> friedResult= friend.createFriends(userId, friendId);
		        		 if(friedResult.getKey() == 200) {
		        			 FriendInvite invite = new FriendInvite();
		        			 Pair<Integer, String> inviteResult = invite.deleteInvite(inviteId, friendId);
		        			 if(inviteResult.getKey() == 200 ) {
		        				 return new Pair<Integer, String>(200,
		        							ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.userRegisterSuccessfully));
		        			 }
		        			 else {
		        				 return new Pair<Integer, String>(400 , ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.acceptInviteFailed));
		        			 }
		        		 }
		        		 else {
		        			 remove();
		        			 return friedResult;
		        		 }
		        	 }
		        	 else {
		        		 remove();
			        	 return new Pair<Integer, String>(400 , ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.acceptInviteFailed));
		        	 }
		         }
		         else {
		        	 remove();
		        	 return new Pair<Integer, String>(400 , ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.acceptInviteFailed));
		         }
			}
			else {
				return registerOutput;
			}
			 
		 }
		 catch(Exception e) {
			 
			 System.out.println("Yoo yooo"+ e);
			 return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		 }
	}
	
	public Boolean checkInviteIdExist(String inviteId) throws Exception{

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
	
	private Pair<Integer, String> checkEmailExist(String email) {
		
		try {
			 String selectStatement = "select * from invite where email = ?;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         prepStmt.setString(1, email);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         if (rs.next()) {
	        	 
	        	Calendar cal  = Calendar.getInstance();
	 	        long timeNow = cal.getTimeInMillis();
	 	        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
	        	 
	        	 java.sql.Timestamp expiryTs = rs.getTimestamp("expiryTime");
	        	 
	        	 String linkId = rs.getString("inviteId");
	        	 
	        	 if(expiryTs.compareTo(ts) > 0) {
	        		 
	        		 System.out.println("Still have expiry time");
	        		 prepStmt.close();
	        		 //need to send the link
		        	 return new Pair<Integer, String>(200, linkId);
	        	 }
	        	 else {
	        		 System.out.println("expired");
	        		 prepStmt.close();
	        		 if(updateExpiryDate(linkId)) {
	        			 
	        			 //Need to send the link
	        			 return new Pair<Integer, String>(200, linkId);
	        		 }
	        		 else {
	        			 return new Pair<Integer, String>(500, "");
	        		 }
		        	
	        	 }
	        	 
	         }
	         else {
	        	 
	        	 prepStmt.close();
	        	 return new Pair<Integer, String>(400, "");
	         }
		 }
		 catch(Exception e) {
			 
			 System.out.println("Yoo yooo"+ e);
			 return new Pair<Integer, String>(500, "");
		 }
	}
	
	private Pair<Integer, String> addInvite(String inviteId, String userId, String email) {
		
		try {
			
	    	
	        String sqlStatement = "insert into invite(inviteId, email, expiryTime, isDeleted, createdDate, updateDate, userId) values (?, ?, ?, ? ,? ,? ,?);";  
	        
	        
	        Calendar cal  = Calendar.getInstance();
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
	        
	        
	        System.out.println("Testing 3");
	        
	        int x = prepStmt.executeUpdate();
	        
	        if (x == 1) {  
	        	
	        	//Send email
	        	prepStmt.close();
	        	ResultSet singleInvite = getSingleInvite(inviteId);
	        	
	        	if (singleInvite.next()) {
		        	 
		        	 JSONObject obj = new JSONObject();
		        	 obj.put("Link", singleInvite.getString("inviteId"));
		        	 remove();
		        	 return new Pair<Integer, String>(200 , ApiResponseHandler.apiResponse(ResponseType.SUCCESS, InviteConstants.inviteCreatedSuccessfully, obj));
		        	 
		         }
		         else {
		        	 remove();
		        	 return new Pair<Integer, String>(400 , ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
		         }
	        }
	        else {
	        	
	        	prepStmt.close();
	        	remove();
	        	return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.inviteCreateFailed));
	        }
		}
		catch(Exception e) {
			
			System.out.println("addInvite" + e);
			remove();
			return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}
	
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

	private ResultSet getSingleInvite(String inviteId) throws Exception {
	
			 String selectStatement = "select * from invite where inviteId = ?;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         prepStmt.setString(1, inviteId);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         return rs;
	}
	
	private Boolean updateExpiryDate(String inviteId) {
		try {
			
	    	
	        String sqlStatement = "update invite set expiryTime = ?, updateDate = ? where inviteId = ?;";  
	        
	        
	        Calendar cal  = Calendar.getInstance();
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
	        	
	        	//Send email
	        	prepStmt.close();
	            remove();
	            return true;
	        }
	        else {
	        	
	        	prepStmt.close();
	        	remove();
	        	return false;
	        }
		}
		catch(Exception e) {
			
			System.out.println("addInvite" + e);
			remove();
			return false;
		}
	}

}
