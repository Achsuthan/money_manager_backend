package models;

import org.json.JSONObject;

public class Group {
	private String groupId;
	private String groupName;
	
	
	public String getGroupId() {
		return this.groupId;
	}
	
	public String getGroupName() {
		return this.groupName;
	}
	
	
	public void setGroupId(String id) {
		this.groupId = id;
	}
	
	public void setGroupName(String name) {
		this.groupName = name;
	}
	
	public JSONObject getGroupObject() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("groupId", this.groupId);
		obj.put("grouName", this.groupName);
		return obj;
	}
}
