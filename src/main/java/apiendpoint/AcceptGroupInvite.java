package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.InviteConstants;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class AcceptGroupInvite
 */
@WebServlet("/accept_group_invite")
public class AcceptGroupInvite extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AcceptGroupInvite() {
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

			// Handle the session
			if (TokenHanler.checkToken()) {

				String userId = request.getParameter("userId");
				String groupInviteId = request.getParameter("groupInviteId");

				if (groupInviteId != null && userId != null) {

					database.GroupInvite invite = new database.GroupInvite();
					Pair<Integer, String> loginOutput = invite.acceptGroupInvite(userId, groupInviteId);
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

}
