package apiendpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import constants.UserConstants;
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
				
				String userId = request.getParameter("userId");
				String searchType = request.getParameter("searchType");

				if (userId != null && searchType != null) {
					
					String keyword = request.getParameter("keyword");
					System.out.println("searchType"+searchType);
					
					if( keyword != null) {
						
						LogsHandler.logs();
						
						//Get all the users
						if(searchType.equals("0")) {
							
							database.Search search = new database.Search();
							Pair<Integer, String> searchResult = search.searchAllUsers(keyword, userId);
							response.setStatus(HttpServletResponse.SC_OK);
							out.print(searchResult.getValue());
						}
						//Only friends
						else if(searchType.equals("1")){
							
							database.Search search = new database.Search();
							Pair<Integer, String> searchResult = search.searchFriends(keyword, userId);
							response.setStatus(HttpServletResponse.SC_OK);
							out.print(searchResult.getValue());
						}
						else if(searchType.equals("2")) {
							
							database.Search search = new database.Search();
							Pair<Integer, String> searchResult = search.searchUserByAllAndFriends(keyword, userId);
							response.setStatus(HttpServletResponse.SC_OK);
							out.print(searchResult.getValue());
						}
						else {
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
						}
					}
					else {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
					}
				}
				else {

					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
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
