package com.example.catcoins;

public class Client extends User{

    Wallet wallet;

    public Client(int id, String name, String email, Role role, Status status, Wallet wallet) {

        super(id, name, email, role, status);
        this.wallet = wallet;
    }

    public void SetWallet(Wallet ClientWallet) {
        this.wallet = ClientWallet;

    }

    public Wallet getWallet() {
        return wallet;
    }
}
