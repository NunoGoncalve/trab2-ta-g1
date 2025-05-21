package com.example.registo01;

public class Wallet {
    Double balance;
    String currency;

    public Wallet(Double balance, String currency) {
        this.balance = balance;
        this.currency = currency;
    }

    public Double SetBalance(Double balance) {
        this.balance = balance;
    }
}
