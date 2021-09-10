package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.Invite;
import database.User;
import unitls.ApiResponseHandler;
import unitls.LogsHandler;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class Search
 */
@WebServlet("/search")
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
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
			if(TokenHanler.checkToken()) { 
				
				String keyword = request.getParameter("keyword");
				
				if( keyword != null) {
					
					LogsHandler.logs();
					
					User user = new User();
					Pair<Integer, String> loginOutput = user.search(keyword);
					response.setStatus(loginOutput.getKey());
					out.print(loginOutput.getValue());
				}
			}
			else {
				
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}
			
			out.flush();
			
		}
		catch(Exception e) {
			
			System.out.println(e);
			LogsHandler.logs();
			throw new ServletException(e);
		}
	}

}
