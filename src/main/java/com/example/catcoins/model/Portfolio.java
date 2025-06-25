package com.example.catcoins.model;

public class Portfolio {
    private Wallet UserWallet;
    private Coin CryptoCoin;
    private int Amount;

    public Portfolio(Wallet UserWallet, Coin CryptoCoin, int amount) {
        this.UserWallet = UserWallet;
        this.CryptoCoin = CryptoCoin;
        this.Amount = amount;
    }

    public Coin getCryptoCoin() {
        return CryptoCoin;
    }

    public Wallet getUserWallet() {
        return UserWallet;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        Amount = amount;
    }
}
