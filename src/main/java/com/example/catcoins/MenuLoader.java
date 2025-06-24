package com.example.catcoins;

import com.example.catcoins.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.*;

import java.io.IOException;

public abstract class MenuLoader implements Loader {

    private User LoggedUser;

    public void setLoggedUser(User user) {
        this.LoggedUser = user;
    }

    public User getLoggedUser() {
        return LoggedUser;
    }

    public void LoadMenus(VBox Stack, BorderPane MainPanel, StackPane Background){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController MenuCntrl = loader.getController();
        MenuCntrl.setUser(LoggedUser);
        MenuCntrl.SetBackground(Background);
        MainPanel.setLeft(menu);
        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController UserMenuCntrl = loader.getController();
        UserMenuCntrl.setUser(LoggedUser);
        UserMenuCntrl.SetBackground(Background);
        MainPanel.setRight(usermenu);

        loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
        Parent Balance = null;
        try {
            Balance = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewBalanceController ViewBalanceCntrl = loader.getController();
        ViewBalanceCntrl.setUser(LoggedUser);
        ViewBalanceCntrl.setUserMenu((HBox)usermenu.lookup("#UserMenu"), (StackPane)usermenu.lookup("#UserMenuPane"));
        Stack.getChildren().add(0, Balance);
    }

    public void GoTo(String fxml, User user, StackPane Background) {
        try {
            Main.setRoot(fxml, user);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
        }
    }

    public void GoTo(String fxml, User user, Object object,StackPane Background) {
        try {
            Main.setRoot(fxml, user, object);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
        }
    }
}
