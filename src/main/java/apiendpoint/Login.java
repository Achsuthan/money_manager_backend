package apiendpoint;

import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.UserConstants;
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

			String email = request.getParameter("email");
			String password = request.getParameter("password");

			//Check email and password is empty
			if (email != null && password != null) {

				if (Helper.isEmailValid(email)) {

				    //Logs event for the user, if needed can add the logic here
					LogsHandler.logs();

					//Handle the login
					User user = new User();
					Pair<Integer, String> loginOutput = user.login(email, password);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				} else {

					//Email format validation fail
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.FAILURE, UserConstants.emailForatRequired));
				}
			} else {

				//If the user name and password is empty user will get error message
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
			}

			out.flush();
		} catch (Exception e) {
			
			//Exception handlers
			throw new ServletException(e);
		}
	}

}
