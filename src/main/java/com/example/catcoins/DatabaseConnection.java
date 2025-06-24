package com.example.catcoins;

import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String Url = "jdbc:mysql://fixstuff.net:3306/catcoin"; //host
    private static final String User = "catcoin"; // user
    private static final String Password = "d64o99yA$"; // pass

    private DatabaseConnection() throws SQLException {
        try {
            this.connection = DriverManager.getConnection(Url, User, Password);
        } catch (SQLException e) {
            throw new SQLException("Error connecting to the database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public static void HandleConnectionError(StackPane Background, Exception e){
        AlertUtils.showAlert(Background, "It wasn't possible to connect to the database\nPlease check your internet connection and try again");
        e.printStackTrace();
        System.err.println("Database error: " + e.getMessage());
    }

}

