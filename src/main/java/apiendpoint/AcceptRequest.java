package apiendpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import constants.FriendRequestConstraints;
import constants.UserConstants;
import database.User;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.LogsHandler;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class AcceptRequest
 */
@WebServlet("/accept-request")
public class AcceptRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AcceptRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			
			//user session handler
			if (TokenHanler.checkToken()) { 
				
				
				StringBuffer jb = new StringBuffer();
				String line = null;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);

				JSONObject jsonBody = new JSONObject(jb.toString());

				try {

					String friendReqeustId = (String) jsonBody.get("friendsRequestId");
					String userId = (String) jsonBody.get("userId");

					//Accept friend request
					database.FriendRequest friendRequest = new database.FriendRequest();
					Pair<Integer, String> loginOutput = friendRequest.acceptFriendRequest(friendReqeustId, userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());

				} catch (JSONException e) {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}
				
			}
			else {
				//User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}
			
			
			out.flush();
		}
		catch(Exception e) {
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

}
