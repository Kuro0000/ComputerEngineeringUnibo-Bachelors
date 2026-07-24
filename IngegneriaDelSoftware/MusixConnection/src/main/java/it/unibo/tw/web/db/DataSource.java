package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Sorgente delle connessioni al DBMS (metodologia "forza bruta").
 *
 * <p>Le credenziali sono lette da variabili d'ambiente ({@code usernameDB},
 * {@code passwordDB}) come da convenzione del corso. Il nome del database e' fisso.
 * Per il prototipo si utilizza MySQL.</p>
 */
public class DataSource {

	// tipo di DBMS utilizzato
	private int usedDb;

	// nome del database
	private String dbName = "musixconnection";
	private String userName = System.getenv("usernameDB");
	private String password = System.getenv("passwordDB");

	public static final int DB2 = 0;
	public static final int HSQLDB = 1;
	public static final int MYSQL = 2;

	public DataSource(int databaseType) {
		this.usedDb = databaseType;
	}

	public Connection getConnection() throws PersistenceException {
		String driver;
		String dbUri;

		switch (this.usedDb) {
			case DB2:
				driver = "com.ibm.db2.jcc.DB2Driver";
				dbUri = "jdbc:db2://localhost:50000/" + dbName;
				break;
			case HSQLDB:
				driver = "org.hsqldb.jdbcDriver";
				dbUri = "jdbc:hsqldb:hsql://localhost/" + dbName;
				break;
			case MYSQL:
				// Connector/J 8.x
				driver = "com.mysql.cj.jdbc.Driver";
				dbUri = "jdbc:mysql://localhost:3306/" + dbName
						+ "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Rome";
				break;
			default:
				return null;
		}

		Connection connection = null;
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(dbUri, userName, password);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e.getMessage());
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		return connection;
	}
}
