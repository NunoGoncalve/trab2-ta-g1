package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserConfigController {

    private User LoggedUser;

    @FXML
    private BorderPane MainPanel;

    @FXML
    private TextField Email;

    @FXML
    private TextField Name;

    @FXML
    private Button EditButton;

    public void setUser(User LoggedUser) {
        this.LoggedUser = LoggedUser;
        Email.setText(LoggedUser.getEmail());
        Name.setText(LoggedUser.getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(LoggedUser);
        MainPanel.setLeft(menu);

        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController controller2 = loader.getController();
        controller2.setUser(LoggedUser);
        MainPanel.setRight(usermenu);
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
