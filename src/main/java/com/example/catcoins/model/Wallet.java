package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Wallet {
    private int ID;
    private Double Balance;
    private String Currency;

    public Wallet(int ID, Double balance, String currency) {
        this.ID = ID;
        this.Balance = balance;
        this.Currency = currency;
    }

    public Double SetBalance(Double balance) {
        this.Balance = balance;
        return balance;
    }

    public void setCurrency(String currency) {
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
