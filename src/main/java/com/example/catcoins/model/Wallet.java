package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Wallet {
    private int ID;
    private Double Balance;
    private String Currency;
    public Wallet(int ID, Double balance, String currency) {
        this.ID = ID;
        this.Balance = balance;
        this.Currency = currency;
    }

    private ResultSet GetPortfolio(){

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String GetCoins = "Select * From Portfolio Where WalletID = ?";
            PreparedStatement Stmt = conn.prepareStatement(GetCoins);
            Stmt.setInt(1, this.ID);
            ResultSet result = Stmt.executeQuery();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao salvar preferência de moeda: " + e.getMessage());
        }
        return null;

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
            System.err.println("Erro ao salvar preferência de moeda: " + e.getMessage());
        }
        return 0;

    }

    public void SetBalance(Double balance) {
        this.Balance = balance;
    }

    public void SetCurrency(String currency) {
        this.Currency = currency;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // Atualiza a moeda na tabela Wallet
            String updateCurrencySql = "UPDATE Wallet SET Currency = ? WHERE ID = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateCurrencySql);
            updateStmt.setString(1, Currency);
            updateStmt.setInt(2, ID);
            updateStmt.executeUpdate();
            System.out.println("Preferência de moeda salva no banco de dados: " + Currency);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao salvar preferência de moeda: " + e.getMessage());
        }
    }

    public void UpdateBalance() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // Atualiza a moeda na tabela Wallet
            String updateCurrencySql = "Select Balance From Wallet WHERE ID = ?";
            PreparedStatement Stmt = conn.prepareStatement(updateCurrencySql);
            Stmt.setInt(1, this.ID);
            ResultSet rs = Stmt.executeQuery();
            rs.next();
            this.Balance = rs.getDouble("Balance");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao salvar preferência de moeda: " + e.getMessage());
        }
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
}
