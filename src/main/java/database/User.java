package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Random;

import org.json.JSONObject;

import constants.UserConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.ResponseType;

public class User extends DatabaseConnector {

	public User() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String register(String email, String name, String password) {
		try {
			String lastId = getLastUser();
			if(lastId != "") {
				if(checkEmailExisit(email)) {
					remove();
	            	return ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.emailAlreadyExist);
				}
				else {
					return registerUser(Helper.nextId(lastId, "USR"), email, name, password);
				}
			}
			else {
				return registerUser(UserConstants.firstUserId, email, name, password);
			}
			
        } catch (Exception e) {
        	remove();
            return ApiResponseHandler.apiResponse(ResponseType.SERVERERROR);
        }
	}
	
	public String login(String email, String password) {
    	boolean status = false;
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
            		return ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.loginSuccessfully, body);
            	}
            	else {
            		prepStmt.close();
            		remove();
            		return ApiResponseHandler.apiResponse(ResponseType.SERVERERROR);
            	}
            } 
            else {
            	prepStmt.close();
            	remove();
            	return ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.loginFailed);
            }
           
        } catch (Exception ex) {
        	remove();
        	System.out.println(ex);
            return ApiResponseHandler.apiResponse(ResponseType.SERVERERROR);
        }
    }
	
	private boolean checkEmailExisit(String email) {
		
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
			 remove();
			 return false;
		 }
	}
	
	private String getLastUser() {
		
		 try {
			 String selectStatement = "select userId from user ORDER BY UserId DESC LIMIT 1;";
	         
	         PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	         
	         ResultSet rs = prepStmt.executeQuery();
	         
	         if (rs.next()) {
	        	 String userId = rs.getString("UserId");
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
	
	private String registerUser(String userId, String email, String name, String password) {
		try {
			String salt = getSalt();
	      	String passwordHashedSalted = hashPassword(password + salt);
	    	
	        String sqlStatement = "insert into user(userId, email, name, salt, password, createdDate, updateDate, isDeleted) values (?, ?, ?, ? ,? ,? ,? ,?);";  
	        
	        long millis=System.currentTimeMillis();  
	        java.sql.Date date = new java.sql.Date(millis);  
	        
	        PreparedStatement prepStmt = con.prepareStatement(sqlStatement);
	        prepStmt.setString(1, userId);
	        prepStmt.setString(2, email);
	        prepStmt.setString(3, name);
	        prepStmt.setString(4, salt);
	        prepStmt.setString(5, passwordHashedSalted);
	        prepStmt.setDate(6, date);
	        prepStmt.setDate(7, date);
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
			return "";
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
