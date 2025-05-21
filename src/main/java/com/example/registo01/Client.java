package com.example.registo01;

public class Client extends User{

    Wallet wallet;

    public Client(String nome, String email, Enum role, Enum status) {
        super(nome, email, role, status);
    }

    public void SetWallet(Wallet ClientWallet) {
        this.wallet = ClientWallet;

    }
}
