package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibo.tw.web.model.Associazione;
import it.unibo.tw.web.model.Associazioni;

/**
 * Persistenza "forza bruta" dell'Associazione (R4F) e delle sue tipologie (R5F),
 * gestite in una tabella di relazione {@code associazione_tipo}.
 * In lettura ricostruisce la catena di decoratori (Strumento/Danza).
 */
public class AssociazioneRepository {

	private DataSource dataSource;

	private static final String TABLE = "associazione";
	private static final String ID = "id";
	private static final String NOME = "nome";
	private static final String CF = "cf";
	private static final String EMAIL = "email";
	private static final String INDIRIZZO = "indirizzo";

	private static final String TABLE_TIPO = "associazione_tipo";
	private static final String AT_ID_ASSOC = "idAssociazione";
	private static final String AT_TIPO = "tipo";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			NOME + " VARCHAR(100) NOT NULL, " +
			CF + " VARCHAR(20) NOT NULL UNIQUE, " +
			EMAIL + " VARCHAR(100), " +
			INDIRIZZO + " VARCHAR(200), " +
			"PRIMARY KEY (" + ID + ") ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String create_tipo = "CREATE TABLE IF NOT EXISTS " + TABLE_TIPO + " ( " +
			AT_ID_ASSOC + " INT NOT NULL, " +
			AT_TIPO + " VARCHAR(20) NOT NULL, " +
			"PRIMARY KEY (" + AT_ID_ASSOC + ", " + AT_TIPO + "), " +
			"CONSTRAINT fk_at_assoc FOREIGN KEY (" + AT_ID_ASSOC + ") REFERENCES " + TABLE + "(" + ID + ") " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;
	private static final String drop_tipo = "DROP TABLE " + TABLE_TIPO;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			NOME + ", " + CF + ", " + EMAIL + ", " + INDIRIZZO + " ) VALUES (?,?,?,?) ";

	private static final String insert_tipo = "INSERT INTO " + TABLE_TIPO + " ( " +
			AT_ID_ASSOC + ", " + AT_TIPO + " ) VALUES (?,?) ";

	private static final String check_query = "SELECT " + ID + " FROM " + TABLE + " WHERE " + CF + " = ? ";

	private static final String read_all = "SELECT * FROM " + TABLE;

	private static final String read_tipi = "SELECT " + AT_TIPO + " FROM " + TABLE_TIPO +
			" WHERE " + AT_ID_ASSOC + " = ? ";

	public AssociazioneRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void dropTable() throws PersistenceException {
		Connection conn = this.dataSource.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(drop_tipo);
			stmt.executeUpdate(drop);
		} catch (SQLException e) {
			// le tabelle non esistono
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
			statement.executeUpdate(create_tipo);
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

	/** R09NF: il codice fiscale dell'associazione deve essere univoco. */
	public boolean existsCf(String cf) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(check_query);
			statement.setString(1, cf);
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

	/**
	 * Inserisce l'associazione con le sue tipologie e restituisce l'id generato.
	 */
	public int persist(String nome, String cf, String email, String indirizzo, Set<String> tipi)
			throws PersistenceException {
		Connection connection = null;
		PreparedStatement pstmtInsert = null;
		PreparedStatement pstmtCheck = null;
		PreparedStatement pstmtTipo = null;
		int idGenerato = -1;
		try {
			connection = this.dataSource.getConnection();
			pstmtInsert = connection.prepareStatement(insert);
			pstmtInsert.setString(1, nome);
			pstmtInsert.setString(2, cf);
			pstmtInsert.setString(3, email);
			pstmtInsert.setString(4, indirizzo);
			pstmtInsert.executeUpdate();

			pstmtCheck = connection.prepareStatement(check_query);
			pstmtCheck.setString(1, cf);
			ResultSet rs = pstmtCheck.executeQuery();
			if (rs.next()) {
				idGenerato = rs.getInt(ID);
			}

			pstmtTipo = connection.prepareStatement(insert_tipo);
			for (String tipo : tipi) {
				pstmtTipo.setInt(1, idGenerato);
				pstmtTipo.setString(2, tipo);
				pstmtTipo.executeUpdate();
			}
			return idGenerato;
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
				if (pstmtTipo != null) {
					pstmtTipo.close();
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

	private Set<String> leggiTipi(Connection connection, int idAssociazione) throws SQLException {
		Set<String> tipi = new HashSet<>();
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(read_tipi);
			pstmt.setInt(1, idAssociazione);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				tipi.add(rs.getString(AT_TIPO));
			}
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return tipi;
	}

	/** Restituisce tutte le associazioni, gia' "decorate" con le rispettive tipologie. */
	public List<Associazione> readAll() throws PersistenceException {
		Connection connection = null;
		PreparedStatement pstmt = null;
		List<Associazione> result = new ArrayList<>();
		try {
			connection = this.dataSource.getConnection();
			pstmt = connection.prepareStatement(read_all);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(ID);
				Set<String> tipi = leggiTipi(connection, id);
				Associazione a = Associazioni.costruisci(id, rs.getString(NOME), rs.getString(CF),
						rs.getString(EMAIL), rs.getString(INDIRIZZO), tipi);
				result.add(a);
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
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
