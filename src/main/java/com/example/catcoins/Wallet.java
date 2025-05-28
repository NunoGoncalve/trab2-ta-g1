package com.example.catcoins;

public class Wallet {
    int ID;
    Double balance;
    String currency;

    public Wallet(int ID, Double balance, String currency) {
        this.balance = balance;
        this.currency = currency;
    }

    public Double SetBalance(Double balance) {
        this.balance = balance;
        return balance;
    }

    public Double getBalance() {
        return balance;
    }
}
