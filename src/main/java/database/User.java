package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Calendar;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.UserConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

public class User extends DatabaseConnector {

	public User() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public Pair<Integer, String> register(String email, String name, String password) {
		try {
			String lastId = getLastUser();
			if(lastId != "") {
				if(checkEmailExisit(email)) {
					
					remove();
	            	return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.emailAlreadyExist));
				}
				else {
					Pair<Integer, String> res = new Pair<Integer, String>(200 ,registerUser(Helper.nextId(lastId, "USR"), email, name, password));
					remove();
					return res;
				}
			}
			else {
				
				Pair<Integer, String> res = new Pair<Integer, String>(200 ,registerUser(UserConstants.firstUserId, email, name, password));
				remove();
				return res;
			}
			
        } catch (Exception e) {
        	remove();
            return new Pair<Integer, String>(500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
        }
	}
	
	public Pair<Integer, String> login(String email, String password) {
		
        try {
            String selectStatement = "select * " + "from user where email = ?";
            
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            prepStmt.setString(1, email);
            
            ResultSet rs = prepStmt.executeQuery();
            
            if (rs.next()) {
            	
            	String hasedPassword = rs.getString("password");
            	String saltDb = rs.getString("salt");
            	
            	if (hasedPassword.equals(hashPassword(password + saltDb))) {
            		
            		JSONObject body = new JSONObject();
            		body.put("userId", rs.getString("userId"));
            		body.put("email", rs.getString("email"));
            		prepStmt.close();
            		remove();
            		return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.loginSuccessfully, body));
            	}
            	else {
            		
            		prepStmt.close();
            		remove();
            		return new Pair<Integer, String>(400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.loginFailed));
            	}
            } 
            else {
            	
            	prepStmt.close();
            	remove();
            	return new Pair<Integer, String>(400, ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.loginFailed));
            }
           
        } catch (Exception ex) {
        	
        	remove();
        	System.out.println(ex);
            return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
        }
    }
	
	public boolean checkEmailExisit(String email) {
		
		 try {
			 String selectStatement = "select *  from user where email = ?;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         prepStmt.setString(1, email);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         
	         if (rs.next()) {
	        	 
	        	 prepStmt.close();
	        	 return true;
	         }
	         else {
	        	 
	        	 prepStmt.close();
	        	 return false;
	         }
		 }
		 catch(Exception e) {
			 
			 return false;
		 }
	}
	
	private String getLastUser() {
		
		 try {
			 String selectStatement = "select userId from user ORDER BY UserId DESC LIMIT 1;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         if (rs.next()) {
	        	 
	        	 String userId = rs.getString("userId");
	        	 prepStmt.close();
	        	 return userId;
	         }
	         else {
	        	 return "";
	         }
		 }
		 catch(Exception e) {
			 
			 remove();
			 return "";
		 }
	}
	
	public Boolean CheckUserExist(String UserId) {
		
		 try {
			 String selectStatement = "select userId from user where userId = ?;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         prepStmt.setString(1, UserId);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         if (rs.next()) {
	        	 
	        	 prepStmt.close();
	        	 return true;
	         }
	         else {
	        	 
	        	 prepStmt.close();
	        	 return false;
	         }
		 }
		 catch(Exception e) {
			 
			 return false;
		 }
	}
	
	private String registerUser(String userId, String email, String name, String password) {
		try {
			
			String salt = getSalt();
	      	String passwordHashedSalted = hashPassword(password + salt);
	    	
	        String sqlStatement = "insert into user(userId, email, name, salt, password, createdDate, updateDate, isDeleted) values (?, ?, ?, ? ,? ,? ,? ,?);";  
	        
	        long timeNow = Calendar.getInstance().getTimeInMillis();
	        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);  
	        
	        PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
	        prepStmt.setString(1, userId);
	        prepStmt.setString(2, email);
	        prepStmt.setString(3, name);
	        prepStmt.setString(4, salt);
	        prepStmt.setString(5, passwordHashedSalted);
	        prepStmt.setTimestamp(6, ts);
	        prepStmt.setTimestamp(7, ts);
	        prepStmt.setBoolean(8, false);
	        
	        
	        System.out.println("Testing 3");
	        
	        int x = prepStmt.executeUpdate();
	        
	        if (x == 1) {  
	        	
	        	prepStmt.close();
	            remove();
	        	return ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.userRegisterSuccessfully);
	        }
	        else {
	        	
	        	prepStmt.close();
	        	remove();
	        	return ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.sqlRegisterError);
	        }
		}
		catch(Exception e) {
			remove();
			return ApiResponseHandler.apiResponse(ResponseType.SERVERERROR);
		}
	}
	
	public Pair<Integer, String> search(String keyword) {
		 try {
			 
			 System.out.println("key world" + keyword);
			 
			 String sqlStatement = "select userId, name, email  from user WHERE name LIKE '%" + keyword + "%' OR email LIKE '%" + keyword + "%' ;";
	         
			 PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         System.out.println("statement" + rs);
	         
	         JSONObject obj = new JSONObject();
	         
	         JSONArray userArray = new JSONArray();
	         while (rs.next()) {
	        	 JSONObject user = new JSONObject();
	        	 user.put("userId", rs.getString("userId"));
	        	 user.put("name", rs.getString("name"));
	        	 user.put("email", rs.getString("email"));
	        	 userArray.put(user);
	         }
	         obj.put("users", userArray);
	         System.out.println("users "+ obj.toString() );
	         
	         if(userArray.length() > 0 ) {
	        	 return new Pair<Integer, String> ( 400 ,ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.usersFoundSuccessfully, obj));
	         }
	         else {
	        	 return new Pair<Integer, String> ( 400 ,ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.searchKeywordNotFound));
	         }
		 }
		 catch(Exception e) {
			 System.out.println("Exception" + e);
			 remove();
			 return new Pair<Integer, String> ( 500 ,ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		 }
	}
    
    private String hashAndSaltPassword(String password)
       	 throws NoSuchAlgorithmException {
    	
       	 String salt = getSalt();
       	 return hashPassword(password + salt);
    }
    
    private static String getSalt() {
    	
		 Random r = new SecureRandom();
		 byte[] saltBytes = new byte[32];
		 r.nextBytes(saltBytes);
		 return Base64.getEncoder().encodeToString(saltBytes);
	}
    
    private String hashPassword(String password)
    		   throws NoSuchAlgorithmException {
    	
    		 	MessageDigest md = MessageDigest.getInstance("SHA-256");
    		 	md.reset();
    		 	md.update(password.getBytes());
    		 	byte[] mdArray = md.digest();
    		 	StringBuilder sb = new StringBuilder(mdArray.length * 2);
    		 	
    		 	for (byte b : mdArray) {
    		 		int v = b & 0xff;
    		 		if (v < 16) {
    		 			sb.append('0');
    		 		}

    				sb.append(Integer.toHexString(v));
    		 	}
    		 	return sb.toString();
    }


}
