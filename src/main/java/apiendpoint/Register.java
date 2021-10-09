package apiendpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.HTTP;
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

/**
 * Servlet implementation class Register
 */
@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Register() {
		super();
		// TODO Auto-generated constructor stub
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

			StringBuffer jb = new StringBuffer();
			String line = null;

			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);

			JSONObject jsonBody = new JSONObject(jb.toString());

			try {

				String email = (String) jsonBody.get("email");
				String password = (String) jsonBody.get("password");
				String name = (String) jsonBody.get("name");

				String inviteId = "";

				try {
					inviteId = (String) jsonBody.get("inviteId");
				} catch (JSONException e) {
					inviteId = "";
				}

				if (Helper.isEmailValid(email)) {

					// Invite handle for registration
					FriendInvite invite = new FriendInvite();
					if (!inviteId.isEmpty()) {

						// Handle the register
						Pair<Integer, String> acceptOutput = invite.acceptInvite(email, name, password, inviteId);
						response.setStatus(acceptOutput.getKey());
						out.print(acceptOutput.getValue());
					} else {

						// Regular user registration
						User user = new User();
						response.setStatus(HttpServletResponse.SC_OK);
						Pair<Integer, String> registerOutput = user.register(email, name, password, true);
						response.setStatus(registerOutput.getKey());
						out.print(registerOutput.getValue());
					}
				} else {

					// Email format not valid case
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.emailForatRequired));
				}

			} catch (JSONException e) {

				// All the required data not found in the API body
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
			}

			out.flush();

		} catch (Exception e) {
			// Exception handler
			throw new ServletException(e);
		}
	}

}
