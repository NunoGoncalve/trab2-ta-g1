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


    public Coin(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public Coin(int ID) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            String sql = "SELECT *, (Select Value from CoinHistory Where Coin= ? Order By Date Desc limit 1) as Value FROM Coin WHERE ID = ? ";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ID);
            stmt.setInt(2, ID);
            stmt.executeQuery();
            ResultSet CoinResult = stmt.getResultSet();
            if (CoinResult.next()) {
                this.ID = CoinResult.getInt("ID");
                this.name = CoinResult.getString("Name");
                this.value = CoinResult.getDouble("Value");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public double getValue() {
        return value;
    }
}
