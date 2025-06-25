package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.User;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserMenuController {

    private Scene scene;
    private Parent root;
    private User LoggedUser;

    @FXML private Button BalanceButton;
    @FXML private Button ManageCoinBttn;
    @FXML private Button ManageUserBttn;
    @FXML private Button ActiveOrdersBttn;
    @FXML private Button OrderHistoryBttn;
    @FXML private Button BalanceHistoryBttn;
    @FXML private StackPane UserMenuPane; // Usando o ID correto do StackPane no FXML
    @FXML private TextField amountField;

    private StackPane Background;

    public void setUser(User user) {
        this.LoggedUser = user;
        if(user.getRole()== Role.Admin) {
            ManageCoinBttn.setVisible(true);
            ManageCoinBttn.setManaged(true);
            ManageUserBttn.setVisible(true);
            ManageUserBttn.setManaged(true);
            BalanceButton.setVisible(false);
            BalanceButton.setManaged(false);
            OrderHistoryBttn.setVisible(false);
            OrderHistoryBttn.setManaged(false);
            ActiveOrdersBttn.setVisible(false);
            ActiveOrdersBttn.setManaged(false);
            BalanceHistoryBttn.setVisible(false);
            BalanceHistoryBttn.setManaged(false);
        }
    }

    public void SetBackground(StackPane background) {
        Background = background;
    }
    @FXML
    void Close() {
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(250), UserMenuPane);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        CloseTransition.setOnFinished(event -> {
            UserMenuPane.setVisible(false);
            UserMenuPane.setManaged(false);
        });
    }

    @FXML
    void GoConfig() {
        GoTo("UserConfig.fxml");
    }

    @FXML
    void AddBalance(ActionEvent event) {
        try {
            // Carrega a janela de adição de saldo como uma nova cena
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddBalance.fxml"));
            Parent addBalanceRoot = loader.load();

            // Cria uma nova janela (Stage) para adicionar saldo
            Stage addBalanceStage = new Stage();
            addBalanceStage.setTitle("Adicionar Saldo");
            addBalanceStage.initModality(Modality.APPLICATION_MODAL);
            addBalanceStage.initOwner(((Node)event.getSource()).getScene().getWindow());

            // Configura a cena
            Scene addBalanceScene = new Scene(addBalanceRoot);
            addBalanceStage.setScene(addBalanceScene);

            // Obtém os componentes da cena
            TextField amountField = (TextField) addBalanceRoot.lookup("#amountField");
            Button confirmButton = (Button) addBalanceRoot.lookup("#ConfirmButton");
            Button cancelButton = (Button) addBalanceRoot.lookup("#CancelButton");

            // Obtém o StackPane Background da janela de adição de saldo
            StackPane addBalanceBackground = (StackPane) addBalanceRoot.lookup("#Background");

            // Configura o botão de confirmação
            confirmButton.setOnAction(e -> {
                try {
                    String amountText = amountField.getText();
                    if (amountText == null || amountText.trim().isEmpty()) {
                        AlertUtils.showAlert(addBalanceBackground, "Please, add the desired value to add." );
                        return;
                    }

                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        AlertUtils.showAlert(addBalanceBackground, "Please, insert a valid value, bigger than 0 (zero).");
                        return;
                    }

                    // Atualiza o saldo na tabela Wallet
                    if (updateWalletBalance(amount, event)) {
                        AlertUtils.showAlert(addBalanceBackground, String.format("%.2f was added to your wallet with success!", amount));
                        addBalanceStage.close(); // Fecha a janela após sucesso
                    } else {
                        AlertUtils.showAlert(addBalanceBackground, "Balance update failed.");
                    }

                } catch (NumberFormatException ex) {
                    AlertUtils.showAlert( addBalanceBackground,"Please, insert a valid value (ex: 5000.00)");
                }
            });

            // Configura o botão de cancelamento
            cancelButton.setOnAction(e -> addBalanceStage.close());

            // Exibe a janela e aguarda até que seja fechada
            addBalanceStage.showAndWait();

        } catch (IOException e) {
            // Aqui usamos o UserMenuPane para mostrar o alerta no menu principal
            AlertUtils.showAlert(Background, "It wasn't possible to load the deposit window.");
            e.printStackTrace();
        }
    }

    // Método para atualizar o saldo na tabela Wallet
    private boolean updateWalletBalance(double amount, ActionEvent event) {
        try {
            Client client = (Client) LoggedUser;
            Boolean Success = client.getWallet().SetBalance(client.getWallet().getBalance() + amount, client.getWallet().getPendingBalance());
            scene = ((Node) event.getSource()).getScene();
            root = scene.getRoot();
            Node node = root.lookup("#balanceLabel");

            if (node instanceof Label) {
                Label label = (Label) node;
                label.setText(String.format("%.2f", client.getWallet().getBalance()));
            }
            return Success; // Retorna verdadeiro se a atualização foi bem-sucedida
        }catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
        return false;

    }

    @FXML
    void ManageCoin() {
        GoTo("ManageCoin.fxml");
    }

    @FXML
    void TransactionHistory() {
        GoTo("OrderHistory.fxml");
    }

    @FXML
    void BalanceHistory() {
        GoTo("BalanceHistory.fxml");
    }

    // Função que manda após clicar no botão gerir users manda para a pagina certa
    @FXML
    void ManageUser() {
        GoTo("ManageUser.fxml");
    }
    @FXML
    void ActiveOrders() {
        GoTo("ActiveOrders.fxml");
    }

    private void GoTo(String View) {
        try {
            Main.setRoot(View, LoggedUser);
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
            e.printStackTrace();
        }
    }
}