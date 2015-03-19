package exam.bitbyte;

public class FacebookFriend {
	private String id;
	private String firstName;
	private String lastName;
	
	public FacebookFriend(String id, String firstName, String lastName){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getId(){
		return id;
	}
	
	public String getFirstName(){
		return firstName;
	}
	
	public String getLastName(){
		return lastName;
	}
}
