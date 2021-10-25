package apiendpoint;

import java.io.BufferedReader;
import javax.servlet.FilterChain;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Filter.Chain;

import constants.UserConstants;
import database.FriendInvite;
import database.User;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.LogsHandler;
import unitls.Pair;
import unitls.ResponseType;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @throws
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		// TODO Auto-generated method stub
		// Analyze the servlet exception

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

				if (Helper.isEmailValid(email)) {

					// Handle the login
					User user = new User();
					Pair<Integer, String> loginOutput = user.login(email, password);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				} else {

					// Email format validation fail
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

			// Exception handlers
			throw new ServletException(e);
		}
	}

}
