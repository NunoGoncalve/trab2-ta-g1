package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.io.IOException;

public class MenuController {

    private User LoggedInUser;

    @FXML
    private HBox Menu;

    @FXML
    private HBox ExtendedMenu;

    @FXML private ImageView Wallet;

    private StackPane Background;

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
        if (user instanceof Client) {
            Wallet.setVisible(true);
        }
    }

    public void SetBackground(StackPane background) {
        Background = background;
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

    private void GoTo(String View) {
        try {
            Main.setRoot(View, LoggedInUser);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
        }

    }
}