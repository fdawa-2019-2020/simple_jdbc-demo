package demo.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

public class Main {
	

	public static void main(String[] args) throws SQLException {
		try(Connection conn  = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "")) {
			
			createPeopleTable(conn);

			insertOneRecordInPeopleTable(conn);
			
			selectAllFromPeopleTable(conn);
			
			createSeveralPeopleRecords(conn);

			selectAllFromPeopleTable(conn);

			injectionWithSimpleStatement(conn, "'Doe'");
			
			injectionWithSimpleStatement(conn, "'Doe' OR 1=1");

			injectionWithPreparedPreparedStatement(conn, "Doe");

			injectionWithPreparedPreparedStatement(conn, "'Doe' OR 1=1");
			
			getMetadata(conn);

		} 

	}
	
	
	private static void getMetadata(Connection conn) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		try(ResultSet catalogRs = metaData.getCatalogs()) {
			System.out.println(catalogRs.getFetchSize());
			while(catalogRs.next()) {
				String catalogName = catalogRs.getString(1);
				try(ResultSet tableRs = metaData.getTables(catalogName, null, "PEOPLE", null)) {
					ResultSetMetaData tableRsMetaData = tableRs.getMetaData();
					while( tableRs.next() ) {
						int size = tableRsMetaData.getColumnCount();
						for (int i = 1; i < size+1; i++) {
							System.out.print(tableRsMetaData.getColumnName(i)+" "+tableRs.getObject(i)+" | ");
						}
					}					
				}
			}
		}
	}


	private static void insertOneRecordInPeopleTable(Connection conn) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(
					"INSERT INTO People "
					+ "(firstname, lastname, age, pswd) "
					+ "VALUES ('John', 'Doe', 45, 'areze');"
				);
			
		}		
	}


	private static void injectionWithSimpleStatement(Connection conn, String parameter) throws SQLException {
		System.out.println("\nTentative d'injection SELECT * FROM People WHERE lastname="+parameter+";");
		try(Statement stmt = conn.createStatement()) {
			try(ResultSet rs = stmt.executeQuery("SELECT * FROM People WHERE lastname="+parameter+";")){
				iterateThrough(rs);		
			}
		}
	}

	private static void injectionWithPreparedPreparedStatement(Connection conn, String parameter) throws SQLException {
		System.out.println("\nTentative d'injection SELECT * FROM People WHERE lastname="+parameter+";");
		
		try(PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM People WHERE lastname= ?")) {
			pStmt.setString(1, parameter);

			try(ResultSet rs = pStmt.executeQuery()) {
				iterateThrough(rs);		
			}			
		}
	}
	

	private static void selectAllFromPeopleTable(Connection conn) throws SQLException {
		System.out.println("--- Select all with \"SELECT * FROM People;\"");
		try(Statement stmt = conn.createStatement()) {
			try(ResultSet rs = stmt.executeQuery("SELECT * FROM People;")) {
				iterateThrough(rs);
			}
		}
	}

	private static void iterateThrough(ResultSet rs) throws SQLException {
		while(rs.next()) {
			String message = String.format("ID=%4s, Firstname=%15s, Lastname=%15s, Age=%4s, Password=%12s", 
					rs.getInt(1), 
					rs.getString(2),
					rs.getString(3),
					rs.getInt(4),
					rs.getString(5));

			System.out.println(message);
		}
	}

	private static void createSeveralPeopleRecords(Connection conn) throws SQLException {
		
		String sql = "INSERT INTO People "
					+ "(firstname, lastname, age, pswd) "
					+ "VALUES (?, ?, ?, ?);";

		try(PreparedStatement pStmt = conn.prepareStatement(sql)){

			String[][] firstAndLastnames = getFirstAndLastnames();
			
			Random random = new Random();
	
			for (int i = 0; i < firstAndLastnames.length; i++) {
				String[] name = firstAndLastnames[i];
				String firstname = name[0];
				String lastname  = name[1];
	
				pStmt.setString(1, firstname);
				pStmt.setString(2, lastname);
				pStmt.setInt(3, random.nextInt(100));
				pStmt.setString(4, UUID.randomUUID().toString().substring(0, 12));
				
				pStmt.execute();
			}
		}
	}
	
	


	private static void createPeopleTable(Connection conn) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			String createTableSQL = "CREATE TABLE People (\n" + 
					"id INTEGER IDENTITY PRIMARY KEY,\n" + 
					"firstname VARCHAR(120) NOT NULL,\n" + 
					"lastname VARCHAR(120) NOT NULL,\n" + 
					"age INTEGER \n,"
					+ "pswd VARCHAR(12) NOT NULL)";
			stmt.execute(createTableSQL);
			
			System.out.println("--- Table Creation with ---\n"+createTableSQL);
		}

	}
	
	private static String[][] getFirstAndLastnames() {
		return new String[][] {
			{ "Nola", "Lang" },
			{ "Marley", "Cole" },
			{ "Jack", "Davenport" },
			{ "Jocelyn", "Rich" },
			{ "Cameron", "Rosario" },
			{ "Jaime", "Morales" },
			{ "Evie", "Dudley" }
		};
	}

}
