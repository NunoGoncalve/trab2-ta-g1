package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BalanceHistoryDAO implements DAO<Wallet> {

    public Wallet GetByID(int id) throws SQLException {
        String sql = "SELECT WalletID, Balance, PendingBalance FROM UserClientWallet WHERE ID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return null;
            } else {
                System.out.println("No wallet found with ID: " + id);
                return null;
            }

        }
    }

    public List<Wallet> GetAll(){
        return null;
    }

    public Wallet Add(Wallet object){
        return null;
    }

    public boolean Update(Wallet wlt) throws SQLException{
        String updateCurrencySql = "Update Wallet Set Balance = ?, PendingBalance = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement Stmt = conn.prepareStatement(updateCurrencySql)){
            Stmt.setDouble(1, wlt.getBalance());
            Stmt.setDouble(2, wlt.getPendingBalance());
            Stmt.setInt(3, wlt.getID());
            int rowsAffected = Stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public ArrayList<BalanceHistory> GetWalletBalanceHistory(Wallet wlt) throws SQLException {
        String query = "Select * From BalanceHistory WHERE Wallet = ?";
        ArrayList<BalanceHistory> BalanceHistoryList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement Stmt = conn.prepareStatement(query);
            Stmt.setInt(1, wlt.getID());
            ResultSet rs = Stmt.executeQuery();
            while (rs.next()) {
                BalanceHistory BlncHstr= new BalanceHistory(
                        rs.getTimestamp("Date"),
                        rs.getDouble("PendingBalance"),
                        rs.getDouble("Balance")
                );
                BalanceHistoryList.add(BlncHstr);
            }
            return BalanceHistoryList;
        }
    }

}
