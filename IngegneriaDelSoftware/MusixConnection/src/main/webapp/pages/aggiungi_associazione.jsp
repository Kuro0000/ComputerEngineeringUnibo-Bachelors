<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="it.unibo.tw.web.model.Utente" %>
<%
	String ctx = request.getContextPath();
	Utente utente = (Utente) session.getAttribute("utente");
	if (utente == null) {
		response.sendRedirect(ctx + "/pages/login.jsp");
		return;
	}
	String errore = request.getParameter("errore");
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title>Nuova Associazione - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body class="center">
	<div class="card narrow" style="width:380px">
		<img src="<%= ctx %>/images/logo.png" class="logo small" alt="MusixConnection">
		<h2>Nuova Associazione</h2>

		<% if ("dati".equals(errore)) { %>
			<div class="msg-err">Compila Nome, CF valido e almeno una tipologia.</div>
		<% } else if ("cf".equals(errore)) { %>
			<div class="msg-err">Esiste gia' un'associazione con questo Codice Fiscale.</div>
		<% } %>

		<form method="post" action="<%= ctx %>/Associazione">
			<input type="hidden" name="action" value="create">
			<input type="text" name="nome" placeholder="Nome" required>
			<input type="text" name="cf" placeholder="Codice Fiscale (11 cifre o 16 caratteri)" required>
			<input type="email" name="email" placeholder="Email">
			<input type="text" name="indirizzo" placeholder="Indirizzo">

			<div style="text-align:left;margin:10px 0">
				<label><input type="checkbox" name="tipo" value="Strumento" style="width:auto"> Strumento (musicale)</label><br>
				<label><input type="checkbox" name="tipo" value="Danza" style="width:auto"> Danza</label>
			</div>

			<button type="submit" class="full">Registra</button>
		</form>
		<a class="block" href="<%= ctx %>/pages/home.jsp">← Torna alla Home</a>
	</div>
</body>
</html>
