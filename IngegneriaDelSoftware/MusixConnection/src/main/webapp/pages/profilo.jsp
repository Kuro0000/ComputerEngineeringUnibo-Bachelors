<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="it.unibo.tw.web.model.Utente" %>
<%@ page import="it.unibo.tw.web.model.Costanti" %>
<%
	String ctx = request.getContextPath();
	Utente utente = (Utente) session.getAttribute("utente");
	if (utente == null) {
		response.sendRedirect(ctx + "/pages/login.jsp");
		return;
	}
	String aggiunto = request.getParameter("aggiunto");
	String errore = request.getParameter("errore");
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title>Il mio profilo - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body>
	<div class="topbar">
		<a href="<%= ctx %>/pages/home.jsp">← Home</a>
		<a href="<%= ctx %>/Logout">Logout</a>
	</div>

	<div class="card wide">
		<img src="<%= ctx %>/images/logo.png" class="logo small" alt="MusixConnection">
		<h1>Profilo di <%= utente.getUsername() %></h1>

		<% if (aggiunto != null) { %><div class="msg-ok">Competenza aggiunta.</div><% } %>
		<% if (errore != null) { %><div class="msg-err">Selezione non valida.</div><% } %>

		<div class="section">
			<h3>Strumenti praticati</h3>
			<% if (utente.getStrumenti().isEmpty()) { %><p>Nessuno.</p><% } %>
			<% for (String s : utente.getStrumenti()) { %><span class="tag"><%= s %></span><% } %>
		</div>

		<div class="section">
			<h3>Stili praticati</h3>
			<% if (utente.getStili().isEmpty()) { %><p>Nessuno.</p><% } %>
			<% for (String s : utente.getStili()) { %><span class="tag"><%= s %></span><% } %>
		</div>

		<div class="section">
			<h3>Aggiungi uno strumento o uno stile</h3>
			<form method="post" action="<%= ctx %>/Profilo" class="inline-form">
				<select name="valore" required>
					<optgroup label="Strumenti">
						<% for (String s : Costanti.STRUMENTI) { %><option value="<%= s %>"><%= s %></option><% } %>
					</optgroup>
					<optgroup label="Stili">
						<% for (String s : Costanti.STILI) { %><option value="<%= s %>"><%= s %></option><% } %>
					</optgroup>
				</select>
				<button type="submit" class="dark">Aggiungi</button>
			</form>
		</div>
	</div>
</body>
</html>
