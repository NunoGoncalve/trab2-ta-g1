package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
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
    private StackPane Background;

    @FXML private Button BuyBttn;

    @FXML private Button SellBttn;

    private Client client;
    private CoinDAO CnDao =  new CoinDAO();
    private OrderDAO OrdDao =  new OrderDAO();

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
        if(super.getLoggedUser() instanceof Client){
            client = (Client) super.getLoggedUser();
            BuyBttn.setVisible(true);
            SellBttn.setVisible(true);
        }
    }

    public void setCoin(Coin CoinDetails){
        CryptoCoin = CoinDetails;
        lineChart.setTitle(CryptoCoin.getName());
        LoadGraph("Day");
        LoadData();
    }

    @FXML
    private void goBack(){
        try {
            Main.setRoot("Main.fxml", super.getLoggedUser());
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
        }
    }

    private void LoadData() {

        try{
            ArrayList<Order> orders = OrdDao.GetCoinOrders(CryptoCoin.getID());

            for (Order ord : orders) {
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

                Label type = new Label(ord.getType());
                type.getStyleClass().add("axis-label");
                grid.add(type, 0, 0);

                double valueToShow = ord.getValue() == 0 ? CryptoCoin.getValue() : ord.getValue();
                Label Value = new Label(String.format("%.2f", valueToShow));
                Value.alignmentProperty().setValue(Pos.CENTER);
                Value.getStyleClass().add("axis-label");
                grid.add(Value, 1, 0);

                Label Amount = new Label(String.valueOf(ord.getAmount()));
                Amount.getStyleClass().add("axis-label");
                grid.add(Amount, 2, 0);
                Scroll.getChildren().add(grid);
            }

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }

    }

    @FXML
    private void FilterGraph(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();
        LoadGraph(clickedItem.getId());
    }

    private void LoadGraph(String filter){
        lineChart.getData().clear();
        try{
            lineChart.getData().add(CnDao.GetCoinHistory(CryptoCoin.getID(), filter));
        }catch (SQLException e){
            DatabaseConnection.HandleConnectionError(Background, e);
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
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    @FXML
    private void OrderCoin(ActionEvent event){
        double Value=0.0;
        int Amount=0;
        Boolean flag=false;
        Button clickedBttn = (Button) event.getSource();
        String OrderType = clickedBttn.getId().replace("Bttn", "");
        WalletDAO WltDAO = new WalletDAO();
        //CheckExpired();
        try {
            if (OrderType.equals("Buy")) {
                Value = Double.parseDouble(InputWindow("Value"));
                Amount = Integer.parseInt(InputWindow("Amount"));

                if (Value * Amount > client.getWallet().getBalance()) {
                    AlertUtils.showAlert(Background, "You don't have enough balance!");
                    flag = true;
                } else {
                    client.getWallet().SetBalance(client.getWallet().getBalance() - Amount * Value, client.getWallet().getPendingBalance() + Amount * Value);
                }
            } else {
                Amount = Integer.parseInt(InputWindow("Amount"));
                if (Amount > client.getWallet().GetCoinAmount(CryptoCoin.getID())) {
                    AlertUtils.showAlert(Background, "You don't have enough coins!");
                    flag = true;
                } else {
                    Value = Double.parseDouble(InputWindow("Value"));
                    client.getWallet().UpdatePortfolio(client.getWallet().GetCoinAmount(CryptoCoin.getID()) - Amount, CryptoCoin.getID());
                }
            }
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }

        if(Value== CryptoCoin.getValue()){
            Value=0.00;
        }

        if(!flag){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try{
                Order NewOrder = new Order(OrderType, client.getWallet(), CryptoCoin, Value, Amount, timestamp, OrderStatus.Active);
                OrdDao.Add(NewOrder);
                MarketInfo MkInfo = OrdDao.OrderSearch(NewOrder);

                if(MkInfo.Orderdone()){
                    AlertUtils.showAlert(Background, "Your order has been completed");
                    EmailConfig.SendEmail(client.getEmail(), "The Order with ID: "+NewOrder.getId()+" has been completed","Order complete");
                }else{
                    AlertUtils.showAlert(Background, "Your order has been posted");
                }
                if(MkInfo.Matchdone()){
                    UserDAO UsrDao = new UserDAO();
                    EmailConfig.SendEmail(UsrDao.GetUserByWalletID(MkInfo.MatchWalletID()).getEmail(), "The Order with ID: "+MkInfo.MatchID()+" has been completed","Order complete");
                }
                WltDAO.GetUpdatedInfo(client.getWallet());
            }catch (SQLException e){
                DatabaseConnection.HandleConnectionError(Background, e);
            }


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
        AlertUtils.showAlert(Background, "The requested information has been sent to your email");

    }
}