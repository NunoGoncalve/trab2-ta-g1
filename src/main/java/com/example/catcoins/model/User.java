package com.example.catcoins.model;

public class User {
    private int ID;
    private String Name;
    private String Email;
    private String Password;
    private Role Role;
    private Status Status;


    public String getPassword() {
        return Password;
    }

    public void setStatus(Status status) {
        Status = status;
    }

    public User(int id, String name, String email, String Password, Role role, Status status) {
        this.ID = id;
        this.Name = name;
        this.Email = email;
        this.Password = Password;
        this.Role = role;
        this.Status = status;
    }

    public User( String name, String email, String Password, Role role, Status status) {
        this.ID = -1;
        this.Name = name;
        this.Email = email;
        this.Password = Password;
        this.Role = role;
        this.Status = status;
    }

    public Role getRole() {
        return Role;
    }

    public Status getStatus() {
        return Status;
    }

    // Getters e setters
    public String getName() {
        return Name;
    }

    public int getID() { return ID; }

    public void setID(int ID) { this.ID = ID; }

    public String getEmail() {
        return Email;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setEmail(String email) { this.Email = email; }

    public void setRole(Role role) { this.Role = role; }

}
