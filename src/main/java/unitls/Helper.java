package unitls;

import javax.mail.*;
import javax.mail.internet.*;

import constants.TransactionConstrains;

import java.util.Properties;

public class Helper {

	public static boolean isEmailValid(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

	public static String nextId(String lastId, String key) {
		String id = lastId.substring(key.length() + 1, lastId.length());
		Integer intId = Integer.parseInt(id);
		intId = intId + 1;
		return key + "-" + intId;
	}

	public static String createInviteLink(String inviteId) {

		// TODO: need to send the actual invite link
		return inviteId;
	}

	// Validate the transaction to
	public static Boolean validteTransactionTo(String transactionTo) {
		String upperTransactionTo = transactionTo.toUpperCase();
		if( upperTransactionTo.equals(TransactionConstrains.personal) || 
				upperTransactionTo.equals(TransactionConstrains.group) || 
				upperTransactionTo.equals(TransactionConstrains.friend)) {
			return true;
		}
		
		return false;
	}

	// Validate the transaction to
	public static Boolean validteTransactionType(String transactionType) {
		String transactionTypeUperCase = transactionType.toUpperCase();
		if(transactionTypeUperCase.equals(TransactionConstrains.expenses) || 
				transactionTypeUperCase.equals(TransactionConstrains.income) || 
				transactionTypeUperCase.endsWith(TransactionConstrains.transfer)) {
			return true;
		}
		
		return false;
	}

	// Send mail using g-mail
	public static Boolean sendMail(String body, String subject, String receiver) {

		final String username = "";
		final String password = "";

		if (receiver.isEmpty() || (username.isEmpty() && password.isEmpty())) {
			return true;
		}

		String host = "smtp.gmail.com";
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", host);
		prop.put("mail.smtp.port", 587);
		prop.put("mail.smtp.ssl.trust", host);
		prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("noreply@moneymanager.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);
			return true;

		} catch (MessagingException e) {
			return false;
		}
	}
}
