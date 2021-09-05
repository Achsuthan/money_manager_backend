package unitls;

import org.json.JSONException;
import org.json.JSONObject;

import constants.Constants;

public class ApiResponseHandler {
	
	public static String apiResponse(ResponseType responseType)  {
		try {
			return getCommonResponse(responseType).toString();
		}
		catch(Exception e) {
			return "";
		}
	}
	
	public static String apiResponse(ResponseType responseType, String message)  {
		try {
			JSONObject response = getCommonResponse(responseType);
			response.put("message", message);
			return response.toString();
		}
		catch(Exception e) {
			return "";
		}
	}
	
	public static String apiResponse(ResponseType responseType, String message, JSONObject bodyJson)  {
		try {
			JSONObject response = getCommonResponse(responseType);
			response.put("message", message);
			response.putOpt("body", bodyJson);
			return response.toString();
		}
		catch(Exception e) {
			return "";
		}
	}
	
	private static JSONObject getCommonResponse(ResponseType responseType) throws JSONException {
		switch (responseType) {
		case SUCCESS: {
			JSONObject json = new JSONObject();
			json.put("status", Constants.success);
			json.put("code", 200);
			json.put("message", Constants.successCoomonMessage);
			return json;
		}
		case FAILURE: {
			JSONObject json = new JSONObject();
			json.put("status", Constants.failure);
			json.put("code", 400);
			json.put("message", Constants.failureMessage);
			return json;
		}
		case DATAMISSING: {
			JSONObject json = new JSONObject();
			json.put("status", Constants.failure);
			json.put("code", 400);
			json.put("message", Constants.dataMissing);
			return json;
		}
		case SERVERERROR: {
			JSONObject json = new JSONObject();
			json.put("status", Constants.failure);
			json.put("code", 500);
			json.put("message", Constants.serverErrorMessage);
			return json;
		}
		case PAGENOTFOUND: {
			JSONObject json = new JSONObject();
			json.put("status", Constants.failure);
			json.put("code", 404);
			json.put("message", Constants.pageNotFound);
			return json;
		}
		}
		return null;

	}
}
