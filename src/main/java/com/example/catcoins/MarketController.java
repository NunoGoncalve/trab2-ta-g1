package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.stage.Stage;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarketController extends MenuLoader{

    @FXML
    private BorderPane MainPanel;

    @FXML
    private VBox Stack;

    @FXML
    private LineChart<String, Number> lineChart;

    private Stage stage;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    public void initialize() {
        // Create a new series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DatabaseConnection.getConnection()){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last24h = now.minusHours(24);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowStr = now.format(formatter);
            String last24hStr = last24h.format(formatter);


            //String sql = "SELECT * FROM CoinHistory WHERE Coin = 1 and Date like '"+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"%'";
            String sql = "SELECT * FROM CoinHistory WHERE Coin = 1 " +
                    "AND Date >= '" + last24hStr + "' " +
                    "AND Date <= '" + nowStr + "'";


            PreparedStatement stmt = conn.prepareStatement(sql);
            //stmt.setString(1, CoinID);
            ResultSet CoinResult = stmt.executeQuery();
            formatter = DateTimeFormatter.ofPattern("HH:mm");
            while (CoinResult.next()) {
                series.getData().add(new XYChart.Data<>(CoinResult.getTimestamp("Date").toLocalDateTime().format(formatter), CoinResult.getDouble("Value")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Add the series to the chart
        lineChart.getData().add(series);

    }

    @FXML
    public void exportLineChartToCSV() throws IOException {
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
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content , subject);
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
