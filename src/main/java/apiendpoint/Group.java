package apiendpoint;

import java.io.IOException;
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
import unitls.TokenHanler;

/**
 * Servlet implementation class Group
 */
@WebServlet("/create-group")
public class Group extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Group() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			String userId = request.getParameter("userId");

			//user session handler
			if (TokenHanler.checkToken()) { 
				
				if (userId != null) {
					database.Group group = new database.Group();
					Pair<Integer, String> groupResult = group.getAllGroup(userId);
					response.setStatus(groupResult.getKey());
					out.print(groupResult.getValue());
					
				} else {

					//User Id not found
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			}
			else {
				
				//User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}
			
			out.flush();
		} catch (Exception e) {
			throw new ServletException(e);
		}
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

			String userId = request.getParameter("userId");
			String groupName = request.getParameter("groupName");

			//user session handler
			if (TokenHanler.checkToken()) { 
				
				//UserId and group name check
				if (userId != null && groupName != null) {
					
					database.Group group = new database.Group();
					Pair<Integer, String> groupResult = group.createGroup(userId, groupName);
					response.setStatus(groupResult.getKey());
					out.print(groupResult.getValue());
					
				} else {

					//Required information not found
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			}
			else {
				
				//User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}
			
			out.flush();
		} catch (Exception e) {
			
			//Exception
			throw new ServletException(e);
		}
	}

}
