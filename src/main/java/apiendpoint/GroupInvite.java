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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
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

			if (TokenHanler.checkToken()) {

				String email = request.getParameter("email");
				String userId = request.getParameter("userId");
				String groupId = request.getParameter("groupId");
				String accessLevel = request.getParameter("accessLevel");

				if (email != null && userId != null) {
					
					Integer accesslevl = Integer.parseInt(accessLevel);

					if (email != null && userId != null) {

						if (Helper.isEmailValid(email)) {

							LogsHandler.logs();

							database.GroupInvite invite = new database.GroupInvite();
							Pair<Integer, String> loginOutput = invite.createGroupInvite(userId, accesslevl, email, groupId);
							response.setStatus(loginOutput.getKey());
							out.print(loginOutput.getValue());

						}

						else {

							LogsHandler.logs();
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE,
									InviteConstants.emailFormatRequired));
						}

					} else {

						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, InviteConstants.fieldsMissing));
				}
			} else {

				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();

		} catch (Exception e) {

			System.out.println(e);
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

}
