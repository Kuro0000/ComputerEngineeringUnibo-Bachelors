package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistenza "forza bruta" delle nomine ad Insegnante: associa ad una iscrizione
 * lo strumento/stile (materia) per cui l'iscritto e' stato nominato (R26F).
 * Il vincolo di unicita' impedisce di nominare due volte lo stesso iscritto per
 * la stessa materia (R37F).
 */
public class InsegnamentoRepository {

	private DataSource dataSource;

	private static final String TABLE = "insegnamento";
	private static final String ID = "id";
	private static final String ID_ISCRIZIONE = "idIscrizione";
	private static final String MATERIA = "materia";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			ID_ISCRIZIONE + " INT NOT NULL, " +
			MATERIA + " VARCHAR(50) NOT NULL, " +
			"PRIMARY KEY (" + ID + "), " +
			"CONSTRAINT uq_insegnamento UNIQUE (" + ID_ISCRIZIONE + ", " + MATERIA + "), " +
			"CONSTRAINT fk_ins_iscr FOREIGN KEY (" + ID_ISCRIZIONE + ") REFERENCES iscrizione(id) " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			ID_ISCRIZIONE + ", " + MATERIA + " ) VALUES (?,?) ";

	private static final String exists_query = "SELECT " + ID + " FROM " + TABLE +
			" WHERE " + ID_ISCRIZIONE + " = ? AND " + MATERIA + " = ? ";

	private static final String read_by_iscrizione = "SELECT " + MATERIA + " FROM " + TABLE +
			" WHERE " + ID_ISCRIZIONE + " = ? ";

	public InsegnamentoRepository(DataSource dataSource) {
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

	public boolean exists(int idIscrizione, String materia) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(exists_query);
			statement.setInt(1, idIscrizione);
			statement.setString(2, materia);
			ResultSet rs = statement.executeQuery();
			return rs.next();
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

	public void persist(int idIscrizione, String materia) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(insert);
			statement.setInt(1, idIscrizione);
			statement.setString(2, materia);
			statement.executeUpdate();
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

	public List<String> readMaterie(int idIscrizione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<String> result = new ArrayList<>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_iscrizione);
			statement.setInt(1, idIscrizione);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(rs.getString(MATERIA));
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
}
