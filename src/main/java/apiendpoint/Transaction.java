package apiendpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jdi.connect.Transport;

import constants.TransactionConstrains;
import constants.UserConstants;
import database.GroupTransaction;
import database.SharedTransaction;
import database.SingleTransaction;
import database.User;
import unitls.ApiResponseHandler;
import unitls.Helper;
import unitls.Pair;
import unitls.ResponseType;

/**
 * Servlet implementation class OwnTransaction
 */
@WebServlet("/transaction")
public class Transaction extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Transaction() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
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
				
				String description = "";
				String name = (String) jsonBody.get("name");
				Double amount = (Double) jsonBody.get("amount");
				String date =  (String) jsonBody.get("date");
				String categoryId =  (String) jsonBody.get("categoryId");
				String userId =  (String) jsonBody.get("userId");
				String transactionTo =  (String) jsonBody.get("transactionTo");
				String transactionType =  (String) jsonBody.get("transactionType");
				
				Date javaDate = new SimpleDateFormat("yyyy-MM-dd").parse(date); 
				
				try {
					description = (String) jsonBody.get("descritpion");
				}
				catch (JSONException e) {
					
					description = "";
				}
				
				if(Helper.validteTransactionTo(transactionTo)) {
					//Own
					if(Helper.validteTransactionType(transactionType)) {
						
						String transactionToUpperCase = transactionTo.toUpperCase();
						if(transactionToUpperCase.equals(TransactionConstrains.personal)) {
							//Own Transaction
							
							if(amount > 0 ) {
								SingleTransaction transaciton = new SingleTransaction();
								Pair<Integer, String> obj = transaciton.createTransaction(userId, name, amount, description, javaDate, transactionTo, transactionType, categoryId);
								response.setStatus(obj.getKey());
								out.print(obj.getValue());
							}
							else {
								
								// All the required data not found in the API body
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.transactionAmountCannotbeZero));
							}
						}
						else if(transactionToUpperCase.equals(TransactionConstrains.friend)) {
							
							//Friends
							try {
								JSONArray friendsDetails = jsonBody.getJSONArray("friends");
								
								if(!transactionType.toUpperCase().equals(TransactionConstrains.transfer)) {
									// All the required data not found in the API body
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.transactionTypeValidationError));
								}
								else {
									
									SharedTransaction transaciton = new SharedTransaction();
									Pair<Integer, String> obj = transaciton.handleSharedTransaction(userId, friendsDetails, name, amount, description, javaDate, transactionTo, transactionType, categoryId);
									response.setStatus(obj.getKey());
									out.print(obj.getValue());
								}
							}
							catch (JSONException e) {

								// All the required data not found in the API body
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
							}
						}
						else {
							//Group
							try {
								JSONArray friendsDetails = jsonBody.getJSONArray("friends");
								String groupId = (String) jsonBody.get("groupId");
								
								if(!transactionType.toUpperCase().equals(TransactionConstrains.transfer)) {
									// All the required data not found in the API body
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.transactionTypeValidationError));
								}
								else {
									GroupTransaction transaction = new GroupTransaction();
									Pair<Integer, String> obj = transaction.handleGroupTransaction(userId, groupId ,friendsDetails, name, amount, description, javaDate, transactionTo, transactionType, categoryId);
									response.setStatus(obj.getKey());
									out.print(obj.getValue());
								}
							}
							catch (JSONException e) {

								// All the required data not found in the API body
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING));
							}
						}
					}
					else {
						
						// All the required data not found in the API body
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						out.print(ApiResponseHandler.apiResponse(ResponseType.DATAMISSING, TransactionConstrains.transactionTypeValidationError));
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
