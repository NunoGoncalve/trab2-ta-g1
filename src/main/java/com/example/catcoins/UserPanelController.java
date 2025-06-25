package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UserPanelController extends MenuLoader {


    public void initialize() {
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

    private Client LoggedCLient;
    PortfolioDAO PrtfDao = new PortfolioDAO();
    List<Portfolio> PortfolioList = new ArrayList<>();

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        if (user instanceof Client) {
            LoggedCLient = (Client) super.getLoggedUser();
        }
        super.LoadMenus(Stack, MainPanel, Background);
        LoadCoins(null);
        GetOrderNum();
        LoadGraph();
    }

    public void LoadCoins(String filtro) {

        try {
            PortfolioList = PrtfDao.GetWalletPortfolio(LoggedCLient.getWallet());
            // FILTRAGEM OPCIONAL
            if (filtro != null) {
                switch (filtro) {
                    case "Price":
                        PortfolioList.sort(Comparator.comparingDouble(c -> -c.getCryptoCoin().getValue()));
                        break;
                    case "Variance":
                        PortfolioList.sort(Comparator.comparingDouble(c -> -c.getCryptoCoin().getVariance()));
                        break;
                    case "Name":
                        PortfolioList.sort(Comparator.comparing(c -> c.getCryptoCoin().getName().toLowerCase()));
                        break;
                    default:
                        break;
                }
            }

            // LIMPA A LISTA
            coinListVBox.getChildren().removeIf(node -> node instanceof GridPane && node.getStyleClass().contains("coin-entry"));

            double sum=0.0, total= 0.00;
            // ADICIONA À UI
            for (Portfolio pf : PortfolioList) {
                total = pf.getAmount() * pf.getCryptoCoin().getValue();

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(20);
                grid.setVgap(15);
                grid.getStyleClass().add("coin-entry");
                grid.setPrefWidth(1090);
                grid.setPrefHeight(59);
                grid.setOnMouseClicked(mouseEvent -> ViewDetails(mouseEvent));
                grid.setId(String.valueOf(PortfolioList.indexOf(pf)));

                ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(5);  col0.setHalignment(HPos.CENTER);
                ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(15); col1.setHalignment(HPos.CENTER);
                ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(47); col2.setHalignment(HPos.CENTER);
                ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(13); col3.setHalignment(HPos.CENTER);
                ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(13); col4.setHalignment(HPos.CENTER);

                grid.getColumnConstraints().addAll(col0, col1, col2, col3, col4);

                RowConstraints row = new RowConstraints(); row.setValignment(VPos.CENTER);
                grid.getRowConstraints().add(row);

                Image logoImage = new Image("http://foodsorter.fixstuff.net/CatCoins/img/" + pf.getCryptoCoin().getID() + ".png", true);
                ImageView logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(35);
                logoImageView.setFitHeight(35);
                logoImageView.getStyleClass().add("coin-logo");
                grid.add(logoImageView, 0, 0);

                Label nameLabel = new Label(pf.getCryptoCoin().getName().toUpperCase());
                nameLabel.getStyleClass().add("coin-label");
                grid.add(nameLabel, 1, 0);

                Label priceLabel = new Label(String.format("%.2f€", pf.getCryptoCoin().getValue()));
                priceLabel.setPrefSize(78, 25);
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                Label taxLabel = new Label(String.format("%.2f", pf.getCryptoCoin().getVariance()) + "%");
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-price");
                taxLabel.setStyle(pf.getCryptoCoin().getVariance() >= 0 ? "-fx-text-fill: #4caf50" : "-fx-text-fill: red");
                grid.add(taxLabel, 3, 0);

                Label AmountLabel = new Label(String.valueOf(pf.getAmount()) );
                taxLabel.setPrefSize(87, 25);
                taxLabel.getStyleClass().add("coin-amount");
                grid.add(AmountLabel, 4, 0);

                VBox.setMargin(grid, new Insets(0, 0, 0, 0));
                grid.setMaxWidth(1090);
                grid.setMinWidth(1090);
                coinListVBox.getChildren().add(grid);
                sum+=total;

            }
            SumLabel.setText(String.format("%.2f€", sum));

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }


    public void ViewDetails(MouseEvent mouseEvent) {
        Node ClickedNode = (Node) mouseEvent.getTarget();
        int CoinIndex = Integer.parseInt(ClickedNode.getId());
        try {
            Main.setRoot("Market.fxml", LoggedCLient, PortfolioList.get(CoinIndex).getCryptoCoin());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void GetOrderNum() {
        OrderDAO OrdDao = new OrderDAO();
        numOrdersBuy.setText("0");
        try{
            Map<String, Integer> map = OrdDao.GetUserOrderNum(LoggedCLient.getWallet());
            numOrdersBuy.setText(String.valueOf(map.get("Buy")));
            numOrdersSell.setText(String.valueOf(map.get("Sell")));

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            numOrdersBuy.setText("0");
            numOrdersSell.setText("0");
        }
    }

    private void LoadGraph(){
        // Limpa os dados existentes do PieChart
        portfolioPieChart.getData().clear();
        for(Portfolio prtf : PortfolioList){
            PieChart.Data slice = new PieChart.Data(prtf.getCryptoCoin().getName()+ " - " + prtf.getAmount(), prtf.getAmount());
            portfolioPieChart.getData().add(slice);

        }
        TotalCoins.setText("Total Coins: "+ PortfolioList.size());
    }
}
