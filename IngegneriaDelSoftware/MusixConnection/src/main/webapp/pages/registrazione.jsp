<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
	String ctx = request.getContextPath();
	String errore = request.getParameter("errore");
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title>Registrazione - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body class="center">
	<div class="card narrow">
		<img src="<%= ctx %>/images/logo.png" class="logo" alt="MusixConnection">
		<h2>Registrati</h2>
		<% if ("esistente".equals(errore)) { %>
			<div class="msg-err">Username gia' in uso, scegline un altro.</div>
		<% } else if ("campi".equals(errore)) { %>
			<div class="msg-err">Compila tutti i campi.</div>
		<% } %>
		<form method="post" action="<%= ctx %>/Registrazione">
			<input type="text" name="username" placeholder="Username" required>
			<input type="password" name="password" placeholder="Password" required>
			<button type="submit" class="full">Crea Account</button>
		</form>
		<a class="block" href="<%= ctx %>/pages/login.jsp">Hai gia' un account? Login</a>
	</div>
</body>
</html>
