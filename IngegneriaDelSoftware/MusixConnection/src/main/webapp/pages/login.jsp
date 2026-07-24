<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
	String ctx = request.getContextPath();
	String errore = request.getParameter("errore");
	String registrato = request.getParameter("registrato");
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title>Login - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body class="center">
	<div class="card narrow">
		<img src="<%= ctx %>/images/logo.png" class="logo" alt="MusixConnection">
		<% if (registrato != null) { %>
			<div class="msg-ok">Registrazione completata! Ora puoi accedere.</div>
		<% } %>
		<% if (errore != null) { %>
			<div class="msg-err">Credenziali errate.</div>
		<% } %>
		<form method="post" action="<%= ctx %>/Login">
			<input type="text" name="username" placeholder="Username (pippo)" required>
			<input type="password" name="password" placeholder="Password (1234)" required>
			<button type="submit" class="full">Accedi</button>
		</form>
		<a class="block" href="<%= ctx %>/pages/registrazione.jsp">Non hai un account? Registrati</a>
	</div>
</body>
</html>
