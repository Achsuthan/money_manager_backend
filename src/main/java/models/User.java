package models;

import org.json.JSONObject;

public class User {
	private String userId;
	private String name;
	private String email;
	private Double amount;
	private Double persentage;
	
	//Get 
	public String getUserId() {
		return this.userId;
	}
	
	public String getName() {
		return this.email;
	}
	
	public String getEmail() {
		return this.name;
	}
	
	public Double getAmount() {
		return this.amount;
	}
	
	public Double getPersentage() {
		return this.persentage;
	}
	
	
	//Set 
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	public void setPersentage(Double persentage) {
		this.persentage = persentage;
	}
	
	public JSONObject getUserObject() throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("userId", this.userId);
		obj.put("name", this.name);
		obj.put("email", this.email);
		obj.put("amount", this.amount);
		obj.put("persentage", this.persentage);
		return obj;
	}
}
