package com.example.catcoins;

import com.example.catcoins.model.Coin;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarketController extends MenuLoader {

    private Coin CryptoCoin;

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;

    @FXML
    private LineChart<String, Number> lineChart;

    private Stage stage;

    @FXML
    private VBox Scroll;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    public void setCoin(int CoinID){
        CryptoCoin = new Coin(CoinID);
        loadData();
    }

    @FXML
    private void goBack(){
        try {
            Main.setRoot("Main.fxml", super.getLoggedUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadData() {
        // Create a new series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last24h = now.minusHours(24);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowStr = now.format(formatter);
            String last24hStr = last24h.format(formatter);

            String sql = "SELECT * FROM CoinHistory WHERE Coin = ? " +
                    "AND Date >= '" + last24hStr + "' " +
                    "AND Date <= '" + nowStr + "'";


            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, CryptoCoin.getID());
            ResultSet CoinResult = stmt.executeQuery();
            formatter = DateTimeFormatter.ofPattern("HH:mm");
            while (CoinResult.next()) {
                series.getData().add(new XYChart.Data<>(CoinResult.getTimestamp("Date").toLocalDateTime().format(formatter), CoinResult.getDouble("Value")));
            }

            sql = "SELECT Type, Value, Amount FROM Transaction WHERE Coin = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, CryptoCoin.getID());
            ResultSet TransactResult = stmt.executeQuery();
            while (TransactResult.next()) {
                GridPane grid = new GridPane();
                grid.getStyleClass().add("transaction");
                grid.setPadding(new Insets(10));
                grid.setMaxWidth(1000);
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setHgrow(Priority.ALWAYS);
                col1.setFillWidth(true);
                col1.setHalignment(HPos.CENTER);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setHgrow(Priority.ALWAYS);
                col2.setFillWidth(true);
                col2.setHalignment(HPos.CENTER);

                ColumnConstraints col3 = new ColumnConstraints();
                col3.setHgrow(Priority.ALWAYS);
                col3.setFillWidth(true);
                col3.setHalignment(HPos.CENTER);
                grid.getColumnConstraints().addAll(col1, col2, col3);

                Label type = new Label(TransactResult.getString("Type"));
                type.getStyleClass().add("axis-label");
                grid.add(type, 0, 0);

                Label Value = new Label(TransactResult.getString("Value"));
                Value.alignmentProperty().setValue(Pos.CENTER);
                Value.getStyleClass().add("axis-label");
                grid.add(Value, 1, 0);

                Label Amount = new Label(TransactResult.getString("Amount"));
                Amount.getStyleClass().add("axis-label");
                grid.add(Amount, 2, 0);
                Scroll.getChildren().add(grid);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Add the series to the chart
        lineChart.getData().add(series);

    }

    @FXML
    private void exportLineChartToCSV() throws IOException {
        try (FileWriter writer = new FileWriter("src/main/resources/CSV/market.csv")) {
            // Write header
            writer.write("Hour,Value\n");
            for (XYChart.Series<String, Number> series : lineChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    String line = String.format("%s,%s\n",
                            data.getXValue(),
                            data.getYValue());
                    writer.write(line);
                }
            }
        }

        String content = "Data related to the Chart", subject ="Chart CSV data";
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content , subject, "src/main/resources/CSV/market.csv");
        java.nio.file.Path path = Paths.get("src/main/resources/CSV/market.csv");
        try {
            Files.delete(path);
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.out.println("Failed to delete the file: " + e.getMessage());
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Enviado");
        alert.setContentText("As informações foram enviadas para o seu email");
        alert.initOwner(stage);
        alert.showAndWait();

    }
}
