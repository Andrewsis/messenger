package sample;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public Connection databaseLink;

    public Connection getConnection() {
        String databaseName = "clients";
        String databaseUser = "postgres";
        String databasePassword = "2405";
        String url = "jdbc:postgresql://127.0.0.1:5432/" + databaseName;

        try {
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }
}
