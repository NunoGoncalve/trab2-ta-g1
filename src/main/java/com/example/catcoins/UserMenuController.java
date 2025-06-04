package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.User;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.scene.control.*;
import javafx.stage.Modality;

public class UserMenuController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private User LoggedUser;

    @FXML private Button BalanceButton;
    @FXML private HBox UserMenu;
    @FXML private Button ManageCoinBttn;
    @FXML private Button ManageUserBttn;
    @FXML private Button TransactionHistoryBttn;

    public void setUser(User user) {
        this.LoggedUser = user;
        if(user.getRole()== Role.Admin) {
            ManageCoinBttn.setVisible(true);
            ManageCoinBttn.setManaged(true);
            ManageUserBttn.setVisible(true);
            ManageUserBttn.setManaged(true);
            BalanceButton.setVisible(false);
            BalanceButton.setManaged(false);
            TransactionHistoryBttn.setVisible(false);
            TransactionHistoryBttn.setManaged(false);

        }
    }

    @FXML
    void Open() {

        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), UserMenu);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        UserMenu.setVisible(true);
    }

    @FXML
    void Close() {
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(500), UserMenu);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        CloseTransition.setOnFinished(event -> {
            UserMenu.setVisible(false);
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

            // Configura o botão de confirmação
            confirmButton.setOnAction(e -> {
                try {
                    String amountText = amountField.getText();
                    if (amountText == null || amountText.trim().isEmpty()) {
                        showAlert("Erro", "Por favor, insira o valor que deseja adicionar.");
                        return;
                    }

                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        showAlert("Erro", "Por favor, insira um valor válido maior que 0 (zero).");
                        return;
                    }

                    // Atualiza o saldo na tabela Wallet
                    if (updateWalletBalance(amount, event)) {
                        showSuccessMessage(amount);
                        addBalanceStage.close(); // Fecha a janela após sucesso
                    } else {
                        showAlert("Erro", "Falha ao atualizar o saldo na carteira.");
                    }

                } catch (NumberFormatException ex) {
                    showAlert("Erro", "Por favor, insira um valor válido. (ex: 5000.00)");
                }
            });

            // Configura o botão de cancelamento
            cancelButton.setOnAction(e -> addBalanceStage.close());

            // Exibe a janela e aguarda até que seja fechada
            addBalanceStage.showAndWait();

        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a janela de adição de saldo.");
            e.printStackTrace();
        }
    }

    // Método para atualizar o saldo na tabela Wallet
    private boolean updateWalletBalance(double amount, ActionEvent event) {
        Client client = (Client) LoggedUser;
        // Atualiza o saldo na tabela Wallet para o ID especificado
        String sql = "UPDATE Wallet SET balance = balance + ? WHERE ID = ?"; // Adiciona o ID como parâmetro

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amount); // Define o valor a ser adicionado ao saldo
            stmt.setInt(2, ((Client) LoggedUser).getWallet().getID()); // Substitua 1 pelo ID correto se necessário
            client.getWallet().SetBalance(client.getWallet().getBalance() + amount);

            int rowsAffected = stmt.executeUpdate(); // Executa a atualização
            scene = ((Node) event.getSource()).getScene();
            root = scene.getRoot();
            Node node = root.lookup("#balanceLabel");
            if (node instanceof Label) {
                Label label = (Label) node;
                label.setText(String.format("%.2f", client.getWallet().getBalance()));
            }
            return rowsAffected > 0; // Retorna verdadeiro se a atualização foi bem-sucedida

        } catch (SQLException e) {
            e.printStackTrace(); // Imprime o erro para depuração
            return false; // Retorna falso em caso de erro
        }
    }

    // Método para mostrar mensagem de sucesso
    private void showSuccessMessage(double amount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Foi adicionado %.2f à sua carteira com sucesso!", amount));
        alert.showAndWait();
    }

    // Método para mostrar alerta de erro
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void ManageCoin() {
        GoTo("ManageCoin.fxml");

    }

    @FXML
    void TransactionHistory() {
        GoTo("TransactionHistory.fxml");
    }

    // Função que manda após clicar no botão gerir users manda para a pagina certa
    @FXML
    void ManageUser() {
        GoTo("ManageUser.fxml");
    }

    private void GoTo(String View) {
        try {
            Main.setRoot(View, LoggedUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}