package constants;

public class TransactionConstrains {
	
	//common 
	public static String personal = "PERSONAL";
	public static String group = "GROUP";
	public static String friend = "FRIEND";
	
	public static String expenses = "EXPENSES";
	public static String income = "INCOME";
	public static String transfer = "TRANSFER";
	
	public static String firstTransactionId = "TRA-11111111";
	public static String firstSharedTransactionId = "SHT-11111111";
	
	//Success
	public static String transactionCreatedSuccessfuly = "Transaction created successfully";
	
	//Failure
	public static String transactionTypeValidationError = "Please check the transaction type";
	public static String transactionToValidationError = "Please check the transaction to type";
	public static String categoryNotExist = "Category Not exist";
	public static String transactionAmountCannotbeZero = "Transaction amount should be greater than zero";
	public static String splitPersentageWrong = "Something wrong with the split persentage please have a look";
	public static String notFriends = "You are not friends with one or more shared users";

}
