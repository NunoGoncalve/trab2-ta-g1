package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainController {

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;

    public void setUser(User user) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(user);
        MainPanel.setLeft(menu);
        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController controller2 = loader.getController();
        controller2.setUser(user);
        MainPanel.setRight(usermenu);

        loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
        Parent Balance = null;
        try {
            Balance = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewBalanceController controller3 = loader.getController();
        controller3.setUser(user);
        controller3.setUserMenu((HBox)usermenu.lookup("#UserMenu"), (StackPane)usermenu.lookup("#UserMenuPane"));
        Stack.getChildren().add(0, Balance);
    }
}
