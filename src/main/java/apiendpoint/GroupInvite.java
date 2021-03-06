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
 * Servlet implementation class GroupInvite
 */
@WebServlet("/group-invite")
public class GroupInvite extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GroupInvite() {
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

			System.out.println("envirment variable " + System.getenv().get("PATH"));
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// Handle the session
			if (TokenHanler.checkToken()) {

				String userId = request.getParameter("userId");

				if (userId != null) {

					// Valid email
					database.GroupInvite invite = new database.GroupInvite();
					Pair<Integer, String> loginOutput = invite.getAllGroupInivite(userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				} else {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}
			} else {

				// Not authorisation
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			// Exception handle
			System.out.println(e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// Handle the session
			if (TokenHanler.checkToken()) {

				StringBuffer jb = new StringBuffer();
				String line = null;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);

				JSONObject jsonBody = new JSONObject(jb.toString());

				try {
					String reciverId = (String) jsonBody.get("reciverId");
					String userId = (String) jsonBody.get("userId");
					String groupId = (String) jsonBody.get("groupId");
					String accessLevel = (String) jsonBody.get("accessLevel");

					if (reciverId.equals(userId)) {

						// Email and userId not found
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(
								ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.sameUserRequest));
					} else {

						Integer accesslevl = Integer.parseInt(accessLevel);

						// Valid email
						database.GroupInvite invite = new database.GroupInvite();
						Pair<Integer, String> loginOutput = invite.createGroupInvite(userId, accesslevl, reciverId,
								groupId);
						response.setStatus(loginOutput.getKey());
						out.print(loginOutput.getValue());
					}

				} catch (JSONException e) {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			} else {

				// Not authorisation
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			// Exception handle
			System.out.println(e);
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

				String groupInviteId = request.getParameter("groupInviteId");
				String userId = request.getParameter("userId");

				if (groupInviteId != null && userId != null) {

					// Handle the delete invite
					database.GroupInvite invite = new database.GroupInvite();
					Pair<Integer, String> loginOutput = invite.deleteGroupInvite(userId, groupInviteId);
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
