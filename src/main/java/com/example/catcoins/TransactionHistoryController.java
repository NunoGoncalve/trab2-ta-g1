package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Transaction;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
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

public class TransactionHistoryController extends MenuLoader {

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

        String sql = " SELECT * FROM Transaction WHERE Wallet = ? ";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Client LoggedClient = (Client) super.getLoggedUser();
            stmt.setInt(1,   LoggedClient.getWallet().getID());
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

        String subject = "Histórico de Transações CatCoins";
        String content = "Este e-mail foi gerado automaticamente pelo sistema CatCoins.";

        // Envia o e-mail com texto simples e anexo
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content, subject, "src/main/resources/CSV/transactions.csv");

        // Limpa o arquivo temporário
        java.nio.file.Path path = Paths.get("src/main/resources/CSV/transactions.csv");
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Erro ao remover arquivo: " + e.getMessage());
        }

    // Substituindo o Alert padrão pelo ShowAlert personalizado
        showAlert(Alert.AlertType.INFORMATION, "Exportação concluída", "O histórico completo foi enviado para seu e-mail!");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        // Verifica se o Background está disponível
        if (Background == null) {
            // Se não estiver disponível, tenta encontrar um StackPane na hierarquia
            try {
                Scene currentScene = MainPanel.getScene();
                if (currentScene != null) {
                    Parent currentRoot = currentScene.getRoot();
                    if (currentRoot instanceof StackPane) {
                        Background = (StackPane) currentRoot;
                    } else {
                        // Procura por um StackPane na hierarquia
                        Node backgroundNode = currentRoot.lookup("#Background");
                        if (backgroundNode instanceof StackPane) {
                            Background = (StackPane) backgroundNode;
                        } else {
                            // Se não encontrar o Background, cria um alerta padrão como fallback
                            Alert alert = new Alert(type);
                            alert.setTitle(title);
                            alert.setHeaderText(null);
                            alert.setContentText(message);
                            alert.initOwner((Stage) MainPanel.getScene().getWindow());
                            alert.showAndWait();
                            return;
                        }
                    }
                } else {
                    // Se não houver cena, cria um alerta padrão como fallback
                    Alert alert = new Alert(type);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                    return;
                }
            } catch (Exception e) {
                // Em caso de erro, usa o alerta padrão como fallback
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
                return;
            }
        }

        // Cria o conteúdo do diálogo (não em tela cheia)
        VBox dialog = new VBox(3);
        dialog.setSpacing(25);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: white;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(170);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white");

        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");

        dialog.getChildren().addAll(messageLabel, okButton);

        // Cria um overlay semi-transparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);"); // 0.2 = 20% de opacidade

        // Adiciona o diálogo ao overlay e centraliza
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);

        // Adiciona o overlay ao StackPane raiz
        Background.getChildren().add(overlay);

        // Remove o overlay quando OK é clicado
        okButton.setOnAction(e -> Background.getChildren().remove(overlay));
    }


}