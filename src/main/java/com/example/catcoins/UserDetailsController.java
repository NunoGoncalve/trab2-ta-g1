package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.sql.*;

public class UserDetailsController extends MenuLoader {

    @FXML
    private Text userNameText;
    @FXML
    private Text userEmailText;
    @FXML
    private Text userRoleText;
    @FXML
    private Text userStatusText;
    @FXML
    private Text balanceText;
    @FXML
    private Text coinBalanceText;
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
    private VBox Stack;
    @FXML
    private BorderPane MainPanel;
    @FXML
    private StackPane Background;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());


    private WalletDAO WalDao = new WalletDAO();
    private OrderDAO OrdDao = new OrderDAO();

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
    }

    public void setUserDetails(User user) {
        if (user != null) {
            loadUserDetails(user);
            LoadOrders(user.getID());
        }
    }

    @FXML
    public void initialize() {
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

        // Configuração clicável para a coluna de ID
        idColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Order, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-cursor: hand; -fx-text-fill: #2a9fd6;");
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            showTransactionPopup(item);
                        }
                    });
                }
            }
        });

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

        // Formatação personalizada para valores de moedas
        coinColumn.setCellValueFactory(cellData -> {
            Coin product = cellData.getValue().getCoin();
            String name = (product != null) ? product.getName() : "";
            return new SimpleStringProperty(name);
        });
    }

    private void showTransactionPopup(int orderId) {
        TableView<Map<String, Object>> table = new TableView<>();

        // Criação das colunas com larguras fixas e corretas
        TableColumn<Map<String, Object>, Integer> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(80);

        TableColumn<Map<String, Object>, Integer> orderIdCol = new TableColumn<>("OrderID");
        orderIdCol.setPrefWidth(100);

        TableColumn<Map<String, Object>, Double> valueCol = new TableColumn<>("Value");
        valueCol.setPrefWidth(80);

        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(80);

        // Configuração dos value factories
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty((Integer) data.getValue().get("ID")).asObject());
        orderIdCol.setCellValueFactory(data -> new SimpleIntegerProperty((Integer) data.getValue().get("OrderID")).asObject());
        valueCol.setCellValueFactory(data -> new SimpleDoubleProperty((Double) data.getValue().get("Value")).asObject());
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(formatDate((Timestamp) data.getValue().get("Date"))));
        amountCol.setCellValueFactory(data -> new SimpleDoubleProperty((Double) data.getValue().get("Amount")).asObject());

        table.getColumns().addAll(idCol, orderIdCol, valueCol, dateCol, amountCol);

        // Ajusta política de redimensionamento para distribuir o espaço corretamente
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Carrega os dados do banco
        ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Transaction WHERE OrderID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double value = rs.getDouble("Value");
                if(value == 0.00){
                    sql="Select Value from CoinHistory Where Coin= ? Order By Date Desc limit 1";
                    PreparedStatement Newstmt = conn.prepareStatement(sql);
                    Newstmt.setInt(1, rs.getInt("O.Coin"));
                    ResultSet RS = Newstmt.executeQuery();
                    RS.next();
                    value= RS.getDouble("Value");
                }
                Map<String, Object> row = new HashMap<>();
                row.put("ID", rs.getInt("ID"));
                row.put("OrderID", rs.getInt("OrderID"));
                row.put("Value", value);
                row.put("Date", rs.getTimestamp("Date"));
                row.put("Amount", rs.getDouble("Amount"));
                items.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        table.setItems(items);

        // Container para a tabela para garantir preenchimento adequado
        VBox root = new VBox(table);
        root.setPrefSize(460, 400);

        // Configuração da janela
        Stage stage = new Stage();
        Scene scene = new Scene(root, 660, 400);

        // Adiciona o arquivo CSS à cena
        scene.getStylesheets().add(getClass().getResource("/com/example/catcoins/styles/TransactionsDetails.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Order Details " + orderId);

        // Impede que o usuário redimensione a janela
        stage.setResizable(false);

        stage.show();
    }


    // Método auxiliar para formatar datas
    private String formatDate(Timestamp timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timestamp);
    }

    private void configureNumberFormats() {
        currencyFormat.setMaximumFractionDigits(2);
        coinFormat.setMaximumFractionDigits(4);
    }

    private void loadUserDetails(User UserDetails) {

        // Atualiza os campos com os dados básicos do usuário
        userNameText.setText(UserDetails.getName());
        userEmailText.setText(UserDetails.getEmail());
        userRoleText.setText(UserDetails.getRole().toString());
        userStatusText.setText(UserDetails.getStatus().toString());

        if (UserDetails instanceof Client) {
            // Atualiza os campos financeiros
            double balance = ((Client) UserDetails).getWallet().getBalance();
            String currency = ((Client) UserDetails).getWallet().getCurrency();
            balanceText.setText(String.format("%.2f %s", balance, currency));
        } else {
            balanceText.setText("0.00 EUR"); // Valor padrão se não encontrar carteira
        }

        System.out.println("User details loaded successfully for user ID: " + UserDetails.getID());

    }

    private void LoadOrders(int userId) {
        try{
            ArrayList<Order> OrderHistoryList = OrdDao.GetUserOrderHistory(WalDao.GetByUserID(userId));
            ObservableList<Order> orderList = FXCollections.observableArrayList(OrderHistoryList);
            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            transactionsTable.setItems(orderList);

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
    }

    @FXML
    private void goBack(){
        try {
            Main.setRoot("ManageUser.fxml", super.getLoggedUser());
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
        }
    }
}