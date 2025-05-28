package com.example.catcoins;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// JavaMail imports (versão clássica)
import javax.mail.*;
import javax.mail.internet.*;

// Java standard imports
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;


public class RecoverPassword implements Initializable {

    private static String storedCode;
    private static String storedEmail;
    private static long codeTimestamp; // armazena quando o código foi gerado


    @FXML private TextField emailField;
    @FXML private TextField codigoField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label errorLabelPassword;
    @FXML private Button SendEmail;
    @FXML private StackPane Background;
    @FXML private HBox strengthIndicator; // Referência ao HBox do indicador de força


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showSendEmailStep();
    }

    public void ToggleField(TextField Field, Boolean IsVisible) {
        if (IsVisible) {
            Field.setVisible(true);
            Field.setManaged(true);

        } else {
            Field.setVisible(false);
            Field.setManaged(false);
            Field.setText(null);
        }
    }

    public void ToggleBn(Button button, Boolean IsVisible) {
        if (IsVisible) {
            button.setVisible(true);
            button.setManaged(true);

        } else {
            button.setVisible(false);
            button.setManaged(false);
            button.setText(null);
        }
    }

    public void ToggleBar(ProgressBar bar, Boolean IsVisible) {
        if (IsVisible) {
            bar.setVisible(true);
            bar.setManaged(true);

        } else {
            bar.setVisible(false);
            bar.setManaged(false);
        }
    }

    public void ToggleErroLabel(Label errorLabel, Boolean IsVisible) {
        if (IsVisible) {
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

        } else {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            errorLabel.setText(null);
        }
    }

    public void ToggleEstrengthIndicator(HBox strengthIndicator, Boolean IsVisible) {
        if (IsVisible) {
            strengthIndicator.setVisible(true);
            strengthIndicator.setManaged(true);

        } else {
            strengthIndicator.setVisible(false);
            strengthIndicator.setManaged(false);
        }
    }

    private void BlockEditing(TextField Text) {
        Text.setEditable(false);
        Text.setDisable(true);
    }

    // Mostra a etapa de envio de email e esconde as demais
    private void showSendEmailStep() {

        // Configurar botão (só texto)
        SendEmail.setText("Enviar");
        SendEmail.setOnAction(this::SendEmail);
    }

    // Mostra a etapa de verificação e esconde as demais
    private void showVerificationCode() {

        ToggleField(codigoField, true);
        BlockEditing(emailField);

        // Configurar botão (só texto)
        SendEmail.setText("Confirmar");
        SendEmail.setOnAction(this::VerifyCode);
    }

    // Mostra a Redefinir a senha e esconde as demais
    private void showNewPassword() {
        ToggleField(passwordField, true);
        ToggleBn(togglePasswordBtn, true);
        ToggleBar(passwordStrengthBar, true);
        ToggleEstrengthIndicator(strengthIndicator, true);
        BlockEditing(emailField);
        BlockEditing(codigoField);

        // Configurar botão (só texto)
        SendEmail.setText("Trocar");
        SendEmail.setOnAction(this::changePassword);
    }

    //Envia um código de verificação para o email especificado
    private Boolean sendVerificationCode(String recipientEmail) {
        storedCode = generateVerificationCode();
        codeTimestamp = System.currentTimeMillis(); // Registra quando o código foi gerado

        String content = "Olá,\n\n"
                + "Recebemos uma solicitação para alteração de senha através deste endereço de e-mail.\n"
                + "Se você não solicitou isso, ignore esta mensagem.\n\n"
                + "Seu código de verificação é: " + storedCode + "\n\n"
                + "Atenciosamente,\n"
                + "Equipe de Suporte CatCoins",
                subject = "Código de Verificação - CatCoins";

        return EmailConfig.SendEmail(recipientEmail, content, subject);
    }

    //Gera um código de verificação aleatório de 6 dígitos
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Gera número entre 100000 e 999999
        return String.valueOf(code);
    }


     //Verifica se o código fornecido corresponde ao código armazenado para o email
    private boolean verifyCode(String email, String code) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - codeTimestamp;
        final long EXPIRATION_TIME = 10 * 60 * 1000; // 10 minutos em milissegundos

        if (!code.equals(storedCode)) {
            return false;
        }

        return true;
    }


     //Processa o envio de email e avança para a próxima etapa
    @FXML
    private void SendEmail(ActionEvent event) {
        String email = emailField.getText();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "O campo de email não pode estar vazio.");
            return;
        }

        if (!email.contains("@") || !(email.endsWith(".com") || email.endsWith(".pt"))) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Email inválido. Use um email válido como exemplo@dominio.com ou .pt");
            return;
        }

        // Verificar se o email existe no banco de dados
        if (!CheckIfEmail(email)) {
            showAlert(Alert.AlertType.INFORMATION, "Email não encontrado", "Este email não está registrado.", () -> GoToScreen("registo.fxml", event));
            return;
        }

        // Enviar o código de verificação para o email
        if (sendVerificationCode(email)) {
            storedEmail = email; // Armazena o email para uso posterior
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Código de verificação enviado para: " + email, this::showVerificationCode);
            System.out.println("COD: " + storedCode);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível enviar o código de verificação. Verifique sua conexão com a internet ou tente novamente mais tarde.");
        }
    }


     //Verifica se o email existe no banco de dados
    private boolean CheckIfEmail(String email) {
        String query = "SELECT 1 FROM User WHERE Email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            return stmt.executeQuery().next(); // Retorna true se encontrou o email

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao verificar o email.");
            return false;
        }
    }


     //Verifica o código inserido e avança para a próxima etapa
    @FXML
    private void VerifyCode(ActionEvent event) {
        String code = codigoField.getText();
        String email = emailField.getText();

        if (code == null || code.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "O campo de código não pode estar vazio.");
            return;
        }

        // Verificar o código usando o método verifyCode
        if (!verifyCode(email, code)) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Código incorreto ou expirado. Solicite um novo código.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Código Verificado", "O código inserido foi aceito.", this::showNewPassword);
    }


     //Navega para outra tela FXML
    private void GoToScreen(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela: " + fxmlPath);
        }
    }


    @FXML
    private void GoLogin(ActionEvent event) {
        GoToScreen("Login.fxml", event);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        showAlert(type, title, message, null);
    }


     //Exibe um alerta personalizado com callback
    private void showAlert(Alert.AlertType type, String title, String message, Runnable onClose) {
        VBox dialog = new VBox(15);
        dialog.setAlignment(Pos.CENTER);
        dialog.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-radius: 10; -fx-background-radius: 10;");
        dialog.setMaxWidth(320);
        dialog.setMaxHeight(300);
        Label messageLabel = new Label(message);
        Button okButton = new Button("OK");
        dialog.getChildren().addAll(messageLabel, okButton);

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        overlay.getChildren().add(dialog);
        overlay.setAlignment(Pos.CENTER);

        Background.getChildren().add(overlay);

        okButton.setOnAction(e -> {
            Background.getChildren().remove(overlay);
            if (onClose != null) {
                onClose.run();
            }
        });
    }


    //Alterna a visibilidade do campo de senha
    @FXML
    public void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
    }


    //Gera hash SHA-256 da senha
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }


    //Processa a alteração de senha
    @FXML
    public void changePassword(ActionEvent event) {
        String novaSenha = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();
        String email = emailField.getText();

        // Validação da senha
        String senhaErro = validatePassword(novaSenha);
        if (senhaErro != null) {
            showAlert(Alert.AlertType.ERROR, "Erro na senha", senhaErro);
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Email do usuário não está disponível.");
            return;
        }

        try {
            // Gera hash da senha
            String hashedSenha = hashPassword(novaSenha);

            // Comando SQL para atualizar senha do usuário pelo email
            String sql = "UPDATE User SET Password = ? WHERE Email = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedSenha);
                stmt.setString(2, email);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Senha alterada com sucesso!", () -> {
                        GoToScreen("Login.fxml", event);
                    });
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro", "Usuário não encontrado ou senha não alterada.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao alterar a senha.");
        }
    }


     //Calcula e exibe a força da senha
    @FXML
    public void CalculateStrength() {
        String password = passwordField.getText();

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0.0);
            ToggleErroLabel(errorLabelPassword, false);
            return;
        }

        double strength = 0.0;
        String mensagemErro = "";

        if (password.length() < 10) {
            mensagemErro += "Mínimo de 10 caracteres!\n";
        } else {
            strength += 0.4;
        }

        if (password.equals(password.toLowerCase())) {
            mensagemErro += "Pelo menos 1 letra maiúscula (A-Z)!\n";
        } else {
            strength += 0.2;
        }

        if (!password.matches(".*\\d.*")) {
            mensagemErro += "Pelo menos 1 número (0-9)!\n";
        } else {
            strength += 0.2;
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[\\]{};:'\",.<>/?].*")) {
            mensagemErro += "Pelo menos 1 caractere especial (!@#$%^&* etc.)!\n";
        } else {
            strength += 0.2;
        }

        passwordStrengthBar.setProgress(strength);

        if (strength < 1.0) {
            errorLabelPassword.setText(mensagemErro.trim());
            ToggleErroLabel(errorLabelPassword, true);
        } else {
            ToggleErroLabel(errorLabelPassword, false);
        }
    }

    //Valida os requisitos da senha
    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "A senha não pode ser vazia.";
        }
        if (password.length() < 10) {
            return "A senha deve ter pelo menos 10 caracteres.";
        }
        if (password.equals(password.toLowerCase())) {
            return "A senha deve conter pelo menos uma letra maiúscula.";
        }
        if (!password.matches(".*\\d.*")) {
            return "A senha deve conter pelo menos um número.";
        }
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[\\]{};:'\",.<>/?].*")) {
            return "A senha deve conter pelo menos um caractere especial.";
        }
        return null;
    }
}
