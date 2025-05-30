package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.*;

public class MainController {

    public void initialize() {
        carregarMoedas();
    }

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;

    public void setUser(User user) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(user);
        MainPanel.setLeft(menu);
        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController controller2 = loader.getController();
        controller2.setUser(user);
        MainPanel.setRight(usermenu);

        loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
        Parent Balance = null;
        try {
            Balance = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewBalanceController controller3 = loader.getController();
        controller3.setUser(user);
        controller3.setUserMenu((HBox)usermenu.lookup("#UserMenu"), (StackPane)usermenu.lookup("#UserMenuPane"));
        Stack.getChildren().add(0, Balance);
    }

    @FXML
    private VBox coinListVBox; // Este é o GridPane com fx:id="coinGrid" no FXML

    public void carregarMoedas() {
        String sql = "SELECT id, Name, Value FROM Coin"; // Assumindo que "Tax" é a quarta coluna

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Remove coin entries but keep the header
            coinListVBox.getChildren().removeIf(node ->
                    node instanceof GridPane && node.getStyleClass().contains("coin-entry"));
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("value");
                double tax = 0.05; // ou rs.getDouble("tax");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(20);
                grid.setVgap(15);
                grid.getStyleClass().add("coin-entry");
                grid.setPrefWidth(799); // Igual ao exemplo do FXML
                grid.setPrefHeight(59); // Igual ao FXML

                // Colunas
                ColumnConstraints col0 = new ColumnConstraints();
                col0.setPercentWidth(5);
                col0.setHalignment(HPos.CENTER);

                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(15);
                col1.setHalignment(HPos.CENTER);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(47);
                col2.setHalignment(HPos.CENTER);

                ColumnConstraints col3 = new ColumnConstraints();
                col3.setPercentWidth(13);
                col3.setHalignment(HPos.CENTER);

                grid.getColumnConstraints().addAll(col0, col1, col2, col3);

                // Linha
                RowConstraints row = new RowConstraints();
                row.setValignment(VPos.CENTER);
                grid.getRowConstraints().add(row);

                // Conteúdo
                Label logoLabel = new Label("₿");
                logoLabel.setPrefSize(12, 25);
                logoLabel.setTextFill(Color.WHITE);
                logoLabel.getStyleClass().add("coin-logo");
                grid.add(logoLabel, 0, 0);

                Label nameLabel = new Label(name.toUpperCase());
                nameLabel.getStyleClass().add("coin-label");
                grid.add(nameLabel, 1, 0);

                Label priceLabel = new Label(String.format("%.2f$", price));
                priceLabel.setPrefSize(78, 25);
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                Label taxLabel = new Label(String.format("%.2f", tax));
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-price");
                grid.add(taxLabel, 3, 0);

                // Centraliza e limita a largura dentro da VBox
                VBox.setMargin(grid, new Insets(0, 0, 0, 0));
                grid.setMaxWidth(799);
                grid.setMinWidth(799);

                // Adiciona ao coinListVBox
                coinListVBox.getChildren().add(grid);
            }


        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao carregar moedas: " + e.getMessage());
            e.printStackTrace();
        }
    }


        private void showAlert(Alert.AlertType type, String title, String message) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

}
