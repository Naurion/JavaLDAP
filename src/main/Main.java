package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class Main {
	// Parameters for connecting to LDAP server
	// public static final String ADMINNAME = "CN=Admin, CN=Users, DC=n-box,
	// DC=com";
	//
	// public static final String PASSWORD = "!Q2w3e4r5t";
	// public static final String SERVER_URL = "ldap://192.168.129.110:389";
	// public static final String SERVER_URL = "ldaps://192.168.129.110:389"; //for
	// SSL/TLS connection.
	public static final String LDAP_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	public static final String AUTH_METHOD = "simple";
	// other variables
	private static LdapContext ctx;
	public static String GROUPNAME;
	// private static User user;
	private static String username;

	public static void connect(String serverName, String adminName, String password) {
		Hashtable<String, String> env = new Hashtable<String, String>();

		// Parameters for connection.
		env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY);
		env.put(Context.SECURITY_AUTHENTICATION, AUTH_METHOD);
		env.put(Context.PROVIDER_URL, "ldap://" + serverName + ":389");
		env.put(Context.SECURITY_PRINCIPAL, "CN=" + adminName + ", CN=Users, DC=n-box, DC=com");
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			ctx = new InitialLdapContext(env, null);
			// System.out.println("Connected!");
			searchGroups();
			// // readCSVFile();
			// ctx.close();

		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	private static boolean searchOU(String group) {
		boolean isFind = false;
		String base = "DC=n-box,DC=com";

		SearchControls sc = new SearchControls();
		String[] attributeFilter = { "CN", "OU" };
		sc.setReturningAttributes(attributeFilter);
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

		String filter = "(&(|(objectClass=organizationalUnit)(objectClass=container))(|(CN=" + group + ")(OU=" + group
				+ ")))";
		try {
			NamingEnumeration<SearchResult> results = ctx.search(base, filter, sc);
			while (results.hasMoreElements()) {
				SearchResult sr = results.next();
				// if (sr.getName().toLowerCase().equals("ou=" + group.toLowerCase())) {
				System.out.println(sr.getName());
				isFind = true;
				// }
			}

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("No more OU find.");
		}
		System.out.println(isFind);
		return isFind;
	}

	private static void searchGroups() throws NamingException {
		String[] returningAttr = { "cn" };
		SearchControls searchControl = new SearchControls();
		searchControl.setReturningAttributes(returningAttr);
		NamingEnumeration<SearchResult> result = ctx.search("CN=Builtin,DC=n-box,DC=com",
				"(&(objectClass=group)(objectSid=S-1-5-32-555))", searchControl);
		SearchResult sr = result.next();
		GROUPNAME = "CN=" + sr.getAttributes().get("CN").get() + ", CN=Builtin, DC=n-box, DC=com";
		// System.out.println(GROUPNAME);
	}

	public static void createNewUser(main.User user, String group) throws NamingException {
		if (group.toLowerCase().equals("users") || group.equals("")) {
			username = "CN=" + user.getFirstName() + ", CN=Users, DC=n-box, DC=com";
		} else {
			if (!searchOU(group)) {
				// Create OU
				createOU(group);
			}
			username = "CN=" + user.getFirstName() + ", OU=" + group + ", DC=n-box, DC=com";
		}

		try {
			Attributes attrs = new BasicAttributes(true);

			// user attribute objectClass.
			Attribute objClasses = new BasicAttribute("objectClass");
			objClasses.add("top");
			objClasses.add("person");
			objClasses.add("organizationalPerson");
			objClasses.add("user");
			attrs.put(objClasses);

			// Main attributes
			attrs.put("samAccountName", user.getLogin());// User logon name (pre-Windows 2000)
			attrs.put("userPrincipalName", user.getLogin() + "@nbox.com");// User logon name
			attrs.put("givenName", user.getFirstName());// First Name
			attrs.put("sn", user.getSurName());// Surname

			// Other attributes
			attrs.put("displayName", user.getFirstName() + " " + user.getSurName());
			// attrs.put("description", "Test user");
			// attrs.put("telephoneNumber", user.getPhone());
			// attrs.put("uid", user.getFirstName().toLowerCase());
			// attrs.put("mail", user.getMail());

			// Account attributes.
			int UF_ACCOUNTENABLE = 0x0001;
			// int UF_ACCOUNTDISABLE = 0x0002;
			int UF_PASSWD_NOTREQD = 0x0020;
			// int UF_PASSWD_CANT_CHANGE = 0x0040;//It can't be modify with programm
			// method.
			// That's write on microsoft site.
			int UF_NORMAL_ACCOUNT = 0x0200;
			int UF_DONT_EXPIRE_PASSWD = 0x10000;
			// int UF_PASSWORD_EXPIRED = 0x800000;
			Attribute userConstants = new BasicAttribute("userAccountControl",
					Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_DONT_EXPIRE_PASSWD + UF_ACCOUNTENABLE));
			attrs.put(userConstants);

			// Create new user.
			Context result = ctx.createSubcontext(username, attrs);

			// Check creation. If value of cn is right - user create.
			System.out.println("Created account for: " + ctx.getAttributes(username, new String[] { "cn" }));

			// StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new
			// StartTlsRequest());
			// tls.negotiate();

			// // Change password for user account.
			changePassword(user);

			// // add user to group Remote Desktop Users
			changeGroup();

			// Close contexts.
			// tls.close();
			result.close();

		} catch (NamingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static void createOU(String group) {
		// TODO Auto-generated method stub
		try {
			Attributes attrs = new BasicAttributes(true); // case-ignore
			Attribute objclass = new BasicAttribute("objectclass");
			objclass.add("top");
			objclass.add("organizationalUnit");
			attrs.put(objclass);

			ctx.createSubcontext("OU=" + group + ", DC=n-box, DC=com", attrs);
		}

		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void changePassword(main.User user) throws NamingException, UnsupportedEncodingException {
		ModificationItem[] mods = new ModificationItem[1];
		String newQuatedPassword = "\"" + user.getPassword() + "\"";
		byte[] newUnicodePassword = newQuatedPassword.getBytes("UTF-16LE");
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("userPassword", newUnicodePassword));
		ctx.modifyAttributes(username, mods);
		System.out.println("Password is changed.");
	}

	//
	private static void changeGroup() throws NamingException {
		ModificationItem[] mods = new ModificationItem[1];
		Attribute mod = new BasicAttribute("member", username);
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);
		ctx.modifyAttributes(GROUPNAME, mods);
		System.out.println("User add to group.");
	}

	public static void createUsersFromFile(String file) {
		String csvFile = "./src/users.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] user = line.split(cvsSplitBy);
				createNewUser(new User(user[0], user[1], user[2]), user[3]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
