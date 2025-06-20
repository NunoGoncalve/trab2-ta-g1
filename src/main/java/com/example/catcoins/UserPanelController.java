package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Transaction;
import com.example.catcoins.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserPanelController extends MenuLoader {


    public void initialize() {
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

    @FXML private BorderPane MainPanel;
    @FXML private VBox Stack;
    @FXML private VBox coinListVBox;
    @FXML private MenuButton sortMenuButton;
    @FXML private Label sortPriceLabel;
    @FXML private Label sortVarianceLabel;
    @FXML private Label sortNameLabel;
    @FXML private Label SumLabel;
    @FXML private Label numOrdersBuy;
    @FXML private Label numOrdersSell;
    @FXML private Label TotalCoins;

    @FXML private StackPane Background;
    @FXML private PieChart portfolioPieChart;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
        carregarMoedas(null);
        atualizarContagemOrdensBuy();
        atualizarContagemOrdensSell();
        LoadGraph();
    }

    public void carregarMoedas(String filtro) {
        Client Logclient = (Client) super.getLoggedUser();

        String sql = "SELECT CoinID, Amount, VarianceCalc(CoinID) as variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value, Name FROM Portfolio INNER join Coin on CoinID = Coin.ID where WalletID = ?";
       /* try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, Logclient.getWallet().getID());
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        class CoinData {
            int id;
            String name;
            double value;
            double variance;
            int amount;

            CoinData(int id, String name, double value, double variance, int amount) {
                this.id = id;
                this.name = name;
                this.value = value;
                this.variance = variance;
                this.amount = amount;
            }
        }

        List<CoinData> coins = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, Logclient.getWallet().getID());
            ResultSet CoinInfo = stmt.executeQuery();

            while (CoinInfo.next()) {
                int id = CoinInfo.getInt("CoinID");
                String name = CoinInfo.getString("name");
                double price = CoinInfo.getDouble("value");
                int amount = CoinInfo.getInt("Amount");
                double variance = CoinInfo.getDouble("variance");



                coins.add(new CoinData(id, name, price, variance,amount));
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

            double sum=0.0;
            // ADICIONA À UI
            for (CoinData coin : coins) {
                double amount=coin.amount;
                double value=coin.value;
                double total = amount * value;

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
                ColumnConstraints col4 = new ColumnConstraints(); col3.setPercentWidth(13); col3.setHalignment(HPos.CENTER);

                grid.getColumnConstraints().addAll(col0, col1, col2, col3, col4);

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

                Label AmountLabel = new Label(String.valueOf(coin.amount) );
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-amount");
                grid.add(AmountLabel, 4, 0);

                VBox.setMargin(grid, new Insets(0, 0, 0, 0));
                grid.setMaxWidth(1090);
                grid.setMinWidth(1090);
                coinListVBox.getChildren().add(grid);
                sum+=total;
                SumLabel.setText(String.format("%.2f€", sum));

            }

        } catch (SQLException e) {
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Error loading order count:\n" + e.getMessage());
            e.printStackTrace();
        }
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

    private void atualizarContagemOrdensBuy() {
        Client loggedClient = (Client) super.getLoggedUser();

        String sql = "SELECT COUNT(ID) AS total_orders FROM `Order` WHERE Wallet = ? AND Type = 'Buy'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, loggedClient.getWallet().getID());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalOrders = rs.getInt("total_orders");
                numOrdersBuy.setText(String.valueOf(totalOrders));
            } else {
                numOrdersBuy.setText("0");
            }

        } catch (SQLException e) {
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Error loading order count:\n" + "\nYou have an error in your SQL syntax");
            e.printStackTrace();
            numOrdersBuy.setText("0");
        }
    }

    private void atualizarContagemOrdensSell() {
        Client loggedClient = (Client) super.getLoggedUser();

        String sql = "SELECT COUNT(ID) AS total_orders FROM `Order` WHERE Wallet = ? AND Type = 'Sell'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, loggedClient.getWallet().getID());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalOrders = rs.getInt("total_orders");
                numOrdersSell.setText(String.valueOf(totalOrders));
            } else {
                numOrdersSell.setText("0");
            }

        } catch (SQLException e) {
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Error loading order count:\n" + "\nYou have an error in your SQL syntax");
            e.printStackTrace();
            numOrdersSell.setText("0");
        }
    }

    private void LoadGraph(){
        // Limpa os dados existentes do PieChart
        portfolioPieChart.getData().clear();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            String sql = "SELECT c.Name as Coin, p.Amount,(SELECT Sum(Amount) from Portfolio where WalletID = ?) as TotalCoins FROM Portfolio p inner join Coin c on c.ID=CoinID WHERE p.WalletID = ?; ";
            PreparedStatement stmt = conn.prepareStatement(sql);
            Client LoggedClient = (Client) super.getLoggedUser();
            stmt.setInt(1,   LoggedClient.getWallet().getID());
            stmt.setInt(2,   LoggedClient.getWallet().getID());


            ResultSet coinResult = stmt.executeQuery();

            while (coinResult.next()) {
                String coinID = coinResult.getString("Coin");
                int amount = coinResult.getInt("Amount");
                int totalCoins = coinResult.getInt("TotalCoins");
                // Para PieChart, cada fatia é um PieChart.Data
                PieChart.Data slice = new PieChart.Data(coinID+ " - " + amount, amount);
                portfolioPieChart.getData().add(slice);
                TotalCoins.setText(String.valueOf("Total Coins: "+ totalCoins));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
