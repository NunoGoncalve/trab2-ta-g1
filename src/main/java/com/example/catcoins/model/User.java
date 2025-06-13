package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private int ID;
    private String Name;
    private String Email;
    private Role Role;
    private Status Status;


    public User(int id, String name, String email, Role role, Status status) {
        this.ID = id;
        this.Name = name;
        this.Email = email;
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
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "UPDATE User SET Name = ? WHERE ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, Name);
            stmt.setInt(2, ID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserEmail(int WalletID) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "Select Email From UserClientWallet WHERE WalletID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, WalletID);
            ResultSet ResultID = stmt.executeQuery();
            ResultID.next();
            return ResultID.getString("Email");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";

    }

    public void setEmail(String email) { this.Email = email; }

    public void setRole(Role role) { this.Role = role; }

}
