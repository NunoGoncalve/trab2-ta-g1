package com.example.catcoins;

public class Transaction {
    private int id;
    private String type;
    private String coin;
    private double value;
    private double amount;
    private String date;

    public Transaction(int id, String type, String coin, double value, double amount, String date) {
        this.id = id;
        this.type = type;
        this.coin = coin;
        this.value = value;
        this.amount = amount;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCoin() {
        return coin;
    }

    public double getValue() {
        return value;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
