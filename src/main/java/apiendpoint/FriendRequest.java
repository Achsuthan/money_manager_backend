package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * Servlet implementation class FriendRequest
 */
@WebServlet("/friend-request")
public class FriendRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FriendRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			
			//user session handler
			if (TokenHanler.checkToken()) { 
				String userId = request.getParameter("userId");

				//UserId required
				if (userId != null) {

					//Get all the invites
					database.FriendRequest friendRequest = new database.FriendRequest();
					Pair<Integer, String> loginOutput = friendRequest.getAllRequest(userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());

				} else {

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
		} catch (Exception e) {
			
			//Exception
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			//user session handler
			if (TokenHanler.checkToken()) { 
				
				String friendId = request.getParameter("friendId");
				String userId = request.getParameter("userId");

				//Friend Id and user Id required
				if (friendId != null && userId != null) {

					//Friend Id and User ID can't be equal
					if (friendId != userId) {
						
						//Create the friend request
						database.FriendRequest friendRequest = new database.FriendRequest();
						Pair<Integer, String> loginOutput = friendRequest.sendFriendRequest(userId, friendId);
						response.setStatus(loginOutput.getKey());
						out.print(loginOutput.getValue());
					} else {

						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								FriendRequestConstraints.sameUserRequest));
					}

				} else {

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
		} catch (Exception e) {
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}
	
	
	/**
	 * @see HttpServlet#doDelte(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			//user session handler
			if (TokenHanler.checkToken()) { 
				
				String friendRequestId = request.getParameter("friendRequestId");
				String userId = request.getParameter("userId");

				//Friend Id and user Id required
				if (friendRequestId != null && userId != null) {

					//Create the friend request
					database.FriendRequest friendRequest = new database.FriendRequest();
					Pair<Integer, String> loginOutput = friendRequest.deleteFriendRequest(userId, friendRequestId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());

				} else {

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
		} catch (Exception e) {
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}


}
