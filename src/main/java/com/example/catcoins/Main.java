package com.example.catcoins;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

public class Main extends Application {
    private static Scene scene;
    private User user;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("ManageCoin.fxml"));
        scene = new Scene(root);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void setRoot(String fxml, User LoggedUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Parent root = loader.load();
       if (fxml.equals("UserConfig.fxml")){
            UserConfigController controller = loader.getController();
            controller.setUser(LoggedUser);
       }
       else if(fxml.equals("Main.fxml")) {
           MainController controller = loader.getController();
           controller.setUser(LoggedUser);
       }
       else if(fxml.equals("UserPanel.fxml")) {
           UserPanelController controller = loader.getController();
           controller.setUser(LoggedUser);
       }else if(fxml.equals("ManageCoin.fxml")) {
           ManageCoinController controller = loader.getController();
           controller.setUser(LoggedUser);
       }

       scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
