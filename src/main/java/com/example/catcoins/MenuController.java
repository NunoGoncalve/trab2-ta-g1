package com.example.catcoins;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MenuController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button btn;

    @FXML
    private HBox Menu;

    @FXML
    private HBox ExtendedMenu;

    @FXML
    private Button backBtn;

    @FXML
    private Button infoBtn;

    @FXML
    private Button newBtn;

    @FXML
    private Button openBtn;

    @FXML
    void Open() {

        FadeTransition CloseTransition = new FadeTransition(Duration.millis(500), Menu);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        CloseTransition.setOnFinished(event -> {
            Menu.setVisible(false);
        });

        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), ExtendedMenu);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        ExtendedMenu.setVisible(true);


    }

    @FXML
    void Close() {
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(500), ExtendedMenu);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        CloseTransition.setOnFinished(event -> {
            ExtendedMenu.setVisible(false);
        });

        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), Menu);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        Menu.setVisible(true);




    }

    @FXML
    void GoHome(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("main.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @FXML
    void GoWallet(ActionEvent event) {

        try {
            root = FXMLLoader.load(getClass().getResource("UserMenu.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @FXML
    void GoMarket(ActionEvent event) {

        try {
            root = FXMLLoader.load(getClass().getResource("UserMenu.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @FXML
    void Logout(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("main.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }
}