package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

	private static Connection connection;

	public static Connection getConnection() throws SQLException {
		if(connection == null || connection.isClosed()){
			connection = createNewConnection();
		}
		return connection;
	}

	private static Connection createNewConnection() {

		try{
			String driver = "com.mysql.jdbc.Driver";
			String url = "jdbc:mysql://localhost/finalprojectdb";
			String username = "scott";
			String password = "tiger";
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, username, password);
			return connection;
		}
		catch(Exception e){
			System.out.println(e);
		}

		return null;

	}
}
