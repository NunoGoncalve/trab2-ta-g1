package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BalanceHistoryController extends MenuLoader {

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
    private TableView<BalanceHistory> BalanceHistoryTable;
    @FXML
    private TableColumn<Order, Date> DateColumn;
    @FXML
    private TableColumn<Order, Double> BalanceColumn;
    @FXML
    private TableColumn<Order, Double> PendingBalanceColumn;
    @FXML
    private VBox Stack;
    @FXML
    private BorderPane MainPanel;
    @FXML
    private StackPane Background;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
        loadHistory();
    }


    private void loadHistory() {
        BalanceColumn.setCellValueFactory(new PropertyValueFactory<>("Balance"));
        PendingBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("PendingBalance"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("Date"));

        Client LoggedClient = (Client) super.getLoggedUser();
        BalanceHistoryDAO BlncHstrDao = new BalanceHistoryDAO();

        try {
            ArrayList<BalanceHistory> BalanceHistory =BlncHstrDao.GetWalletBalanceHistory(LoggedClient.getWallet());
            ObservableList<BalanceHistory> BalanceHistoryList = FXCollections.observableArrayList(BalanceHistory);
            BalanceHistoryTable.setItems(BalanceHistoryList);

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            BalanceHistoryTable.setItems(FXCollections.emptyObservableList());
        }
    }

    @FXML
    private void exportLineChartToCSV() throws IOException {
        // Garante que a pasta existe
        java.nio.file.Path dirPath = Paths.get("src/main/resources/CSV");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Cria o arquivo CSV com separador ponto e vírgula
        try (FileWriter writer = new FileWriter("src/main/resources/CSV/balanceHistory.csv")) {
            writer.write("Date;Balance;PendingBalance\n");
            for (BalanceHistory bh : BalanceHistoryTable.getItems()) {
                String line = String.format(Locale.US, "%s;%.2f;%.2f\n",
                        bh.getDate().toString(),
                        bh.getBalance(),
                        bh.getPendingBalance()
                );
                writer.write(line);
            }
        }

        String subject = "CatCoins Balance History";
        String content = "This email was automatically generated by the CatCoins system.";

        // Envia o e-mail com texto simples e anexo
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content, subject, "src/main/resources/CSV/balanceHistory.csv");

        // Limpa o arquivo temporário
        java.nio.file.Path path = Paths.get("src/main/resources/CSV/balanceHistory.csv");
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Error removing file: " + e.getMessage());
        }

        AlertUtils.showAlert(Background, "The full history has been sent to your email!");
    }
}