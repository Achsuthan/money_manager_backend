package unitls;

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

}