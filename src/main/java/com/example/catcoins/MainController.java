package com.example.catcoins;

import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.*;

public class MainController extends MenuLoader {

    public void initialize() {
        carregarMoedas();
    }

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;


    @FXML
    private VBox coinListVBox; // Este é o GridPane com fx:id="coinGrid" no FXML

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    public void carregarMoedas() {
        String sql = "SELECT id, Name, Value FROM Coin"; // Assumindo que "Tax" é a quarta coluna
        String sqlV = "SELECT Value from CoinHistory where Coin = ? order by Date Desc limit 1,1";
        double variance = 0.00;


        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
             Statement stmt = conn.createStatement();
             ResultSet CoinInfo = stmt.executeQuery(sql);
             PreparedStatement ValueStat = conn.prepareStatement(sqlV);

                    // Remove coin entries but keep the header
            coinListVBox.getChildren().removeIf(node ->
                    node instanceof GridPane && node.getStyleClass().contains("coin-entry"));
            while (CoinInfo.next()) {

                int id = CoinInfo.getInt("id");
                String name = CoinInfo.getString("name");
                double price = CoinInfo.getDouble("value");

                ValueStat.setInt(1, id);
                ResultSet ValueInfo = ValueStat.executeQuery();
                if(ValueInfo.next()){
                    variance = (CoinInfo.getDouble("Value") - ValueInfo.getDouble("Value")) / ValueInfo.getDouble("Value") * 100;
                }else{
                    variance = 0.00;
                }

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(20);
                grid.setVgap(15);
                grid.getStyleClass().add("coin-entry");
                grid.setPrefWidth(799); // Igual ao exemplo do FXML
                grid.setPrefHeight(59); // Igual ao FXML
                grid.setOnMouseClicked(mouseEvent -> {ViewDetails(mouseEvent);});
                grid.setId(String.valueOf(id));

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

                Label priceLabel = new Label(String.format("%.2f€", price));
                priceLabel.setPrefSize(78, 25);
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                Label taxLabel = new Label(String.format("%.2f", variance)+"%");
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-price");
                if(variance>=0){
                    taxLabel.setStyle("-fx-text-fill: #4caf50");
                }else{
                    taxLabel.setStyle("-fx-text-fill: red");
                }
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

    public void ViewDetails(MouseEvent mouseEvent) {
        Node ClickedNode = (Node) mouseEvent.getTarget();
        int CoinID = Integer.parseInt(ClickedNode.getId());
        try {
            Main.setRoot("Market.fxml", super.getLoggedUser(), CoinID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
