package com.example.catcoins;

import com.example.catcoins.model.Coin;
import com.example.catcoins.model.CoinDAO;
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
    @FXML private VBox coinListVBox;
    @FXML private MenuButton sortMenuButton;
    @FXML private Label sortPriceLabel;
    @FXML private Label sortVarianceLabel;
    @FXML private Label sortNameLabel;
    @FXML private StackPane Background;

    private CoinDAO CnDao = new CoinDAO();
    ArrayList<Coin> coins = new ArrayList<>();

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
    }

    public void initialize() {
        LoadCoins(null);

        sortPriceLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Price");
            LoadCoins("Price");
        });

        sortVarianceLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Variance");
            LoadCoins("Variance");
        });

        sortNameLabel.setOnMouseClicked(event -> {
            sortMenuButton.setText("Name");
            LoadCoins("Name");
        });
    }

    public void LoadCoins(String filtro) {

        try  {
            coins = CnDao.GetAll();
            // FILTRAGEM OPCIONAL
            if (filtro != null) {
                switch (filtro) {
                    case "Price":
                        coins.sort(Comparator.comparingDouble(c -> -c.getValue()));
                        break;
                    case "Variance":
                        coins.sort(Comparator.comparingDouble(c -> -c.getVariance()));
                        break;
                    case "Name":
                        coins.sort(Comparator.comparing(c -> c.getName().toLowerCase()));
                        break;
                    default:
                        break;
                }
            }

            // LIMPA A LISTA
            coinListVBox.getChildren().removeIf(node -> node instanceof GridPane && node.getStyleClass().contains("coin-entry"));

            // ADICIONA À UI
            for (Coin coin : coins) {
                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(20);
                grid.setVgap(15);
                grid.getStyleClass().add("coin-entry");
                grid.setPrefWidth(1090);
                grid.setPrefHeight(59);
                grid.setOnMouseClicked(mouseEvent -> ViewDetails(mouseEvent));
                grid.setId(String.valueOf(coins.indexOf(coin)));

                ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(5);  col0.setHalignment(HPos.CENTER);
                ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(15); col1.setHalignment(HPos.CENTER);
                ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(47); col2.setHalignment(HPos.CENTER);
                ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(13); col3.setHalignment(HPos.CENTER);
                grid.getColumnConstraints().addAll(col0, col1, col2, col3);

                RowConstraints row = new RowConstraints(); row.setValignment(VPos.CENTER);
                grid.getRowConstraints().add(row);

                Image logoImage = new Image("http://foodsorter.fixstuff.net/CatCoins/img/" + coin.getID()+ ".png", true);
                ImageView logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(35);
                logoImageView.setFitHeight(35);
                logoImageView.getStyleClass().add("coin-logo");
                grid.add(logoImageView, 0, 0);

                Label nameLabel = new Label(coin.getName().toUpperCase());
                nameLabel.getStyleClass().add("coin-label");
                grid.add(nameLabel, 1, 0);

                Label priceLabel = new Label(String.format("%.2f€", coin.getValue()));
                priceLabel.setPrefSize(78, 25);
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                Label taxLabel = new Label(String.format("%.2f", coin.getVariance()) + "%");
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-price");
                taxLabel.setStyle(coin.getVariance() >= 0 ? "-fx-text-fill: #4caf50" : "-fx-text-fill: red");
                grid.add(taxLabel, 3, 0);

                VBox.setMargin(grid, new Insets(0, 0, 0, 0));
                grid.setMaxWidth(1090);
                grid.setMinWidth(1090);
                coinListVBox.getChildren().add(grid);
            }

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    public void ViewDetails(MouseEvent mouseEvent) {
        Node ClickedNode = (Node) mouseEvent.getTarget();
        int CoinIndex = Integer.parseInt(ClickedNode.getId());
        super.GoTo("Market.fxml", super.getLoggedUser(), coins.get(CoinIndex),Background);

    }

}
