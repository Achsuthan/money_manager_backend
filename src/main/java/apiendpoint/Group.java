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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// user session handler
			if (TokenHanler.checkToken()) {

				String userId = request.getParameter("userId");

				if (userId != null) {
					
					database.Group group = new database.Group();
					Pair<Integer, String> groupResult = group.getAllGroup(userId);
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
			throw new ServletException(e);
		}
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

			// user session handler
			if (TokenHanler.checkToken()) {

				StringBuffer jb = new StringBuffer();
				String line = null;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);

				JSONObject jsonBody = new JSONObject(jb.toString());

				try {

					String userId = (String) jsonBody.get("userId");
					String groupName = (String) jsonBody.get("groupName");

					database.Group group = new database.Group();
					Pair<Integer, String> groupResult = group.createGroup(userId, groupName);
					response.setStatus(groupResult.getKey());
					out.print(groupResult.getValue());

				} catch (JSONException e) {

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
