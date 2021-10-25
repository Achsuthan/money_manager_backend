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

import constants.TransactionConstrains;
import database.GroupTransaction;
import database.SharedTransaction;
import database.SingleTransaction;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

/**
 * Servlet implementation class GetTeansacitons
 */
@WebServlet("/getTransactions")
public class GetTransacitons extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTransacitons() {
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

			StringBuffer jb = new StringBuffer();
			String line = null;

			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);

			JSONObject jsonBody = new JSONObject(jb.toString());

			try {
				
				String transactionTo = (String) jsonBody.get("transactionTo");
				String userId =  (String) jsonBody.get("userId"); 
				
				
				if(Helper.validteTransactionTo(transactionTo)) {
					
					String transactionToUpperCase = transactionTo.toUpperCase();
					
					if(transactionToUpperCase.equals(TransactionConstrains.personal)) {
						//Own Transaction
						
						SingleTransaction transaction = new SingleTransaction();
						Pair<Integer, String> obj = transaction.getOwnTransaction(userId, transactionTo.toLowerCase());
						response.setStatus(obj.getKey());
						out.print(obj.getValue());
						
					}
					else if(transactionToUpperCase.equals(TransactionConstrains.friend)) {
						//Friends
						
						SharedTransaction transaction = new SharedTransaction();						
						Pair<Integer, String> obj = transaction.getAllSharedTransactions(userId);
						response.setStatus(obj.getKey());
						out.print(obj.getValue());
					}
					else {
						//Group
						GroupTransaction transaction = new GroupTransaction();						
						Pair<Integer, String> obj = transaction.getAllGroupTransactions(userId);
						response.setStatus(obj.getKey());
						out.print(obj.getValue());
					}
				}
				else {
					
					// All the required data not found in the API body
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.transactionToValidationError));
				}
				

			} catch (JSONException e) {

				// All the required data not found in the API body
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
			}
			catch (ClassCastException ee) {
				
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
