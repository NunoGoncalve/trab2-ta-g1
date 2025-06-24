package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WalletDAO implements DAO<Wallet> {

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

    public Wallet GetByUserID(int id) throws SQLException {
        String sql = "SELECT WalletID, Balance, PendingBalance FROM UserClientWallet WHERE ID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Wallet(rs.getInt("WalletID"), rs.getDouble("Balance"),rs.getDouble("PendingBalance"), "EUR");
            } else {
                System.out.println("No wallet found with ID: " + id);
                return null;
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public Wallet GetByUserID(Connection conn, int id) throws SQLException {
        String sql = "SELECT WalletID, Balance, PendingBalance FROM UserClientWallet WHERE ID = ?;";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Wallet(rs.getInt("WalletID"), rs.getDouble("Balance"),rs.getDouble("PendingBalance"), "EUR");
            } else {
                System.out.println("No wallet found with ID: " + id);
                return null;
            }

        } catch (SQLException e) {
            throw new SQLException(e);
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

    public Wallet GetUpdatedInfo(Wallet wlt) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Atualiza a moeda na tabela Wallet
            String query = "Select * From Wallet WHERE ID = ?";
            PreparedStatement Stmt = conn.prepareStatement(query);
            Stmt.setInt(1, wlt.getID());
            ResultSet rs = Stmt.executeQuery();
            rs.next();
            wlt.setBalance(rs.getDouble("Balance"));
            wlt.setPendingBalance(rs.getDouble("PendingBalance"));
            wlt.setCurrency(rs.getString("Currency"));
            return wlt;
        }
    }

}
