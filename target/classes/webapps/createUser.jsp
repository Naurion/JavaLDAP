<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="./script.js"></script>
<title>Create new user</title>
</head>
<body>
<script type="text/javascript">
	function doIt() {
		alert(str);
	}
</script>
	<form action = "CreateUser" method = "POST">
         First name: <input type = "text" name = "firstName">
         <br />
         Last name: <input type = "text" name = "lastName" />
         <br />
         Login name: <input type = "text" name = "loginName" />
         <br />
         Password: <input type = "password" name = "password">
         <br />
         Group: <input type = "text" name = "group">
         <br />
         <input type = "submit" value = "Create" />
	</form>

    <br />
    <form action="CreateUserFromFile" enctype="multipart/form-data" method="POST">
    	<input type="file" name="filename"/>
    	<br />
    	<input type="button" onclick="doIt(1)" value="DO IT!">
    	<input type="submit" onclick="doIt(1)" value= "Create from file"/>
    </form>
</body>
</html>