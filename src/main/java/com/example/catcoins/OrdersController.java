package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Transaction;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;


import java.awt.event.ActionEvent;
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

public class OrdersController extends MenuLoader {

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
    private TableColumn<Transaction, Void> ActionsColumn;
    @FXML
    private VBox Stack;
    @FXML
    private BorderPane MainPanel;
    @FXML
    private StackPane Background;


    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private Scene scene;
    private Parent root;

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

        // Formatação personalizada para quantidade
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

        // Configuração do botão "✖" na coluna de ações
        ActionsColumn.setCellFactory(col -> new TableCell<>() {
            Button deleteButton = new Button("✖");
            {

                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteOrder(transaction.getId());
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


    private void loadTransactions() {

        String sql = "SELECT O.ID,O.Type, O.Value,O.Date, O.Status, Coin.Name as Coin, O.Amount as Amount,O.Coin as CoinID FROM `Order` O inner join Coin on O.coin= Coin.ID Where `Wallet` = ? and O.Status = 'Open' group by O.ID Order by Date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Client LoggedClient = (Client) super.getLoggedUser();
            stmt.setInt(1,   LoggedClient.getWallet().getID());
            ResultSet rs = stmt.executeQuery();

            transactionList.clear();

            while (rs.next()) {
                int id = rs.getInt("ID");
                double value = rs.getDouble("Value");
                if(value == 0.00){
                    sql="Select Value from CoinHistory Where Coin= ? Order By Date Desc limit 1";
                    PreparedStatement Newstmt = conn.prepareStatement(sql);
                    Newstmt.setInt(1, rs.getInt("CoinID"));
                    ResultSet RS = Newstmt.executeQuery();
                    RS.next();
                    value= RS.getDouble("Value");
                }
                Transaction t = new Transaction(
                        id,
                        rs.getString("Type"),
                        rs.getString("Coin"),
                        value,
                        rs.getDouble("Amount"),
                        rs.getTimestamp("Date").toString()
                );

                transactionList.add(t);
            }



            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY) ; // removeu aquele espaço branco
            transactionsTable.setItems(transactionList);
            System.out.println("Loaded " + transactionList.size() + " Orders for user ID: " + LoggedClient.getWallet().getID());

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
    private boolean confirmarAcao(String titulo, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10");

        Label Label = new Label(message);
        Label.setStyle("-fx-text-fill: white;");

        VBox content = new VBox(10, Label);
        dialogPane.setContent(content);

        ButtonType okButtonType = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType CancelButtonType = new ButtonType("No", ButtonBar.ButtonData.NO);
        dialogPane.getButtonTypes().addAll(okButtonType, CancelButtonType);
        dialogPane.lookupButton(okButtonType).setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");
        dialogPane.lookupButton(CancelButtonType).setStyle("-fx-background-color: red; -fx-max-width: 50; -fx-border-radius: 10; -fx-text-fill: white;");

        return alert.showAndWait().filter(resposta -> resposta == okButtonType).isPresent();
    }

    public void deleteOrder(int id) {
        if (!confirmarAcao("Confimation", "Are you sure you want to cancel this order?")) {
            return;
        }

        String sql = "UPDATE `Order` SET Status = 'Cancelled' WHERE ID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order successfully cancelled!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No order was cancelled");
            }
            sql = "Select Type,Amount,Coin from `Order` where ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Client LoggedClient = (Client) super.getLoggedUser();
            if(rs.getString("Type").equals("Buy")){
                LoggedClient.getWallet().SetBalance(LoggedClient.getWallet().getBalance()+LoggedClient.getWallet().getPendingBalance(), 0.00);
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
                String sql2 = "Update Portfolio Set Amount = Amount + ? where WalletID = ? and Coin=?";
                stmt = conn.prepareStatement(sql2);
                stmt.setDouble(1, rs.getInt("Amount"));
                stmt.setInt(2, LoggedClient.getWallet().getID());
                stmt.setInt(3, rs.getInt("Coin"));
                stmt.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting order: " + e.getMessage());
            e.printStackTrace();
        }
    }


}