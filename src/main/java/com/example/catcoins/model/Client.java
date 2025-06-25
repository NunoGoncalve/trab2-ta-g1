package com.example.catcoins.model;

public class Client extends User {

    Wallet wallet;

    public Client(int id, String name, String email, String password, Role role, Status status, Wallet wallet) {

        super(id, name, email,password, role, status);
        this.wallet = wallet;
    }

    public void SetWallet(Wallet ClientWallet) {
        this.wallet = ClientWallet;

    }

    public Wallet getWallet() {
        return wallet;
    }
}
