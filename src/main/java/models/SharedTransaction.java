package models;

import java.util.ArrayList;

import org.json.JSONArray;

public class SharedTransaction {
	private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	public void addTransaction(Transaction transaction, User friend) {
		
		if(this.transactions.size() > 0){
			Transaction single = new Transaction();
			Boolean isFound = false;
			for (Transaction singleTransaction : new ArrayList<Transaction>(transactions)) {
				if(singleTransaction.isTransactionExist(transaction.getTransactionId())) {
					single = singleTransaction;
					isFound = true;
					break;
				}
			}
			
			if(isFound) {
				single.setFriends(friend);
			}else {
				single = transaction;
				single.setFriends(friend);
				transactions.add(single);
			}
		}
		else {
			Transaction tmp = new Transaction();
			tmp = transaction;
			tmp.setFriends(friend);
			transactions.add(tmp);
		}
		
	}
	
	public JSONArray getSharedTransactionObject() throws Exception{
		
		JSONArray returnArray = new JSONArray();
		
		for (Transaction singleTransaction : new ArrayList<Transaction>(transactions)) {
			returnArray.put(singleTransaction.getTransactionObject());
		}
		return returnArray;
	}
}
