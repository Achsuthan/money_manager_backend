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
			
		    
		    String html = "<!DOCTYPE html>\n"
		    		+ "<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" lang=\"en\">\n"
		    		+ "\n"
		    		+ "<head>\n"
		    		+ "	<title></title>\n"
		    		+ "	<meta charset=\"UTF-8\">\n"
		    		+ "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
		    		+ "	<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml><![endif]-->\n"
		    		+ "	<style>\n"
		    		+ "		* {\n"
		    		+ "			box-sizing: border-box;\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		body {\n"
		    		+ "			margin: 0;\n"
		    		+ "			padding: 0;\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		th.column {\n"
		    		+ "			padding: 0\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		a[x-apple-data-detectors] {\n"
		    		+ "			color: inherit !important;\n"
		    		+ "			text-decoration: inherit !important;\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		#MessageViewBody a {\n"
		    		+ "			color: inherit;\n"
		    		+ "			text-decoration: none;\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		p {\n"
		    		+ "			line-height: inherit\n"
		    		+ "		}\n"
		    		+ "\n"
		    		+ "		@media (max-width:660px) {\n"
		    		+ "			.icons-inner {\n"
		    		+ "				text-align: center;\n"
		    		+ "			}\n"
		    		+ "\n"
		    		+ "			.icons-inner td {\n"
		    		+ "				margin: 0 auto;\n"
		    		+ "			}\n"
		    		+ "\n"
		    		+ "			.row-content {\n"
		    		+ "				width: 100% !important;\n"
		    		+ "			}\n"
		    		+ "\n"
		    		+ "			.stack .column {\n"
		    		+ "				width: 100%;\n"
		    		+ "				display: block;\n"
		    		+ "			}\n"
		    		+ "		}\n"
		    		+ "	</style>\n"
		    		+ "</head>\n"
		    		+ "\n"
		    		+ "<body style=\"background-color: #FFFFFF; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;\">\n"
		    		+ "	<table class=\"nl-container\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #FFFFFF;\">\n"
		    		+ "		<tbody>\n"
		    		+ "			<tr>\n"
		    		+ "				<td>\n"
		    		+ "					<table class=\"row row-1\" align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n"
		    		+ "						<tbody>\n"
		    		+ "							<tr>\n"
		    		+ "								<td>\n"
		    		+ "									<table class=\"row-content stack\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000;\" width=\"640\">\n"
		    		+ "										<tbody>\n"
		    		+ "											<tr>\n"
		    		+ "												<th class=\"column\" width=\"100%\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 50px; padding-bottom: 0px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\">\n"
		    		+ "													<table class=\"text_block\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\">\n"
		    		+ "														<tr>\n"
		    		+ "															<td style=\"padding-bottom:30px;padding-left:10px;padding-right:10px;padding-top:10px;\">\n"
		    		+ "																<div style=\"font-family: sans-serif\">\n"
		    		+ "																	<div style=\"font-family: Helvetica Neue, Helvetica, Arial, sans-serif; font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #555555; line-height: 1.2;\">\n"
		    		+ "																		<p style=\"margin: 0; font-size: 14px; text-align: center;\"><span style=\"font-size:58px;\"><strong><span style=\"color:#fe702c;font-size:58px;\">Money Manager</span></strong></span></p>\n"
		    		+ "																	</div>\n"
		    		+ "																</div>\n"
		    		+ "															</td>\n"
		    		+ "														</tr>\n"
		    		+ "													</table>\n"
		    		+ "												</th>\n"
		    		+ "											</tr>\n"
		    		+ "										</tbody>\n"
		    		+ "									</table>\n"
		    		+ "								</td>\n"
		    		+ "							</tr>\n"
		    		+ "						</tbody>\n"
		    		+ "					</table>\n"
		    		+ "					<table class=\"row row-2\" align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n"
		    		+ "						<tbody>\n"
		    		+ "							<tr>\n"
		    		+ "								<td>\n"
		    		+ "									<table class=\"row-content stack\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000;\" width=\"640\">\n"
		    		+ "										<tbody>\n"
		    		+ "											<tr>\n"
		    		+ "												<th class=\"column\" width=\"100%\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 0px; padding-bottom: 0px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\">\n"
		    		+ "													<table class=\"text_block\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\">\n"
		    		+ "														<tr>\n"
		    		+ "															<td style=\"padding-top:45px;padding-right:10px;padding-bottom:45px;padding-left:10px;\">\n"
		    		+ "																<div style=\"font-family: sans-serif\">\n"
		    		+ "																	<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #393d47; line-height: 1.2; font-family: Helvetica Neue, Helvetica, Arial, sans-serif;\">\n"
		    		+ "																		<p style=\"margin: 0; font-size: 14px; text-align: center;\">" + body + "</p>\n"
		    		+ "																	</div>\n"
		    		+ "																</div>\n"
		    		+ "															</td>\n"
		    		+ "														</tr>\n"
		    		+ "													</table>\n"
		    		+ "												</th>\n"
		    		+ "											</tr>\n"
		    		+ "										</tbody>\n"
		    		+ "									</table>\n"
		    		+ "								</td>\n"
		    		+ "							</tr>\n"
		    		+ "						</tbody>\n"
		    		+ "					</table>\n"
		    		+ "					<table class=\"row row-3\" align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n"
		    		+ "						<tbody>\n"
		    		+ "							<tr>\n"
		    		+ "								<td>\n"
		    		+ "									<table class=\"row-content stack\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000;\" width=\"640\">\n"
		    		+ "										<tbody>\n"
		    		+ "											<tr>\n"
		    		+ "												<th class=\"column\" width=\"100%\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\">\n"
		    		+ "													<table class=\"icons_block\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n"
		    		+ "														<tr>\n"
		    		+ "															<td style=\"color:#9d9d9d;font-family:inherit;font-size:15px;padding-bottom:5px;padding-top:5px;text-align:center;\">\n"
		    		+ "																<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n"
		    		+ "																	<tr>\n"
		    		+ "																		<td style=\"text-align:center;\">\n"
		    		+ "																			<!--[if vml]><table align=\"left\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;\"><![endif]-->\n"
		    		+ "																			<!--[if !vml]><!-->\n"
		    		+ "																			<table class=\"icons-inner\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; margin-right: -4px; padding-left: 0px; padding-right: 0px;\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\">\n"
		    		+ "																				<!--<![endif]-->\n"
		    		+ "																				<tr>\n"
		    		+ "\n"
		    		+ "																				</tr>\n"
		    		+ "																			</table>\n"
		    		+ "																		</td>\n"
		    		+ "																	</tr>\n"
		    		+ "																</table>\n"
		    		+ "															</td>\n"
		    		+ "														</tr>\n"
		    		+ "													</table>\n"
		    		+ "												</th>\n"
		    		+ "											</tr>\n"
		    		+ "										</tbody>\n"
		    		+ "									</table>\n"
		    		+ "								</td>\n"
		    		+ "							</tr>\n"
		    		+ "						</tbody>\n"
		    		+ "					</table>\n"
		    		+ "				</td>\n"
		    		+ "			</tr>\n"
		    		+ "		</tbody>\n"
		    		+ "	</table><!-- End -->\n"
		    		+ "</body>\n"
		    		+ "\n"
		    		+ "</html>";
		    
		    
		    message.setContent(html, "text/html; charset=utf-8");

			Transport.send(message);
			return true;

		} catch (MessagingException e) {
			System.out.println("Error "+ e);
			return false;
		}
	}
}
