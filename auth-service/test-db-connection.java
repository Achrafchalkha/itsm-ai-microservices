import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDbConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/auth_db";
        String username = "postgres";
        String password = "achrafmas03";
        
        try {
            System.out.println("Testing PostgreSQL connection...");
            
            // Load the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Establish connection
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connection successful!");
            
            // Test query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT version()");
            
            if (resultSet.next()) {
                System.out.println("PostgreSQL Version: " + resultSet.getString(1));
            }
            
            // Test database existence
            ResultSet dbResult = statement.executeQuery("SELECT datname FROM pg_database WHERE datname = 'auth_db'");
            if (dbResult.next()) {
                System.out.println("✅ Database 'auth_db' exists!");
            } else {
                System.out.println("❌ Database 'auth_db' not found!");
            }
            
            connection.close();
            System.out.println("✅ Connection closed successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
