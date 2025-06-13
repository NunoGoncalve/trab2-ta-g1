package com.example.catcoins;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

//   AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Verify the credentials entered");


public class AlertUtils {

    public static void showAlert(StackPane background, Alert.AlertType type, String title, String messageText) {
        // Caixa de diálogo
        VBox dialog = new VBox(3);
        dialog.setSpacing(25);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: white;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(170);

        // Mensagem
        Label message = new Label(messageText);
        message.setStyle("-fx-text-fill: white");

        // Botão OK
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");

        dialog.getChildren().addAll(message, okButton);

        // Fundo semi-transparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);

        // Adiciona o overlay no Background informado
        background.getChildren().add(overlay);

        // Ação do botão OK
        okButton.setOnAction(e -> background.getChildren().remove(overlay));
    }

    public static void showAlert(StackPane Background,String Message, Runnable onClose) {
        // Create the dialog content (not fullscreen)
        VBox dialog = new VBox(3);
        dialog.setSpacing(25);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: white;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(170);
        Label message = new Label(Message);
        message.setStyle("-fx-text-fill: white");
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");
        dialog.getChildren().addAll(message, okButton);

        // Optional: create a semi-transparent background overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);"); // 0.4 = 40% opacity
        // Add the dialog to the overlay and center it
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);
        // Add overlay to the root StackPane
        Background.getChildren().add(overlay);

        okButton.setOnAction(e -> {
            Background.getChildren().remove(overlay);
            if (onClose != null) {
                onClose.run();
            }
        });
    }
}
