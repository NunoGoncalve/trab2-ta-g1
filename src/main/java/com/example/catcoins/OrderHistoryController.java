package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderHistoryController extends MenuLoader {

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


    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final NumberFormat coinFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private OrderDAO OrdDao = new OrderDAO();

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
        LoadOrderHistory();
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
        valueColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
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
    }

    private void configureNumberFormats() {
        currencyFormat.setMaximumFractionDigits(2);
        coinFormat.setMaximumFractionDigits(4);
    }


    private void LoadOrderHistory() {

        try{
            Client LoggedClient = (Client) super.getLoggedUser();
            ArrayList<Order> OrderHistoryList = OrdDao.GetUserOrderHistory(LoggedClient.getWallet());
            ObservableList<Order> orderList = FXCollections.observableArrayList(OrderHistoryList);
            transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            transactionsTable.setItems(orderList);

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            transactionsTable.setItems(FXCollections.emptyObservableList());
        }
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
            for (Order order : transactionsTable.getItems()) {
                String line = String.format(
                        "%d;\"%s\";\"%s\";%.2f;%.4f;\"%s\"\n",
                        order.getId(),
                        order.getType(),
                        order.getCoin(),
                        order.getValue(),
                        order.getAmount(),
                        order.getDate()
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
        AlertUtils.showAlert(Background, "The full history has been sent to your email!");
    }
}