#!/bin/bash

# ==============================================================================
# Script di configurazione e avvio per MusixConnection (Linux/macOS)
# ==============================================================================

echo "🎵 Inizio configurazione di MusixConnection 🎵"
echo "------------------------------------------------"

# 1. Richiesta credenziali MySQL
read -p "Inserisci lo username di MySQL (default: root): " MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}

read -s -p "Inserisci la password di MySQL per l'utente '$MYSQL_USER': " MYSQL_PASS
echo ""

# 2. Creazione del Database
echo "Creazione del database 'musixconnection' in corso..."
mysql -u "$MYSQL_USER" -p"$MYSQL_PASS" -e "CREATE DATABASE IF NOT EXISTS musixconnection CHARACTER SET utf8mb4;"

if [ $? -eq 0 ]; then
    echo "✅ Database creato con successo (o già esistente)."
else
    echo "❌ Errore durante la creazione del database. Verifica che MySQL sia in esecuzione e le credenziali siano corrette."
    exit 1
fi

# 3. Impostazione delle variabili d'ambiente
echo "Impostazione delle variabili d'ambiente per il DB..."
export usernameDB="$MYSQL_USER"
export passwordDB="$MYSQL_PASS"

# 4. Richiesta percorso Tomcat (per sovrascrivere la proprietà del pom.xml)
echo ""
read -p "Inserisci il percorso assoluto della tua cartella Tomcat 9 (es. /opt/tomcat): " TOMCAT_PATH

# 5. Compilazione Maven
echo ""
echo "📦 Compilazione del progetto con Maven (mvn clean package)..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "❌ Errore durante la compilazione Maven."
    exit 1
fi

# 6. Avvio tramite Cargo
echo ""
echo "🚀 Avvio di Tomcat tramite Maven Cargo..."
echo "🌐 L'applicazione sarà disponibile su: http://localhost:8080/MusixConnection/"
echo "Premi CTRL+C per terminare il server."
echo "------------------------------------------------"

# Avvia passando il percorso Tomcat direttamente via command line per sovrascrivere il pom.xml
mvn cargo:run -Dtomcat.server="$TOMCAT_PATH"
