package com.example.catcoins.model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private String type;
    private Wallet orderwallet;
    private Coin coin;
    private double value;
    private int amount;
    private Timestamp date;
    private OrderStatus Status;

    public Order(int id, String type, Wallet orderwallet, Coin cryptocoin, double value, int amount, Timestamp date,  OrderStatus status) {
        this.id = id;
        this.type = type;
        this.orderwallet = orderwallet;
        this.coin = cryptocoin;
        this.value = value;
        this.amount = amount;
        this.date = date;
        this.Status = status;
    }

    public Order(String type, Wallet orderwallet, Coin cryptocoin, double value, int amount, Timestamp date,  OrderStatus status) {
        this.id = -1;
        this.type = type;
        this.orderwallet = orderwallet;
        this.coin = cryptocoin;
        this.value = value;
        this.amount = amount;
        this.date = date;
        this.Status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return Status;
    }

    public void setStatus(OrderStatus status) {
        Status = status;
    }

    public String getType() {
        return type;
    }

    public Coin getCoin() {
        return coin;
    }

    public double getValue() {
        return value;
    }

    public void setID(int id) {
        this.id = id;
    }

    public Wallet getOrderwallet() {
        return orderwallet;
    }

    public int getAmount() {
        return amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
