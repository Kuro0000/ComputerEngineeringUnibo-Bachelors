package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.unibo.tw.web.model.Utente;

/**
 * Persistenza "forza bruta" dell'entita' Utente.
 */
public class UtenteRepository {

	private DataSource dataSource;

	// === Costanti letterali per non sbagliarsi a scrivere ===============================
	private static final String TABLE = "utente";
	private static final String ID = "id";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	// == STATEMENT SQL ===================================================================
	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			USERNAME + " VARCHAR(50) NOT NULL UNIQUE, " +
			PASSWORD + " VARCHAR(100) NOT NULL, " +
			"PRIMARY KEY (" + ID + ") ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			USERNAME + ", " + PASSWORD + " ) VALUES (?,?) ";

	private static final String read_by_username = "SELECT " + ID + ", " + USERNAME + ", " + PASSWORD +
			" FROM " + TABLE + " WHERE " + USERNAME + " = ? ";

	private static final String check_query = "SELECT " + ID + " FROM " + TABLE +
			" WHERE " + USERNAME + " = ? ";
	// ====================================================================================

	public UtenteRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void dropTable() throws PersistenceException {
		Connection conn = this.dataSource.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(drop);
		} catch (SQLException e) {
			// la tabella non esiste
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	public void createTable() throws PersistenceException {
		Connection connection = this.dataSource.getConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(create);
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	/** Inserisce l'utente e ne valorizza l'id generato dal DBMS. */
	public void persist(Utente u) throws PersistenceException {
		Connection connection = null;
		PreparedStatement pstmtInsert = null;
		PreparedStatement pstmtCheck = null;
		try {
			connection = this.dataSource.getConnection();
			pstmtInsert = connection.prepareStatement(insert);
			pstmtInsert.setString(1, u.getUsername());
			pstmtInsert.setString(2, u.getPassword());
			pstmtInsert.executeUpdate();

			pstmtCheck = connection.prepareStatement(check_query);
			pstmtCheck.setString(1, u.getUsername());
			ResultSet rs = pstmtCheck.executeQuery();
			if (rs.next()) {
				u.setId(rs.getInt(ID));
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (pstmtInsert != null) {
					pstmtInsert.close();
				}
				if (pstmtCheck != null) {
					pstmtCheck.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	/** Restituisce l'utente con lo username indicato, oppure null. */
	public Utente findByUsername(String username) throws PersistenceException {
		Utente result = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_username);
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				result = new Utente(rs.getInt(ID), rs.getString(USERNAME), rs.getString(PASSWORD));
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	/** R02NF: unicita' dello username. */
	public boolean existsUsername(String username) throws PersistenceException {
		return findByUsername(username) != null;
	}
}
