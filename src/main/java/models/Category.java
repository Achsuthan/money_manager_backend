package models;

import org.json.JSONObject;

public class Category {
	private String categoryId;
	private String name;
	private String color;
	private String imageName;
	
	//Get 
	public String getCategoryId() {
		return this.categoryId;
	}
	
	public String getCategoryName() {
		return this.name;
	}
	
	public String getColor() {
		return this.color;
	}
	
	public String getImageName() {
		return this.imageName;
	}
	
	
	//Set 
	
	public void setCategoryId(String catId) {
		this.categoryId = catId;
	}
	
	public void setCategoryName(String name) {
		this.name = name;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	public JSONObject getCategoryObject() throws Exception{
		JSONObject obj = new JSONObject();
		obj.put("categoryId", this.categoryId);
		obj.put("categoryName", this.name);
		obj.put("categoryColor", this.color);
		obj.put("imageName", this.imageName);
		return obj;
	}
}
