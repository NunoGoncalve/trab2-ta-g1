package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Coin {
    int ID;
    String name;
    double value;


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Coin(int ID, String name, double value) {
        this.ID = ID;
        this.name = name;
        this.value = value;
    }

    public Coin(int ID) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            String sql = "SELECT * FROM Coin WHERE ID = ? ";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ID);
            stmt.executeQuery();
            ResultSet CoinResult = stmt.getResultSet();
            if (CoinResult.next()) {
                this.ID = CoinResult.getInt("ID");
                this.name = CoinResult.getString("Name");
                this.value = CoinResult.getDouble("value");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
