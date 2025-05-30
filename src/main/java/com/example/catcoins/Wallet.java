package com.example.catcoins;

public class Wallet {
    private int ID;
    private Double balance;
    private String currency;

    public Wallet(int ID, Double balance, String currency) {
        this.ID = ID;
        this.balance = balance;
        this.currency = currency;
    }

    public Double SetBalance(Double balance) {
        this.balance = balance;
        return balance;
    }

    public int getID() {
        return ID;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getBalance() {
        return balance;
    }
}
