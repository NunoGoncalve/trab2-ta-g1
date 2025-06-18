package com.example.catcoins.model;

public class BalanceHistory {
    private double Balance;
    private double PendingBalance;
    private String date;

    public BalanceHistory(String date, double pendingBalance, double balance) {
        this.date = date;
        PendingBalance = pendingBalance;
        Balance = balance;
    }

    public String getDate() {
        return date;
    }

    public double getPendingBalance() {
        return PendingBalance;
    }

    public double getBalance() {
        return Balance;
    }
}
