package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrdersController extends MenuLoader {

    @FXML
    private TableView<Order> transactionsTable;
    @FXML
    private TableColumn<Order, Integer> idColumn;
    @FXML
    private TableColumn<Order, String> typeColumn;
    @FXML
    private TableColumn<Order, String> coinColumn;
    @FXML
    private TableColumn<Order, Double> valueColumn;
    @FXML
    private TableColumn<Order, Double> amountColumn;
    @FXML
    private TableColumn<Order, String> dateColumn;
    @FXML
    private TableColumn<Order, Void> ActionsColumn;
    @FXML
    private VBox Stack;
    @FXML
    private BorderPane MainPanel;
    @FXML
    private StackPane Background;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private OrderDAO OrdDao = new OrderDAO();
    private ArrayList<Order> ActiveOrderList;
    private Client LoggedClient;

    private Scene scene;
    private Parent root;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        if (user instanceof Client) {
            LoggedClient = (Client) user;
        }
        super.LoadMenus(Stack, MainPanel, Background);
        LoadActiveOrders();
        configureTableColumns();
        configureNumberFormats();

    }

    private void configureTableColumns() {
        // Configuração das colunas da tabela
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        coinColumn.setCellValueFactory(new PropertyValueFactory<>("coin"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Formatação personalizada para valores monetários
        valueColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        coinColumn.setCellValueFactory(cellData -> {
            Coin product = cellData.getValue().getCoin();
            String name = (product != null) ? product.getName() : "";
            return new SimpleStringProperty(name);
        });

        // Configuração do botão "✖" na coluna de ações
        ActionsColumn.setCellFactory(col -> new TableCell<>() {
            Button deleteButton = new Button("✖");
            {

                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }


    private void configureNumberFormats() {
        currencyFormat.setMaximumFractionDigits(2);
        coinFormat.setMaximumFractionDigits(4);
    }

    private void LoadActiveOrders() {

        try {
            ActiveOrderList = OrdDao.GetUserActiveOrders(LoggedClient.getWallet());
            ObservableList<Order> orderList = FXCollections.observableArrayList(ActiveOrderList);
            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY) ; // removeu aquele espaço branco
            transactionsTable.setItems(orderList);

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
    }

    public void deleteOrder(Order CancelOrder) {
        if (!AlertUtils.ConfirmAction("Confimation", "Are you sure you want to cancel this order?")) {
            return;
        }

        try {
            CancelOrder.setStatus(OrderStatus.Cancelled);

            if (OrdDao.CancelOrder(CancelOrder)) {
                AlertUtils.showAlert(Background, "Order successfully cancelled!");
            } else {
                AlertUtils.showAlert(Background, "No order was cancelled");
            }
            Client LoggedClient = (Client) super.getLoggedUser();
            if(CancelOrder.getType().equals("Buy")){
                WalletDAO WltDAO = new WalletDAO();
                LoggedClient.getWallet().SetBalance(LoggedClient.getWallet().getBalance()+LoggedClient.getWallet().getPendingBalance(), 0.00);
                WltDAO.Update(LoggedClient.getWallet());
                scene = MainPanel.getScene();
                root = scene.getRoot();
                Node node = root.lookup("#balanceLabel");
                if (node instanceof Label) {
                    Label label = (Label) node;
                    label.setText(String.format("%.2f", LoggedClient.getWallet().getBalance()));
                }
                node = root.lookup("#PendingbalanceLabel");
                if (node instanceof Label) {
                    Label label = (Label) node;
                    label.setText(String.format("%.2f", LoggedClient.getWallet().getPendingBalance()));
                }
            }else{
                PortfolioDAO PrtfDao = new PortfolioDAO();
                ArrayList<Portfolio> UserPortfolioList = PrtfDao.GetWalletPortfolio(LoggedClient.getWallet());

                for (Portfolio prtf : UserPortfolioList) {
                    if (prtf.getCryptoCoin().getID() == CancelOrder.getCoin().getID()) {
                        prtf.setAmount(prtf.getAmount()+CancelOrder.getAmount());
                        PrtfDao.Update(prtf);
                        break;
                    }
                }
            }
            LoadActiveOrders();
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }


}