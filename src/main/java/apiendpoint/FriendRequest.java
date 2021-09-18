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

			String userId = request.getParameter("userId");

			if (userId != null) {

				database.FriendRequest friendRequest = new database.FriendRequest();
				Pair<Integer, String> loginOutput = friendRequest.getAllRequest(userId);
				response.setStatus(loginOutput.getKey());
				out.print(loginOutput.getValue());

			} else {

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
			}

			out.flush();
		} catch (Exception e) {
			LogsHandler.logs();
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

			String friendId = request.getParameter("friendId");
			String userId = request.getParameter("userId");

			if (friendId != null && userId != null) {

				if (friendId != userId) {

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

			out.flush();
		} catch (Exception e) {
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

}
