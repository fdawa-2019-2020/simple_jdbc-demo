package demo.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

public class Main {
	

	public static void main(String[] args) throws SQLException {
		Connection conn = null; 
		try {
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
			
			createTablePeople(conn);

			insertOneRecord(conn);
			
			selectAllFromPeople(conn);
			
			createPeopleRecords(conn);

			selectAllFromPeople(conn);
			
			injection(conn, "1 OR 1=1");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}

	}
	
	
	private static void insertOneRecord(Connection conn) throws SQLException {
		conn.createStatement().executeUpdate(
				"INSERT INTO People "
				+ "(firstname, lastname, age, pswd) "
				+ "VALUES ('John', 'Doe', 45, 'areze');"
			);
	}


	private static void injection(Connection conn, String parameter) throws SQLException {
		System.out.println("Injection");
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT * FROM People WHERE ID="+parameter+";");		
		iterateThrough(rs);
		
	}


	private static void selectAllFromPeople(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM People;");
		iterateThrough(rs);
		stmt.close();
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

	public static void createPeopleRecords(Connection conn) throws SQLException {

		PreparedStatement pStmt = conn.prepareStatement(
				"INSERT INTO People "
						+ "(firstname, lastname, age, pswd) "
						+ "VALUES (?, ?, ?, ?);"
				);


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
	
	


	public static void createTablePeople(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		stmt.execute("CREATE TABLE People (\n" + 
				"id INTEGER IDENTITY PRIMARY KEY,\n" + 
				"firstname VARCHAR(120) NOT NULL,\n" + 
				"lastname VARCHAR(120) NOT NULL,\n" + 
				"age INTEGER \n,"
				+ "pswd VARCHAR(12) NOT NULL)" );
		stmt.close();

	}
	
	private static String[][] getFirstAndLastnames() {
		return new String[][] {
			{ "Nola", "Lang" },
			{ "Marley", "Cole" },
			{ "Jack", "Davenport" },
			{ "Jocelyn", "Rich" },
			{ "Cameron", "Rosario" },
			{ "Jaime", "Morales" },
			{ "Evie", "Dudley" },
			{ "Anabella", "Buckley" },
			{ "Courtney", "Curtis" },
			{ "Alexander", "Williamson" },
			{ "Sarahi", "Weaver" },
			{ "Karma", "Sherman" },
			{ "Janae", "Gill" },
			{ "Clinton", "Burgess" },
			{ "Stacy", "Farley" },
			{ "Malik", "Howell" },
			{ "Nehemiah", "Combs" },
			{ "Marshall", "Pineda" },
			{ "Ruby", "Heath" },
			{ "Addisyn", "Hoffman" },
		};
	}

}
