package com.example.catcoins;

public class User {
    private String nome;
    private String email;
    private Role role;
    private Status status;

    public User(String nome, String email, Enum role, Enum status) {
        this.nome = nome;
        this.email = email;
    }

    // Getters e setters
    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
