package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserConfigController {

    private User LoggedUser;

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField Email;

    @FXML
    private TextField Name;

    @FXML
    private Button EditButton;

    public void setUser(User user) {
        this.LoggedUser = user;
        Name.setText(user.getName());
        Email.setText(user.getEmail());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(LoggedUser);
        rootPane.setLeft(menu);

        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController controllers = loader.getController();
        controller.setUser(LoggedUser);
        rootPane.setRight(usermenu);
    }

    @FXML
    public void EditButtonOnAction() {
        if (EditButton.getText().equals("Edit")) {
            Name.setEditable(true);
            EditButton.setText("Save");
        }else{
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE User SET Name = ? WHERE ID = 1";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, Name.getText().toString());
                stmt.executeUpdate();
                /*int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    messageLabel.setText("User updated successfully!");
                } else {
                    messageLabel.setText("Update failed.");
                }*/
            } catch (SQLException e) {
                e.printStackTrace();
                //messageLabel.setText("Database error.");
            }
            EditButton.setText("Edit");
            Name.setEditable(false);
            LoggedUser.setName(Name.getText().toString());
        }


    }
}
