package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Wallet {
    private int ID;
    private Double Balance;
    private Double PendingBalance;
    private String Currency;
    public Wallet(int ID, Double balance, Double PendingBalance,String currency) {
        this.ID = ID;
        this.Balance = balance;
        this.Currency = currency;
        this.PendingBalance = PendingBalance;
    }

    public int GetCoinAmount(int CoinID){

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String GetCoins = "Select Amount From Portfolio Where WalletID = ? and CoinID = ?";
            PreparedStatement Stmt = conn.prepareStatement(GetCoins);
            Stmt.setInt(1, this.ID);
            Stmt.setInt(2, CoinID);
            ResultSet result = Stmt.executeQuery();
            result.next();
            return result.getInt("Amount");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;

    }

    public void GetUpdatedBalance() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // Atualiza a moeda na tabela Wallet
            String updateCurrencySql = "Select Balance,PendingBalance From Wallet WHERE ID = ?";
            PreparedStatement Stmt = conn.prepareStatement(updateCurrencySql);
            Stmt.setInt(1, this.ID);
            ResultSet rs = Stmt.executeQuery();
            rs.next();
            this.Balance = rs.getDouble("Balance");
            this.PendingBalance = rs.getDouble("PendingBalance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean SetBalance(Double balance, Double PendingBalance) throws SQLException {
        WalletDAO WltDao = new WalletDAO();
        this.Balance = balance;
        this.PendingBalance = PendingBalance;
        return WltDao.Update(this);
    }

    public void UpdatePortfolio(int Amount, int CoinID) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String updateCurrencySql = "Update Portfolio Set Amount = ? WHERE WalletID = ? and CoinID = ?";
            PreparedStatement Stmt = conn.prepareStatement(updateCurrencySql);
            Stmt.setInt(1, Amount);
            Stmt.setInt(2, this.ID);
            Stmt.setInt(3, CoinID);
            Stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setBalance(Double balance) {
        Balance = balance;
    }

    public void setPendingBalance(Double pendingBalance) {
        PendingBalance = pendingBalance;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public int getID() {
        return ID;
    }

    public String getCurrency() {
        return Currency;
    }

    public Double getBalance() {
        return Balance;
    }

    public Double getPendingBalance() {
        return PendingBalance;
    }
}
