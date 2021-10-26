package apiendpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import database.SharedTransaction;
import unitls.ApiResponseHandler;
import unitls.Pair;
import unitls.ResponseType;
import unitls.TokenHanler;

/**
 * Servlet implementation class GetFriendsTransactionByTransactionId
 */
@WebServlet("/GetUserByTransacitonFriend")
public class GetUserByTransactionIdFriend extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetUserByTransactionIdFriend() {
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

			// user session handler
			if (TokenHanler.checkToken()) {

				String userId = request.getParameter("userId");
				String transactionId = request.getParameter("transactionId");
				if(userId != null && transactionId != null) {

					SharedTransaction group = new SharedTransaction();
					Pair<Integer, String> groupResult = group.getUsersByTransactionsId(userId, transactionId);
					response.setStatus(groupResult.getKey());
					out.print(groupResult.getValue());

				} else {

					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
				}

			} else {

				// User session failure
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.print(ApiResponseHandler.apiResponse(ResponseType.UNAUTHORIZED));
			}

			out.flush();
		} catch (Exception e) {

			// Exception
			throw new ServletException(e);
		}
	}


}
