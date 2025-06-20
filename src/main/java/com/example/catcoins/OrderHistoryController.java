package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Transaction;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderHistoryController extends MenuLoader {

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
    @FXML
    private StackPane Background;


    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private Stage stage;




    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
        loadTransactions();
        configureTableColumns();
        configureNumberFormats();

    }


//    @FXML
//    public void initialize() {
//        configureTableColumns();
//        configureNumberFormats();
//        loadTransactions();
//
//    }

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


    private void loadTransactions() {

        String sql = " SELECT O.ID,O.Type, O.Value,O.Date, O.Coin ,Sum(Transaction.Amount) as Amount " +
                    "FROM Transaction inner join `Order` O on Transaction.OrderID=O.ID " +
                    "Where Wallet = ? group by OrderID Order by Date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Client LoggedClient = (Client) super.getLoggedUser();
            stmt.setInt(1,   LoggedClient.getWallet().getID());
            ResultSet rs = stmt.executeQuery();

            transactionList.clear();
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

            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY) ; // removeu aquele espaço branco
            transactionsTable.setItems(transactionList);
            System.out.println("Loaded " + transactionList.size() + " transactions for user ID: " + LoggedClient.getWallet().getID());

        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            handleDatabaseError(e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        System.err.println("Database error: " + e.getMessage());
    }

    @FXML
    private void exportLineChartToCSV() throws IOException {
        // Garante que o diretório existe
        java.nio.file.Path dirPath = Paths.get("src/main/resources/CSV");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Cria o arquivo CSV com separador ponto e vírgula
        try (FileWriter writer = new FileWriter("src/main/resources/CSV/transactions.csv")) {
            writer.write("ID;Type;Coin;Value;Amount;Date\n");
            for (Transaction transaction : transactionsTable.getItems()) {
                String line = String.format(
                        "%d;\"%s\";\"%s\";%.2f;%.4f;\"%s\"\n",
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getCoin(),
                        transaction.getValue(),
                        transaction.getAmount(),
                        transaction.getDate()
                );
                writer.write(line);
            }
        }

        String subject = "CatCoins Transaction History";
        String content = "This email was automatically generated by the CatCoins system.";

        // Envia o e-mail com texto simples e anexo
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content, subject, "src/main/resources/CSV/transactions.csv");

        // Limpa o arquivo temporário
        java.nio.file.Path path = Paths.get("src/main/resources/CSV/transactions.csv");
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Error removing file: " + e.getMessage());
        }

    // Substituindo o Alert padrão pelo ShowAlert personalizado
        AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "Export completed", "The full history has been sent to your email!");
    }
}