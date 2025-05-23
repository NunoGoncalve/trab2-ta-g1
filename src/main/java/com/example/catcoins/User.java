package com.example.catcoins;

public class User {
    private String name;
    private String email;
    private Role role;
    private Status status;


    public User(String nome, String email, Enum role, Enum status) {
        this.name = nome;
        this.email = email;
    }

    // Getters e setters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }
}
