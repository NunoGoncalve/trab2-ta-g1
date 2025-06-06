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
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainController extends MenuLoader {


    @FXML private BorderPane MainPanel;
    @FXML private VBox Stack;
    @FXML private VBox coinListVBox; // Este é o GridPane com fx:id="coinGrid" no FXML
    @FXML private MenuButton sortMenuButton;
    @FXML private Label sortPriceLabel;
    @FXML private Label sortVarianceLabel;
    @FXML private Label sortNameLabel;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    public void initialize() {
        carregarMoedas(null);

        sortPriceLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Price");
            carregarMoedas("Price");
        });

        sortVarianceLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Variance");
            carregarMoedas("Variance");
        });

        sortNameLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Name");
            carregarMoedas("Name");
        });
    }

    public void carregarMoedas(String filtro) {
        String sql = "SELECT id, Name, Value FROM Coin";
        String sqlV = "SELECT Value FROM CoinHistory WHERE Coin = ? ORDER BY Date DESC LIMIT 1 OFFSET 1";

        class CoinData {
            int id;
            String name;
            double value;
            double variance;

            CoinData(int id, String name, double value, double variance) {
                this.id = id;
                this.name = name;
                this.value = value;
                this.variance = variance;
            }
        }

        List<CoinData> coins = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet CoinInfo = stmt.executeQuery(sql);
            PreparedStatement ValueStat = conn.prepareStatement(sqlV);

            while (CoinInfo.next()) {
                int id = CoinInfo.getInt("id");
                String name = CoinInfo.getString("name");
                double price = CoinInfo.getDouble("value");
                double variance = 0.0;

                ValueStat.setInt(1, id);
                ResultSet ValueInfo = ValueStat.executeQuery();
                if (ValueInfo.next()) {
                    double oldValue = ValueInfo.getDouble("value");
                    if (oldValue != 0) {
                        variance = (price - oldValue) / oldValue * 100;
                    }
                }

                coins.add(new CoinData(id, name, price, variance));
            }

            // FILTRAGEM OPCIONAL
            if (filtro != null) {
                switch (filtro) {
                    case "Price":
                        coins.sort(Comparator.comparingDouble(c -> -c.value));
                        break;
                    case "Variance":
                        coins.sort(Comparator.comparingDouble(c -> -c.variance));
                        break;
                    case "Name":
                        coins.sort(Comparator.comparing(c -> c.name.toLowerCase()));
                        break;
                    default:
                        break;
                }
            }

            // LIMPA A LISTA
            coinListVBox.getChildren().removeIf(node -> node instanceof GridPane && node.getStyleClass().contains("coin-entry"));

            // ADICIONA À UI
            for (CoinData coin : coins) {
                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(20);
                grid.setVgap(15);
                grid.getStyleClass().add("coin-entry");
                grid.setPrefWidth(1090);
                grid.setPrefHeight(59);
                grid.setOnMouseClicked(mouseEvent -> ViewDetails(mouseEvent));
                grid.setId(String.valueOf(coin.id));

                ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(5);  col0.setHalignment(HPos.CENTER);
                ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(15); col1.setHalignment(HPos.CENTER);
                ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(47); col2.setHalignment(HPos.CENTER);
                ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(13); col3.setHalignment(HPos.CENTER);
                grid.getColumnConstraints().addAll(col0, col1, col2, col3);

                RowConstraints row = new RowConstraints(); row.setValignment(VPos.CENTER);
                grid.getRowConstraints().add(row);

                Image logoImage = new Image("http://foodsorter.fixstuff.net/CatCoins/img/" + coin.id + ".png", true);
                ImageView logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(35);
                logoImageView.setFitHeight(35);
                logoImageView.getStyleClass().add("coin-logo");
                grid.add(logoImageView, 0, 0);

                Label nameLabel = new Label(coin.name.toUpperCase());
                nameLabel.getStyleClass().add("coin-label");
                grid.add(nameLabel, 1, 0);

                Label priceLabel = new Label(String.format("%.2f€", coin.value));
                priceLabel.setPrefSize(78, 25);
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                Label taxLabel = new Label(String.format("%.2f", coin.variance) + "%");
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-price");
                taxLabel.setStyle(coin.variance >= 0 ? "-fx-text-fill: #4caf50" : "-fx-text-fill: red");
                grid.add(taxLabel, 3, 0);

                VBox.setMargin(grid, new Insets(0, 0, 0, 0));
                grid.setMaxWidth(1090);
                grid.setMinWidth(1090);
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
