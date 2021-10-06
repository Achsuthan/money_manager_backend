package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
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

	public Pair<Integer, String> register(String email, String name, String password, Boolean isCloseConection) {
		try {
			String lastId = getLastUser();
			if (!lastId.isEmpty()) {
				if (checkEmailExisit(email)) {

					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.emailAlreadyExist));
				} else {

					Pair<Integer, String> res = registerUser(Helper.nextId(lastId, "USR"), email, name, password);
					if(isCloseConection) {
						remove();
					}
					return res;
				}
			} else {

				Pair<Integer, String> res = registerUser(UserConstants.firstUserId, email, name, password);
				remove();
				return res;
			}

		} catch (Exception e) {

			remove();
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	//Handle the login logic
	public Pair<Integer, String> login(String email, String password) {

		try {
			
			String selectStatement = "select * " + "from user where email = ?";

			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, email);

			ResultSet rs = prepStmt.executeQuery();

			if (rs.next()) {

				String hasedPassword = rs.getString("password");
				String saltDb = rs.getString("salt");

				// Success response for user
				if (hasedPassword.equals(hashPassword(password + saltDb))) {

					// TODO: Can think about mail service that we can send notification user logged in
					JSONObject body = new JSONObject();
					body.put("userId", rs.getString("userId"));
					body.put("email", rs.getString("email"));
					prepStmt.close();
					remove();
					return new Pair<Integer, String>(200, ApiResponseHandler.apiResponse(ResponseType.SUCCESS,
							UserConstants.loginSuccessfully, body));
				} else {

					// Password Failure case
					prepStmt.close();
					remove();
					return new Pair<Integer, String>(400,
							ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.loginFailed));
				}
			} else {

				//Email not exist 
				prepStmt.close();
				remove();
				return new Pair<Integer, String>(400,
						ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.loginFailed));
			}

		} catch (Exception ex) {

			//Exception handler
			remove();
			System.out.println(ex);
			return new Pair<Integer, String>(500, ApiResponseHandler.apiResponse(ResponseType.SERVERERROR));
		}
	}

	public boolean checkEmailExisit(String email) throws Exception {

		String selectStatement = "select *  from user where email = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, email);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}

	private String getLastUser() throws Exception {

		String selectStatement = "select userId from user ORDER BY UserId DESC LIMIT 1;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			String userId = rs.getString("userId");
			prepStmt.close();
			return userId;
		} else {
			return "";
		}

	}

	public Boolean CheckUserExist(String UserId) throws Exception {
		String selectStatement = "select userId from user where userId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, UserId);

		ResultSet rs = prepStmt.executeQuery();

		if (rs.next()) {

			prepStmt.close();
			return true;
		} else {

			prepStmt.close();
			return false;
		}

	}

	public JSONObject getSingleUserDetails(String UserId) throws Exception {
		String selectStatement = "select * from user where userId = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, UserId);

		ResultSet rs = prepStmt.executeQuery();

		JSONObject obj = new JSONObject();

		if (rs.next()) {

			obj.put("userId", rs.getString("userId"));
			obj.put("name", rs.getString("name"));
			obj.put("email", rs.getString("email"));
			prepStmt.close();
		}
		return obj;
	}
	
	public ResultSet getSingleUserDetailsByEmail(String email) throws Exception {
		String selectStatement = "select * from user where email = ?;";

		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, email);

		ResultSet rs = prepStmt.executeQuery();
		return rs;
	}

	private Pair<Integer, String> registerUser(String userId, String email, String name, String password) throws Exception {

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
			return new Pair<Integer, String>(200,
					ApiResponseHandler.apiResponse(ResponseType.SUCCESS, UserConstants.userRegisterSuccessfully));
		} else {

			prepStmt.close();
			return new Pair<Integer, String>(400,
					ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.sqlRegisterError));
		}

	}

	private String hashAndSaltPassword(String password) throws NoSuchAlgorithmException {

		String salt = getSalt();
		return hashPassword(password + salt);
	}

	private static String getSalt() {

		Random r = new SecureRandom();
		byte[] saltBytes = new byte[32];
		r.nextBytes(saltBytes);
		return Base64.getEncoder().encodeToString(saltBytes);
	}

	private String hashPassword(String password) throws NoSuchAlgorithmException {

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
