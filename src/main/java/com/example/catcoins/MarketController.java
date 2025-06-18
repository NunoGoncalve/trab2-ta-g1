package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Coin;
import com.example.catcoins.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.Locale;
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

            String sql = "SELECT `Order`.ID,`Order`.Type, `Order`.Value,`Order`.Date ,Sum(Transaction.Amount) as Amount " +
                    "FROM `Transaction` inner join `Order` on Transaction.OrderID=`Order`.ID " +
                    "Where Coin = ? group by OrderID Order by Date DESC";
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
        LoadGraph(clickedItem.getId());
    }

    private void LoadGraph(String filter){
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){

            String sql = "SELECT XValue, Value FROM ( SELECT DATE_FORMAT(Date, '%H:%i') AS XValue, Value, Date FROM CoinHistory WHERE Date >= NOW() - INTERVAL 24 HOUR AND Coin = ? ORDER BY Date DESC LIMIT 24 ) sub ORDER BY Date ASC";
            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setInt(1, CryptoCoin.getID());

            if(filter.equals("Week")){
                sql = "SELECT DAY(Date) AS XValue, AVG(Value) AS Value FROM CoinHistory WHERE Date >= CURDATE() - INTERVAL 7 DAY AND Date <= CURDATE() AND Coin=? GROUP BY Coin, XValue ORDER BY Coin, XValue";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, CryptoCoin.getID());
            }else if(filter.equals("Month")){
                sql = "SELECT DAY(Date) AS XValue, AVG(Value) AS Value FROM CoinHistory WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Coin=? GROUP BY Coin, XValue ORDER BY Coin, XValue";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, LocalDate.now().getMonthValue());
                stmt.setInt(2, LocalDate.now().getYear());
                stmt.setInt(3, CryptoCoin.getID());
            }
            ResultSet CoinResult = stmt.executeQuery();

            while (CoinResult.next()) {
                series.getData().add(new XYChart.Data<>(CoinResult.getString("XValue"), CoinResult.getDouble("Value")));
            }

            lineChart.getData().add(series);
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    private void CheckExpired(){
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            String sql="Select Wallet, ID from `Order` WHERE Status NOT IN('Expired','Filled') AND Date <= NOW() - INTERVAL 24 HOUR";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                sql="Select ID, Email from UserClientWallet WHERE Wallet = ?";
                PreparedStatement stmts = conn.prepareStatement(sql);
                stmts.setInt(1, stmt.getResultSet().getInt("Wallet"));
                ResultSet result = stmts.executeQuery(sql);
                result.next();
                EmailConfig.SendEmail(result.getString("Email"),"Your order with ID "+rs.getInt("ID")+"has expired", "Order expired");
            }

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
        //CheckExpired();
        if(OrderType.equals("Buy")){
            Value = Double.parseDouble(InputWindow("Value"));
            Amount = Integer.parseInt(InputWindow("Amount"));

            if(Value*Amount > client.getWallet().getBalance()) {
                Error();
                flag=true;
            }else{
                client.getWallet().SetBalance(client.getWallet().getBalance()-Amount*Value, client.getWallet().getPendingBalance()+Amount*Value);
            }
        }else{
            Amount = Integer.parseInt(InputWindow("Amount"));
            if(Amount> client.getWallet().GetCoinAmount(CryptoCoin.getID()) ){
                Error();
                flag=true;
            }else{
                Value = Double.parseDouble(InputWindow("Value"));
                client.getWallet().UpdatePortfolio(client.getWallet().GetCoinAmount(CryptoCoin.getID())-Amount, CryptoCoin.getID());
            }
        }

        if(Value== CryptoCoin.getValue()){
            Value=0.00;
        }

        if(!flag){
            String NewOrder = "INSERT INTO `Order` (Type, Wallet, Coin, Amount, Value) Values (?, ?, ?, ?, ?)"; // `Order` -> to escape word Order

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

                String SearchOrder = "Call SaleSearch(?, ?, ?, ?, ?, ?,?, ?, ?, ?)";
                CallableStatement stmts = conn.prepareCall(SearchOrder);
                stmts.setString(1, OrderType);
                stmts.setInt(2, OrderID);
                stmts.setInt(3, client.getWallet().getID());
                stmts.setInt(4, CryptoCoin.getID());
                stmts.setInt(5, Amount);
                stmts.setDouble(6, Value);
                stmts.registerOutParameter(7, Types.BOOLEAN);
                stmts.registerOutParameter(8, Types.BOOLEAN);
                stmts.registerOutParameter(9, Types.INTEGER);
                stmts.registerOutParameter(10, Types.INTEGER);
                stmts.execute();
                Boolean Orderdone= stmts.getBoolean(7), Matchdone=stmts.getBoolean(8);
                int MatchID = stmts.getInt(10), MatchWalletID = stmts.getInt(9);
                if(Orderdone){
                    AlertUtils.showAlert(StackPane,Alert.AlertType.INFORMATION, "Order Complete", "Your order has been completed");
                    EmailConfig.SendEmail(client.getEmail(), "The Order with ID: "+OrderID+" has been completed","Order complete");
                }else{
                    AlertUtils.showAlert(StackPane,Alert.AlertType.INFORMATION, "Order Posted", "Your order has been posted");
                }
                if(Matchdone){

                    EmailConfig.SendEmail(client.getUserEmail(MatchWalletID), "The Order with ID: "+MatchID+" has been completed","Order complete");
                }


            }catch (SQLException e){
                e.printStackTrace();
            }
            client.getWallet().GetUpdatedBalance();
            scene = ((Node) event.getSource()).getScene();
            root = scene.getRoot();
            Node node = root.lookup("#balanceLabel");
            if (node instanceof Label) {
                Label label = (Label) node;
                label.setText(String.format("%.2f", client.getWallet().getBalance()));
            }
            node = root.lookup("#PendingbalanceLabel");
            if (node instanceof Label) {
                Label label = (Label) node;
                label.setText(String.format("%.2f", client.getWallet().getPendingBalance()));
            }
            LoadData();
        }

    }

    private String InputWindow(String type){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(type);

        TextField inputField = new TextField();
        inputField.setStyle("-fx-background-color: #28323E; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 10px");

        Label inputLabel = new Label("Enter "+type);
        inputLabel.setStyle("-fx-text-fill: white;");

        Label Amount = new Label("Owned: "+ client.getWallet().GetCoinAmount(CryptoCoin.getID()));
        Amount.setStyle("-fx-text-fill: white;");

        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType CancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Button MarketButton = new Button("Market value");
        MarketButton.setStyle("-fx-background-color: #FFA630; -fx-border-radius: 10;");
        MarketButton.setOnAction(event -> {
            inputField.setText(String.format(Locale.US,"%.10f", CryptoCoin.getValue()));
        });

        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, CancelButtonType);

        // Layout for the dialog content
        HBox hBox = new HBox(inputLabel, MarketButton);
        hBox.setSpacing(20);

        VBox content;
        if(type.equals("Amount")){
            content = new VBox(10, inputLabel, Amount, inputField);
        }else{
            content = new VBox(10, hBox, inputField);
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
        java.nio.file.Path dirPath = Paths.get("src/main/resources/CSV");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

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