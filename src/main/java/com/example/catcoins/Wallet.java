package com.example.catcoins;

public class Wallet {
    Double balance;
    String currency;

    public Wallet(Double balance, String currency) {
        this.balance = balance;
        this.currency = currency;
    }

    public Double SetBalance(Double balance) {
        this.balance = balance;
        return balance;
    }
}
