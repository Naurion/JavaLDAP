package main;

public class User {
	private String firstName;
	private String surName;
	private String password;
	private String login;

	public User(String firstName, String surName, String password) {
		this.firstName = firstName;
		this.surName = surName;
		this.password = password;
		this.login = createLoginName();
	}

	public User(String firstName, String surName, String password, String login) {
		super();
		this.firstName = firstName;
		this.surName = surName;
		this.password = password;
		this.login = login;
	}

	private String createLoginName() {
		String loginName = this.firstName.substring(0, 1).toLowerCase() + "." + this.surName.toLowerCase();
		return loginName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

}
