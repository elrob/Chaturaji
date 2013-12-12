<!DOCTYPE HTML>
<html>
<%@ page pageEncoding="utf-8"%>
<%
	session.invalidate();
%>
<head>
<title>Imperial Chaturaji</title>
<link rel="stylesheet" type="text/css" href="./login_style.css">

<!-- <script type="text/javascript">
	function login() {
		username.innerText = "It Works!"
		return false
	}
</script> -->

</head>

<body>

	<h1>Imperial Chaturaji</h1>

	<div id="content">

		<p>
			<img src="./images/logo(black).svg" alt="Imperial Chaturaji Logo"
				width="115" height="93">
		</p>

		<form id="login_form" action="GLS" method="POST">
			username <input id="username" type="text" name="userName">
			<!-- password<input type="password" name ="password"> -->
			<a> <%
 					if (request.getAttribute("result") != null) {
 						out.println(request.getAttribute("result"));
 					}
 				%>	</a> <input id="login_button" type="submit" value="login">
		</form>
		<div id="noscriptmsg">Imperial Chaturaji requires JavaScript.
				Please use a browser with JavaScript enabled and try again.</div>
				
		<script type="text/javascript">
		    document.getElementById('login_form').style.display = 'block';
    		document.getElementById('noscriptmsg').style.display = 'none';
		</script>
		
	</div>

</body>
</html>
