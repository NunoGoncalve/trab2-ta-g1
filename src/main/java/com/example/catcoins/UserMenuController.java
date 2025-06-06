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

    private Stage stage;
    private Scene scene;
    private Parent root;
    private User LoggedUser;

    @FXML private Button BalanceButton;
    @FXML private HBox UserMenu;
    @FXML private Button ManageCoinBttn;
    @FXML private Button ManageUserBttn;
    @FXML private Button TransactionHistoryBttn;
    @FXML private StackPane UserMenuPane; // Usando o ID correto do StackPane no FXML
    @FXML private TextField amountField;

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

            // Obtém o StackPane Background da janela de adição de saldo
            StackPane addBalanceBackground = (StackPane) addBalanceRoot.lookup("#Background");

            // Configura o botão de confirmação
            confirmButton.setOnAction(e -> {
                try {
                    String amountText = amountField.getText();
                    if (amountText == null || amountText.trim().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, insira o valor que deseja adicionar.", addBalanceBackground);
                        return;
                    }

                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, insira um valor válido maior que 0 (zero).", addBalanceBackground);
                        return;
                    }

                    // Atualiza o saldo na tabela Wallet
                    if (updateWalletBalance(amount, event)) {
                        showAlert(Alert.AlertType.INFORMATION, "Sucesso", String.format("Foi adicionado %.2f à sua carteira com sucesso!", amount), addBalanceBackground);
                        addBalanceStage.close(); // Fecha a janela após sucesso
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar o saldo na carteira.", addBalanceBackground);
                    }

                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, insira um valor válido. (ex: 5000.00)", addBalanceBackground);
                }
            });

            // Configura o botão de cancelamento
            cancelButton.setOnAction(e -> addBalanceStage.close());

            // Exibe a janela e aguarda até que seja fechada
            addBalanceStage.showAndWait();

        } catch (IOException e) {
            // Aqui usamos o UserMenuPane para mostrar o alerta no menu principal
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a janela de adição de saldo.", UserMenuPane);
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

    // Método para mostrar alerta personalizado (substituindo os alertas padrão do JavaFX)
    private void showAlert(Alert.AlertType type, String title, String message, StackPane container) {
        // Verifica se o container está disponível
        if (container == null) {
            // Se não estiver disponível, usa o método alternativo para obter o container
            try {
                Scene currentScene = BalanceButton.getScene();
                if (currentScene != null) {
                    Parent currentRoot = currentScene.getRoot();
                    if (currentRoot instanceof StackPane) {
                        container = (StackPane) currentRoot;
                    } else {
                        // Se não encontrar o container, cria um alerta padrão como fallback
                        Alert alert = new Alert(type);
                        alert.setTitle(title);
                        alert.setHeaderText(null);
                        alert.setContentText(message);
                        alert.showAndWait();
                        return;
                    }
                } else {
                    // Se não houver cena, cria um alerta padrão como fallback
                    Alert alert = new Alert(type);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                    return;
                }
            } catch (Exception e) {
                // Em caso de erro, usa o alerta padrão como fallback
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
                return;
            }
        }

        // Cria o conteúdo do diálogo (não em tela cheia)
        VBox dialog = new VBox(3);
        dialog.setSpacing(25);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: white;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(170);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white");

        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #FFA630; -fx-max-width: 50; -fx-border-radius: 10;");

        dialog.getChildren().addAll(messageLabel, okButton);

        // Cria um overlay semi-transparente
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);"); // 0.2 = 20% de opacidade

        // Adiciona o diálogo ao overlay e centraliza
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);

        // Adiciona o overlay ao StackPane raiz
        container.getChildren().add(overlay);

        // Remove o overlay quando OK é clicado
        StackPane finalContainer = container;
        okButton.setOnAction(e -> finalContainer.getChildren().remove(overlay));
        amountField.clear();
    }


    // Sobrecarga do método showAlert para usar o UserMenuPane como container padrão
    private void showAlert(Alert.AlertType type, String title, String message) {
        showAlert(type, title, message, UserMenuPane);
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
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a página solicitada.");
            e.printStackTrace();
        }
    }
}