package models;

import java.sql.Date;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Transaction {
	
	private String transcationId;
	private String name;
	private String description;
	private Double amount;
	private Date date;
	private Category category;
	private Boolean isOwn;
	private Group group;
	private ArrayList<User> friends= new ArrayList<User>();
	
	
	//Get 
	public String getTransactionId() {
		return this.transcationId;
	}
	
	public String getTransactionName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public Double getAmount() {
		return this.amount;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public Category getCategory() {
		return this.category;
	}
	
	public Boolean getIsOwn() {
		return this.isOwn;
	}
	
	public Group getGroup() {
		return this.group;
	}
	
	public ArrayList<User> getFriends(){
		return this.friends;
	}
	
	public Boolean isTransactionExist(String id) {
		return this.transcationId.equals(id);
	}
	
	
	//Set 
	
	public void setTransactionId(String id) {
		this.transcationId = id;
	}
	
	public void setTransactionName(String name) {
		this.name = name;
	}
	
	public void setDescription(String des) {
		this.description = des;
	}
	
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setCategory(Category cat) {
		this.category = cat;
	}
	
	public void setIsOwn(Boolean isOwn) {
		this.isOwn = isOwn;
	}
	
	public void setFriends(User user) {
		this.friends.add(user);
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	
	public JSONObject getTransactionObject() throws Exception {
		
		JSONObject obj = new JSONObject();
		obj.put("transactionId", this.transcationId);
		obj.put("transactionName", this.name);
		obj.put("transactionDescription", this.description);
		obj.put("amount", this.amount);
		obj.put("date", this.date);
		obj.put("isOwn", this.isOwn);
		obj.putOpt("category", this.category.getCategoryObject());
		
		if(this.group != null) {
			obj.putOpt("group", this.group.getGroupObject());
		}
		
		JSONArray userArray = new JSONArray();
		
		for (User friend : new ArrayList<User>(friends)) {
			userArray.put(friend.getUserObject());
		}
		obj.put("friends", userArray);
		return obj;
	}
}
