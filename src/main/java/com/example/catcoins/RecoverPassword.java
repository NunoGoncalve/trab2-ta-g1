package com.example.catcoins;

// JavaFX imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// JavaMail imports (versão clássica)
import javax.mail.*;
import javax.mail.internet.*;

// Java standard imports
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;

public class RecoverPassword {

    private static String storedCode;
    private static String storedEmail;
    private static long codeTimestamp; // armazena quando o código foi gerado


    @FXML
    private TextField emailField;
    @FXML
    private TextField codigoField;
    @FXML
    private StackPane Background;

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private Label errorLabelPassword;


     /* Envia um código de verificação para o email especificado*/
    private Boolean sendVerificationCode(String recipientEmail) {
        storedCode = generateVerificationCode();

        String content = "Olá,\n\n"
                + "Recebemos uma solicitação para alteração de senha através deste endereço de e-mail.\n"
                + "Se você não solicitou isso, ignore esta mensagem.\n\n"
                + "Seu código de verificação é: " + storedCode + "\n\n"
                + "Atenciosamente,\n"
                + "Equipe de Suporte CatCoins",
                subject = "Código de Verificação - CatCoins";

        return EmailConfig.SendEmail(recipientEmail, content, subject);



    }


     /* Gera um código de verificação aleatório de 6 dígitos*/
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Gera número entre 100000 e 999999
        return String.valueOf(code);
    }


     /* Verifica se o código fornecido corresponde ao código armazenado para o email*/
     private boolean verifyCode(String email, String code) {
         long currentTime = System.currentTimeMillis();
         long elapsed = currentTime - codeTimestamp;
         final long EXPIRATION_TIME = 10 * 60 * 1000; // 10 minutos em milissegundos

         if (!code.equals(storedCode)) {
             return false;
         }



         return true;
     }


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

        // Enviar o código de verificação para o email

        if (sendVerificationCode(email)) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Código de verificação enviado para: " + email, () -> GoToVerificationScreen(event));
            // aparecer campo codigo
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível enviar o código de verificação. Verifique sua conexão com a internet ou tente novamente mais tarde.");
        }
    }

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

        showAlert(Alert.AlertType.INFORMATION, "Código Verificado", "O código inserido foi aceito.");
        GoPassNewPassword(event);
    }

    private void GoToScreen(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (fxmlPath.equals("PassVerificationCode.fxml")) {
                RecoverPassword controller = loader.getController();
                if (controller.emailField != null) {
                    controller.emailField.setText(emailField.getText());
                }
            }

            if (fxmlPath.equals("PassNewPassword.fxml")) {
                RecoverPassword controller = loader.getController();
                if (controller.emailField != null) {
                    controller.emailField.setText(emailField.getText());
                }
                if (controller.codigoField != null) {
                    controller.codigoField.setText(codigoField.getText());
                }
            }

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

    @FXML
    private void GoPassSendEmail(ActionEvent event) {
        GoToScreen("PassSendEmail.fxml", event);
    }

    @FXML
    private void GoPassNewPassword(ActionEvent event) {
        GoToScreen("PassNewPassword.fxml", event);
    }

    @FXML
    private void GoPassVerificationCode(ActionEvent event) {
        GoToScreen("PassVerificationCode.fxml", event);
    }

    private void GoToVerificationScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PassVerificationCode.fxml"));
            Parent root = loader.load();

            RecoverPassword controller = loader.getController();
            if (controller.emailField != null) {
                controller.emailField.setText(emailField.getText());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de verificação.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        showAlert(type, title, message, null);
    }

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

    // Função para gerar hash SHA-256
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

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

    public void CalculateStrength() {
        String password = passwordField.getText();

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0.0);
            ToggleErroLabel(errorLabelPassword, false);
            //return;
        }

        double strength = 0.0;
        String mensagemErro = "";

        if (password.length() < 10) {
            //strength += 0.0;
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
