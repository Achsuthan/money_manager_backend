package constants;

public class FriendRequestConstraints {
	
	//Common
	public static String firstRequestId = "FRI-11111111";
	
	//Success
	public static String acceptedSuccessfully = "Friend request accepted successfully";
	public static String getFriendsSuccess = "Get all friend request successfully";
	public static String friendsCreatedSuccessfully = "Request Created successfully";

	
	//failure
	public static String requestExists = "Friend request already exists";
	public static String sameUserRequest = "You can't send request to you";
	public static String requestNotExisit = "Friend request is not exisists";
	public static String alreayFriends = "You are already friends with the requested person";
	public static String friendsCreationFailed = "Something went wrond when create friends please try again later";
	public static String noAccess = "You don't have the access";
	public static String deletedSuccess = "Friend request deleted successfuly";
}
