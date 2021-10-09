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

import constants.InviteConstants;
import constants.UserConstants;
import database.FriendInvite;
import database.User;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.LogsHandler;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class FriendIvite
 */
@WebServlet("/friend-invite")
public class FriendIvite extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FriendIvite() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		try {

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// user session handler
			if (TokenHanler.checkToken()) {
				
				String userId = request.getParameter("userId");

				if (userId != null) {

					// Create invite
					FriendInvite invite = new FriendInvite();
					Pair<Integer, String> loginOutput = invite.getAllInvites(userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				} else {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			} else {

				// User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			// Exception handler
			System.out.println(e);
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// user session handler
			if (TokenHanler.checkToken()) {

				StringBuffer jb = new StringBuffer();
				String line = null;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);

				JSONObject jsonBody = new JSONObject(jb.toString());

				try {

					String email = (String) jsonBody.get("email");
					String userId = (String) jsonBody.get("userId");

					// Email validation
					if (Helper.isEmailValid(email)) {

						// Create invite
						FriendInvite invite = new FriendInvite();
						Pair<Integer, String> loginOutput = invite.createInvite(email, userId);
						response.setStatus(loginOutput.getKey());
						out.print(loginOutput.getValue());

					}

					else {

						// Email validation failed
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE,
								InviteConstants.emailFormatRequired));
					}

				} catch (JSONException e) {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}
			} else {

				// User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			// Exception handler
			System.out.println(e);
			LogsHandler.logs();
			throw new ServletException(e);
		}

	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// User Session check
			if (TokenHanler.checkToken()) {

				String inviteId = request.getParameter("inviteId");
				String userId = request.getParameter("userId");

				if (inviteId != null && userId != null) {

					// Handle the delete invite
					FriendInvite invite = new FriendInvite();
					Pair<Integer, String> loginOutput = invite.deleteInvite(inviteId, userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				} else {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			} else {

				// User session not found
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			// Exception
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

}
