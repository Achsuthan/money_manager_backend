package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	protected void goGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
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

			if (TokenHanler.checkToken()) {

				String email = request.getParameter("email");
				String userId = request.getParameter("userId");

				if (email != null && userId != null) {

					if (email != null && userId != null) {

						if (Helper.isEmailValid(email)) {

							LogsHandler.logs();

							FriendInvite invite = new FriendInvite();
							Pair<Integer, String> loginOutput = invite.createInvite(email, userId);
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

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			if (TokenHanler.checkToken()) {

				String inviteId = request.getParameter("inviteId");
				String userId = request.getParameter("userId");

				if (inviteId != null && userId != null) {
					LogsHandler.logs();
					FriendInvite invite = new FriendInvite();
					Pair<Integer, String> loginOutput = invite.deleteInvite(inviteId, userId);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
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
