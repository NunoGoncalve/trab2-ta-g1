package com.example.catcoins;

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

    @FXML private HBox UserMenu;
    @FXML private Button ManageCoinBttn;

    @FXML
    private Button BalanceButton;

    public void setUser(User user) {
        this.LoggedUser = user;
        if(user.getRole()==Role.Admin) {
            ManageCoinBttn.setVisible(true);
            ManageCoinBttn.setManaged(true);
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

//    @FXML
//    void AddBalance(ActionEvent event) {
//         double balance = 0.0; // Variável para armazenar o saldo
//
//
//
////        try {
////            root = FXMLLoader.load(getClass().getResource("UserMenu.fxml"));
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
////        scene = new Scene(root);
////        stage.setScene(scene);
////        stage.show();
//
//    }
    @FXML
    void AddBalance(ActionEvent event) {
        try {
            // Carrega a janela de adição de saldo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddBalance.fxml"));
            DialogPane addBalancePane = loader.load();

            // Obtém os componentes
            TextField amountField = (TextField) addBalancePane.lookup("#amountField");
            Label errorLabel = (Label) addBalancePane.lookup("#errorLabel");

            // Configura a caixa de diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(addBalancePane);
            dialog.setTitle("Adicionar Saldo");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node)event.getSource()).getScene().getWindow());

            // Processa o resultado
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    try {
                        double amount = Double.parseDouble(amountField.getText());
                        if (amount <= 0) {
                            throw new NumberFormatException();
                        }

                        // Atualiza o saldo na tabela Wallet
                        if (updateWalletBalance(amount)) {
                            showSuccessMessage(amount);
                        } else {
                            showAlert("Erro", "Falha ao atualizar o saldo na carteira.");
                        }

                    } catch (NumberFormatException e) {
                        errorLabel.setText("Por favor, insira um valor válido (ex: 50.00)");
                        errorLabel.setVisible(true);
                        AddBalance(event); // Reabre a janela
                    }
                }
            });

        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a janela de adição de saldo.");
            e.printStackTrace();
        }
    }

    // Método para atualizar o saldo na tabela Wallet
    private boolean updateWalletBalance(double amount) {
        Client client = (Client) LoggedUser;
        // Atualiza o saldo na tabela Wallet para o ID especificado
        String sql = "UPDATE Wallet SET balance = balance + ? WHERE ID = ?"; // Adiciona o ID como parâmetro

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDouble(1, amount); // Define o valor a ser adicionado ao saldo
                stmt.setInt(2, LoggedUser.getId());
                client.getWallet().SetBalance(client.getWallet().getBalance() + amount);

            int rowsAffected = stmt.executeUpdate(); // Executa a atualização
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

    private void GoTo(String View) {
        try {
            Main.setRoot(View, LoggedUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}