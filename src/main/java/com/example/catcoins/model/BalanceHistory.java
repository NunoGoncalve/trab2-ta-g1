package com.example.catcoins.model;

import java.sql.Timestamp;

public class BalanceHistory {
    private double Balance;
    private double PendingBalance;
    private Timestamp date;

    public BalanceHistory(Timestamp date, double pendingBalance, double balance) {
        this.date = date;
        PendingBalance = pendingBalance;
        Balance = balance;
    }

    public Timestamp getDate() {
        return date;
    }

    public double getPendingBalance() {
        return PendingBalance;
    }

    public double getBalance() {
        return Balance;
    }
}
