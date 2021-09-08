package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.User;
import unitls.ApiResponseHandler;
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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String name = request.getParameter("name");
			
			if(email != null && password != null && name != null) {
				User user = new User();
				response.setStatus(HttpServletResponse.SC_OK);
				Pair<Integer, String> registerOutput = user.register(email, name, password);
				response.setStatus(registerOutput.getKey());
				out.print(registerOutput.getValue());
			}
			else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
			}
			
			out.flush();
		}
		catch(Exception e) {
			throw new ServletException(e);
		}
	}

}
