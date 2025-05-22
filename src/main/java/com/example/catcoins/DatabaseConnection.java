package com.example.catcoins;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    //private static final String URL = "jdbc:mysql://localhost:3306/testetrabalho1";
    private static final String URL = "jdbc:mysql://fixstuff.net:3306/catcoin";

    private static final String USER = "catcoin"; // teu usuário do MySQL root
    private static final String PASSWORD = "d64o99yA$"; // tua senha do MySQL vazio

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

