package com.example.catcoins;

import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class MenuController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private User LoggedInUser;

    @FXML
    private HBox Menu;

    @FXML
    private HBox ExtendedMenu;

    @FXML
    void Open() {
        CloseAnimation(Menu);
        OpenAnimation(ExtendedMenu);
    }

    @FXML
    void Close() {
        CloseAnimation(ExtendedMenu);
        OpenAnimation(Menu);
    }

    public void setUser(User user) {
        this.LoggedInUser = user;
    }

    public User getUser() {
        return this.LoggedInUser;
    }

    private void CloseAnimation(HBox menu){
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(500), menu);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        CloseTransition.setOnFinished(event -> {
            menu.setVisible(false);
        });
    }

    private void OpenAnimation(HBox menu){
        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), menu);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        menu.setVisible(true);
    }

    @FXML
    void GoHome() {
        GoTo("Main.fxml");
    }

    @FXML
    void GoWallet() {
        GoTo("UserPanel.fxml");
    }

    @FXML
    void Logout() {
        LoggedInUser = null;
        GoTo("Login.fxml");
    }

    @FXML
    void GoMarket() {
        GoTo("Main.fxml");
    }

    private void GoTo(String View) {

        /*try {
            root = FXMLLoader.load(getClass().getResource(View+".fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root, 1000, 1000);
        stage.setScene(scene);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();*/
        try {
            Main.setRoot(View, LoggedInUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}