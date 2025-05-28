package com.example.catcoins;

public class User {
    private int id;
    private String name;
    private String email;
    private Role role;
    private Status status;


    public User(int id, String name, String email, Role role, Status status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public Status getStatus() {
        return status;
    }

    // Getters e setters
    public String getName() {
        return name;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setRole(Role role) { this.role = role; }
}
