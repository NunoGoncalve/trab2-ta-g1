package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Coin;
import com.example.catcoins.model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class MarketController extends MenuLoader {
    private Scene scene;
    private Parent root;
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

    @FXML
    private StackPane StackPane;

    @FXML private Button BuyBttn;

    @FXML private Button SellBttn;

    private Client client;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
        if(super.getLoggedUser() instanceof Client){
            client = (Client) super.getLoggedUser();
            BuyBttn.setVisible(true);
            SellBttn.setVisible(true);
        }
    }

    public void setCoin(int CoinID){
        CryptoCoin = new Coin(CoinID);
        lineChart.setTitle(CryptoCoin.getName());
        LoadGraph("Day");
        LoadData();
    }

    @FXML
    private void goBack(){
        try {
            Main.setRoot("Main.fxml", super.getLoggedUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void LoadData() {
        // Create a new series


        try (Connection conn = DatabaseConnection.getInstance().getConnection()){

            String sql = "SELECT Type, Value, Amount FROM Transaction WHERE Coin = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
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

    }

    @FXML
    private void FilterGraph(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();
        String id = clickedItem.getId();
        lineChart.getData().clear();
        LoadGraph(id);
    }

    private void LoadGraph(String filter){

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowStr = now.format(formatter);
        String last24hStr = last24h.format(formatter);

        String sql = "SELECT * FROM CoinHistory WHERE Coin = ? AND Date >= '" + last24hStr + "' AND Date <= '" + nowStr + "'";
        if(filter.equals("Week")){
            sql = "SELECT * FROM CoinHistory WHERE Coin = ? AND Date >= NOW() - INTERVAL 7 DAY";
        }else if(filter.equals("Month")){
            sql = "SELECT * FROM CoinHistory WHERE Coin = ? AND Date >= NOW() - INTERVAL 31 DAY";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            PreparedStatement stmt = conn.prepareStatement(sql);
            formatter = DateTimeFormatter.ofPattern("HH:mm");
            stmt.setInt(1, CryptoCoin.getID());
            ResultSet CoinResult = stmt.executeQuery();
            if(!filter.equals("Day")) {
                formatter = DateTimeFormatter.ofPattern("dd");
            }

            while (CoinResult.next()) {
                series.getData().add(new XYChart.Data<>(CoinResult.getTimestamp("Date").toLocalDateTime().format(formatter), CoinResult.getDouble("Value")));
            }

            lineChart.getData().add(series);
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    private void OrderCoin(ActionEvent event){
        double Value=0.0;
        int Amount=0;
        Boolean flag=false;
        Button clickedBttn = (Button) event.getSource();
        String OrderType = clickedBttn.getId().replace("Bttn", "");


        if(OrderType.equals("Buy")){
            Value = Double.parseDouble(InputWindow("Value"));
            if(Value> client.getWallet().getBalance() ) {
                Error();
                flag=true;
            }else{
                Amount = Integer.parseInt(InputWindow("Amount"));
            }
        }else{
            Amount = Integer.parseInt(InputWindow("Amount"));
            if(Amount> client.getWallet().GetCoinAmount(CryptoCoin.getID()) ){
                // Create the dialog content (not fullscreen)
                Error();
                flag=true;
            }else{
                Value = Double.parseDouble(InputWindow("Value"));
            }
        }

        if(!flag){
            String NewOrder = "INSERT INTO `Order` (Type, Wallet, Coin, Amount, Value) Values (?, ?, ?, ?, ?)"; // `Order` -> to escape word Order
            String NewTransaction = "INSERT INTO Transaction (Type, Wallet, Coin, Amount, Value) Values (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getInstance().getConnection()){
                PreparedStatement stmt = conn.prepareStatement(NewOrder, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, OrderType);
                stmt.setInt(2, client.getWallet().getID());
                stmt.setInt(3, CryptoCoin.getID());
                stmt.setInt(4, Amount);
                stmt.setDouble(5, Value);
                stmt.executeUpdate();
                ResultSet result = stmt.getGeneratedKeys();
                result.next();
                int OrderID = result.getInt(1);

                stmt = conn.prepareStatement(NewTransaction);
                stmt.setString(1, OrderType);
                stmt.setInt(2, client.getWallet().getID());
                stmt.setInt(3, CryptoCoin.getID());
                stmt.setInt(4, Amount);
                stmt.setDouble(5, Value);
                stmt.executeUpdate();

                String SearchOrder = "Call SaleSearch(?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(SearchOrder);
                stmt.setString(1, OrderType);
                stmt.setInt(2, OrderID);
                stmt.setInt(3, client.getWallet().getID());
                stmt.setInt(4, CryptoCoin.getID());
                stmt.setInt(5, Amount);
                stmt.setDouble(6, Value);
                stmt.executeQuery();


            }catch (SQLException e){
                e.printStackTrace();
            }
            client.getWallet().UpdateBalance();
            scene = ((Node) event.getSource()).getScene();
            root = scene.getRoot();
            Node node = root.lookup("#balanceLabel");
            if (node instanceof Label) {
                Label label = (Label) node;
                label.setText(String.format("%.2f", client.getWallet().getBalance()));
            }
            LoadData();
        }

    }

    private String InputWindow(String type){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(type);

        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType CancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, CancelButtonType);

        // Create the text field
        TextField inputField = new TextField();
        inputField.setStyle("-fx-background-color: #28323E; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 10px");

        Label inputLabel = new Label("Enter "+type);
        inputLabel.setStyle("-fx-text-fill: white;");
        Label Amount = new Label("Owned: "+ client.getWallet().GetCoinAmount(CryptoCoin.getID()));
        Amount.setStyle("-fx-text-fill: white;");
        // Layout for the dialog content
        VBox content;
        if(type.equals("Amount")){

            content = new VBox(10, inputLabel, Amount, inputField);
        }else{
            content = new VBox(10, inputLabel, inputField);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10");
        dialog.getDialogPane().lookupButton(okButtonType).setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");
        dialog.getDialogPane().lookupButton(CancelButtonType).setStyle("-fx-background-color: red; -fx-max-width: 50; -fx-border-radius: 10; -fx-text-fill: white;");

        // Request focus on the text field by default
        Platform.runLater(inputField::requestFocus);

        // Convert the result to the input value when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return inputField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private void Error(){
        VBox dialog = new VBox(3);
        dialog.setSpacing(25);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: white;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(170);
        Label message = new Label("Insufficient Balance");
        message.setStyle("-fx-text-fill: white");
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");
        dialog.getChildren().addAll(message, okButton);

        // Optional: create a semi-transparent background overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);"); // 0.4 = 40% opacity
        // Add the dialog to the overlay and center it
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);
        // Add overlay to the root StackPane
        StackPane.getChildren().add(overlay);

        // Remove overlay when OK is clicked
        okButton.setOnAction(e -> StackPane.getChildren().remove(overlay));
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
        alert.setContentText("The requested information has been sent to your email.");
        alert.initOwner(stage);
        alert.showAndWait();

    }
}