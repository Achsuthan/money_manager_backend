package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.InviteConstants;
import database.FriendInvite;
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
			
			System.out.println("envirment variable "+ System.getenv().get("PATH"));
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// Handle the session
			if (TokenHanler.checkToken()) {

				String userId = request.getParameter("userId");

				if (userId != null) {

					//Valid email
					database.GroupInvite invite = new database.GroupInvite();
					Pair<Integer, String> loginOutput = invite.getAllGroupInivite(userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());

				} else {

					// Email and userId not found
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.fieldsMissing));
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

				String reciverId = request.getParameter("reciverId");
				String userId = request.getParameter("userId");
				String groupId = request.getParameter("groupId");
				String accessLevel = request.getParameter("accessLevel");

				if (reciverId != null && userId != null && groupId != null && accessLevel != null) {

					if(reciverId.equals(userId)) {
						
						// Email and userId not found
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.sameUserRequest));
					}
					else {
						
						Integer accesslevl = Integer.parseInt(accessLevel);

						//Valid email
						database.GroupInvite invite = new database.GroupInvite();
						Pair<Integer, String> loginOutput = invite.createGroupInvite(userId, accesslevl, reciverId, groupId);
						response.setStatus(loginOutput.getKey());
						out.print(loginOutput.getValue());
					}
					

				} else {

					// Email and userId not found
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.fieldsMissing));
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

}
