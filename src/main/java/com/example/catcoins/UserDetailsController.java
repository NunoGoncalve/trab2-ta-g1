package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

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

    int userId = ManageUserController.GlobalData.userId;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);

        if (user != null) {
            loadUserDetails(userId);
            loadTransactions(userId);
        } else {
            clearUserDetails();
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

    private void configureNumberFormats() {
        currencyFormat.setMaximumFractionDigits(2);
        coinFormat.setMaximumFractionDigits(4);
    }


    private void loadUserDetails(int userId) {
        // Query 1: Busca informações básicas do usuário
        String userSql = "SELECT Name, Email, Role, Status FROM User WHERE ID = ?";

        // Query 2: Busca informações da carteira do usuário
        String walletSql = "SELECT Balance, Currency FROM UserClientWallet WHERE ID = ?";




        try (Connection conn = DatabaseConnection.getConnection();
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
                clearUserDetails();
            }

        } catch (SQLException e) {
            System.err.println("Error loading user details: " + e.getMessage());
            handleDatabaseError(e);
        }
    }

    private void loadTransactions(int userId) {
        String sql = """
        SELECT t.ID, t.Type, c.Name AS Coin, t.Value, t.Amount, t.Date
        FROM Transaction t
        JOIN Wallet w ON t.Wallet = w.ID
        JOIN Coin c ON t.Coin = c.ID
        JOIN UserClientWallet ucw ON ucw.WalletID = w.ID
        WHERE ucw.ID = ?
        ORDER BY t.Date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            transactionList.clear();
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("ID"),
                        rs.getString("Type"),
                        rs.getString("Coin"),
                        rs.getDouble("Value"),
                        rs.getDouble("Amount"),
                        rs.getTimestamp("Date").toString()  // Alterado para getTimestamp
                );
                transactionList.add(t);
            }

            transactionsTable.setItems(transactionList);
            System.out.println("Loaded " + transactionList.size() + " transactions for user ID: " + userId);

        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            handleDatabaseError(e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
    }
    private void clearUserDetails() {
        userNameText.setText("");
        userEmailText.setText("");
        userRoleText.setText("");
        userStatusText.setText("");
        balanceText.setText("");
        coinBalanceText.setText("");
        transactionsTable.setItems(FXCollections.emptyObservableList());
    }

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
}