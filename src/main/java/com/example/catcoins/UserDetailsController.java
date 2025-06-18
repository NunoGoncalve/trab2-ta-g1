package com.example.catcoins;

import com.example.catcoins.model.Transaction;
import com.example.catcoins.model.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, Integer> idColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableColumn<Transaction, String> coinColumn;
    @FXML
    private TableColumn<Transaction, Double> valueColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private VBox Stack;
    @FXML
    private BorderPane MainPanel;


    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private int userId;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);

        if (user != null) {
            loadUserDetails(userId);
            loadTransactions(userId);
        }
    }

    public void setUserDetails(int id) {
        userId = id;
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
        idColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Transaction, Integer>() {
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
        valueColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Transaction, Double>() {
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
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(coinFormat.format(item));
                }
            }
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


    private void loadUserDetails(int userId) {
        // Query 1: Busca informações básicas do usuário
        String userSql = "SELECT Name, Email, Role, Status FROM User WHERE ID = ?";

        // Query 2: Busca informações da carteira do usuário
        String walletSql = "SELECT Balance, Currency FROM UserClientWallet WHERE ID = ?";




        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             // Primeira query para dados do usuário
             PreparedStatement userStmt = conn.prepareStatement(userSql);
             // Segunda query para dados da carteira
             PreparedStatement walletStmt = conn.prepareStatement(walletSql)) {

            // Executa a primeira query (dados do usuário)
            userStmt.setInt(1, userId);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                // Atualiza os campos com os dados básicos do usuário
                userNameText.setText(userRs.getString("Name"));
                userEmailText.setText(userRs.getString("Email"));
                userRoleText.setText(userRs.getString("Role"));
                userStatusText.setText(userRs.getString("Status"));

                // Executa a segunda query (dados da carteira)
                walletStmt.setInt(1, userId);
                ResultSet walletRs = walletStmt.executeQuery();

                if (walletRs.next()) {
                    // Atualiza os campos financeiros
                    double balance = walletRs.getDouble("Balance");
                    String currency = walletRs.getString("Currency");
                    balanceText.setText(String.format("%.2f %s", balance, currency));
                } else {
                    balanceText.setText("0.00 USD"); // Valor padrão se não encontrar carteira
                }

                System.out.println("User details loaded successfully for user ID: " + userId);
            } else {
                System.out.println("No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.err.println("Error loading user details: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    public int getWalletId(int userId) {
        String sql = "SELECT WalletID FROM UserClientWallet WHERE ID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("WalletID");
            } else {
                System.out.println("No wallet found with ID: " + userId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return userId;

        }
        return userId;
    }


    private void loadTransactions(int userId) {

        int userIDWallet = getWalletId(userId);

        String sql = " SELECT t.ID,t.Type, t.Value,t.Date, t.Coin ,Sum(Transaction.Amount) as Amount " +
                "FROM Transaction inner join `Order` t on Transaction.OrderID=t.ID " +
                "Where Wallet = ? group by OrderID Order by Date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userIDWallet);
            ResultSet rs = stmt.executeQuery();

            transactionList.clear();
            while (rs.next()) {
                double value = rs.getDouble("Value");
                if(value == 0.00){
                    sql="Select Value from CoinHistory Where Coin= ? Order By Date Desc limit 1";
                    PreparedStatement Newstmt = conn.prepareStatement(sql);
                    Newstmt.setInt(1, rs.getInt("t.Coin"));
                    ResultSet RS = Newstmt.executeQuery();
                    RS.next();
                    value= RS.getDouble("Value");
                }
                Transaction t = new Transaction(
                        rs.getInt("ID"),
                        rs.getString("Type"),
                        rs.getString("Coin"),
                        value,
                        rs.getDouble("Amount"),
                        rs.getTimestamp("Date").toString()  // Alterado para getTimestamp
                );
                transactionList.add(t);
            }
            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            transactionsTable.setItems(transactionList);
            System.out.println("Loaded " + transactionList.size() + " Order for user ID: " + userId);

        } catch (SQLException e) {
            System.err.println("Error loading Orders: " + e.getMessage());
            handleDatabaseError(e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
    }

    // SELECT * FROM Transaction WHERE OrderId = 138;

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        System.err.println("Database error: " + e.getMessage());
        // Aqui você poderia adicionar um alerta para o usuário
        // Alert alert = new Alert(Alert.AlertType.ERROR);
        // alert.setTitle("Database Error");
        // alert.setHeaderText("Error accessing database");
        // alert.setContentText("Could not load user data: " + e.getMessage());
        // alert.showAndWait();
    }

    @FXML
    private void goBack(){
        try {
            Main.setRoot("ManageUser.fxml", super.getLoggedUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}