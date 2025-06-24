package com.example.catcoins.model;

import com.example.catcoins.AlertUtils;
import com.example.catcoins.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements DAO<User> {

    public User GetByID(int id) throws SQLException {
        return null;
    }

    public ArrayList<User> GetAll() throws SQLException {
        WalletDAO WltDAO = new WalletDAO();
        String sql = "SELECT * FROM User";
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet UserResult = stmt.executeQuery();
            while (UserResult.next()) {
                if(UserResult.getString("Role").equals("Client")) {
                    users.add(
                        new Client(
                                UserResult.getInt("ID"),
                                UserResult.getString("Name"),
                                UserResult.getString("Email"),
                                UserResult.getString("Password"),
                                Role.fromString(UserResult.getString("Role")),
                                Status.fromString(UserResult.getString("Status")),
                                WltDAO.GetByUserID(conn, UserResult.getInt("ID"))
                        )
                    );
                }else{
                    users.add(
                        new User(
                                UserResult.getInt("ID"),
                                UserResult.getString("Name"),
                                UserResult.getString("Email"),
                                UserResult.getString("Password"),
                                Role.fromString(UserResult.getString("Role")),
                                Status.fromString(UserResult.getString("Status"))
                        )
                    );
                }

            }
        }

        return users;
    }

    public User Add(User ur) throws SQLException{
        String sql = "INSERT INTO User (Name, Email, Password, Role, Status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ur.getName());
            stmt.setString(2, ur.getEmail());
            stmt.setString(3, ur.getPassword());
            stmt.setString(4, ur.getRole().toString());
            stmt.setString(5, ur.getStatus().toString());
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                ur.setID(generatedId);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
        return ur;

    }

    public boolean Update(User object) throws SQLException{
        String sql = "Update User Set Name = ?, Status = ? WHERE ID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, object.getName());
            stmt.setString(2, object.getStatus().toString());
            stmt.setInt(3, object.getID());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        }
    }

    public User Login(String email, String password) throws SQLException {
        WalletDAO WltDao = new WalletDAO();
        try(Connection conn = DatabaseConnection.getInstance().getConnection()){

            String sql = "SELECT * FROM User WHERE email = ? AND password = ? and Status = 'Active'";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet UserResult = stmt.executeQuery();

            if (UserResult.next()) {
                if(UserResult.getString("Role").equals("Client")) {
                    return new Client(UserResult.getInt("ID"), UserResult.getString("Name"), UserResult.getString("Email"), UserResult.getString("Password"), Role.Client, Status.Active, WltDao.GetByUserID(conn, UserResult.getInt("ID")));
                }else{
                    return new User(UserResult.getInt("ID"), UserResult.getString("Name"), UserResult.getString("Email"), UserResult.getString("Password"),Role.Admin, Status.Active);
                }
            }else{
                return null;
            }

        }
    }

    public User GetUserByWalletID(int WalletID) throws SQLException {
        String sql = "Select * From UserClientWallet WHERE WalletID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, WalletID);
            ResultSet ResultID = stmt.executeQuery();
            ResultID.next();
            return new User(
                ResultID.getInt("ID"),
                ResultID.getString("Name"),
                ResultID.getString("Email"),
                ResultID.getString("Password"),
                Role.fromString(ResultID.getString("Role")),
                Status.fromString(ResultID.getString("Status"))
            );
        }

    }

    public int LoadTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM User";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean CheckIfEmail(String email) throws SQLException {
        String query = "SELECT 1 FROM User WHERE Email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return stmt.executeQuery().next();
            }
        }
        return false;
    }

}
