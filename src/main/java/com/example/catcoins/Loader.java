package com.example.catcoins;

import com.example.catcoins.model.User;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public interface Loader {

    void setLoggedUser(User user);

    void LoadMenus(VBox Stack, BorderPane MainPanel, StackPane Background);


}
