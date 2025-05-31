package com.example.catcoins;

import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class UserPanelController extends MenuLoader {

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    /*public void setUser(User LoggedUser) {
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

        loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
        Parent Balance = null;
        try {
            Balance = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewBalanceController controller3 = loader.getController();
        controller3.setUser(LoggedUser);
        controller3.setUserMenu((HBox)usermenu.lookup("#UserMenu"), (StackPane)usermenu.lookup("#UserMenuPane"));
        Stack.getChildren().add(0, Balance);


    }*/

}
