package com.example.catcoins;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public interface Loader {

    void setLoggedUser(User user);

    void LoadMenus(VBox Stack, BorderPane MainPanel);


}
