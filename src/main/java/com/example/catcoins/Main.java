package com.example.catcoins;

import com.example.catcoins.model.Coin;
import com.example.catcoins.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.Connection;

public class Main extends Application {
    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        scene = new Scene(root);
        primaryStage.setTitle("Login");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("imgs/logo.png")));
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            conn.close();
        }
        super.stop();
    }

    public static void setRoot(String fxml, User LoggedUser) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof MenuLoader) {
                ((MenuLoader) controller).setLoggedUser(LoggedUser);
            }
            scene.setRoot(root);
        }catch (Exception e){
            throw e;
        }
    }

    public static void setRoot(String fxml, User LoggedUser, Object object) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof MarketController) {
            ((MarketController) controller).setLoggedUser(LoggedUser);
            ((MarketController) controller).setCoin((Coin) object);
        }else if (controller instanceof UserDetailsController){
            ((UserDetailsController) controller).setUserDetails((User) object);
            ((UserDetailsController) controller).setLoggedUser(LoggedUser);

        }
        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
