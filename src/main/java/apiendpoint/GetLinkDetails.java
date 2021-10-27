package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.Category;
import database.FriendInvite;
import unitls.ApiResponseHandler;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class GetLinkDetails
 */
@WebServlet("/GetLinkDetails")
public class GetLinkDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetLinkDetails() {
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
				String inviteId = request.getParameter("inviteId");

				if (userId != null) {

					FriendInvite cat = new FriendInvite();
					Pair<Integer, String> groupResult = cat.getSingleFriendInviteDetails(userId, inviteId);
					response.setStatus(groupResult.getKey());
					out.print(groupResult.getValue());
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

}
