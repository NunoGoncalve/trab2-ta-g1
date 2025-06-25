package com.example.catcoins;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AlertUtils {

    public static void showAlert(StackPane background, String messageText) {
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

    public static boolean ConfirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10");

        Label Label = new Label(message);
        Label.setStyle("-fx-text-fill: white;");

        VBox content = new VBox(10, Label);
        dialogPane.setContent(content);

        ButtonType okButtonType = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType CancelButtonType = new ButtonType("No", ButtonBar.ButtonData.NO);
        dialogPane.getButtonTypes().addAll(okButtonType, CancelButtonType);
        dialogPane.lookupButton(okButtonType).setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");
        dialogPane.lookupButton(CancelButtonType).setStyle("-fx-background-color: red; -fx-max-width: 50; -fx-border-radius: 10; -fx-text-fill: white;");

        return alert.showAndWait().filter(resposta -> resposta == okButtonType).isPresent();
    }
}
