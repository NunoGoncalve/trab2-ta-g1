package com.example.catcoins;

// JavaFX imports
import com.example.catcoins.model.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
// Java standard imports
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;



public class RecoverPassword {

    private static String storedCode;
    private static long codeTimestamp; // armazena quando o código foi gerado


    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label errorLabelPassword;
    @FXML private Label errorLabelFields;
    @FXML private Label errorLabelEmail;
    @FXML private Button SendEmail;
    @FXML private StackPane Background;
    @FXML private HBox strengthIndicator; // Referência ao HBox do indicador de força

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

    private void BlockEditing(TextField Text) {
        Text.setEditable(false);
        Text.setDisable(true);
    }

    // Mostra a etapa de verificação e esconde as demais
    private void showVerificationCode() {
        codeField.setVisible(true);
        codeField.setManaged(true);
        BlockEditing(emailField);

        // Configurar botão (só texto)
        SendEmail.setText("Verify");
        SendEmail.setOnAction(this::VerifyCode);
    }

    // Mostra a Redefinir a senha e esconde as demais
    private void showNewPassword() {
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        togglePasswordBtn.setVisible(true);
        togglePasswordBtn.setManaged(true);
        passwordStrengthBar.setVisible(true);
        passwordStrengthBar.setManaged(true);
        strengthIndicator.setVisible(true);
        strengthIndicator.setManaged(true);
        BlockEditing(emailField);
        BlockEditing(codeField);

        // Configurar botão (só texto)
        SendEmail.setText("Trocar");
        SendEmail.setOnAction(this::changePassword);
    }

    //Envia um código de verificação para o email especificado
    private Boolean sendVerificationCode(String recipientEmail) {
        storedCode = generateVerificationCode();
        codeTimestamp = System.currentTimeMillis(); // Registra quando o código foi gerado

        String content = "Hello,\n\n"
                + "We received a request to change your password using this email address.\n"
                + "If you did not request this, please ignore this message.\n\n"
                + "Your verification code is: " + storedCode + "\n\n"
                + "Sincerely,\n"
                + "CatCoins Support Team",
                subject = "Verification Code - CatCoins";

        return EmailConfig.SendEmail(recipientEmail, content, subject);
    }

    //Gera um código de verificação aleatório de 6 dígitos
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Gera número entre 100000 e 999999
        return String.valueOf(code);
    }


     //Verifica se o código fornecido corresponde ao código armazenado para o email
    private boolean verifyCode(String code) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - codeTimestamp;
        final long EXPIRATION_TIME = 10 * 60 * 1000; // 10 minutos em milissegundos

        if (elapsed > EXPIRATION_TIME) {
            return false;
        }
        else return !code.equals(storedCode);
    }

    public void HandleEmail() {
        String email = emailField.getText();
        if(email.isEmpty()) {
            ToggleErroLabel(errorLabelEmail, false);
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            ToggleErroLabel(errorLabelEmail, true);
            errorLabelEmail.setText("Invalid email !");
        }else {
            ToggleErroLabel(errorLabelEmail, false);
        }
    }

     //Processa o envio de email e avança para a próxima etapa
    @FXML
    private void SendEmail() {
        UserDAO UsrDao = new UserDAO();
        String email = emailField.getText();

        try {
            if (email.isEmpty()) {
                ToggleErroLabel(errorLabelFields, true);
                errorLabelFields.setText("Required field!");
                ToggleErroLabel(errorLabelEmail, false);

                // Verificar se o email existe no banco de dados
            } else if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") && (!UsrDao.CheckIfEmail(email))) {
                AlertUtils.showAlert(Background, "This email isn't registered.\nRedirecting to register", () -> GoToScreen("registo.fxml"));
            }

            // Enviar o código de verificação para o email
            if (sendVerificationCode(email)) {
                AlertUtils.showAlert(Background, "Verification code sent to: \n" + email, this::showVerificationCode);
                System.out.println("COD: " + storedCode);
            } else {
                AlertUtils.showAlert(Background, "Try again");
            }
        }catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

     //Verifica o código inserido e avança para a próxima etapa
    @FXML
    private void VerifyCode(ActionEvent event) {
        String code = codeField.getText();

        if (code == null || code.trim().isEmpty()) {
            ToggleErroLabel(errorLabelFields, true);
            errorLabelFields.setText("Mandatory field!");
            ToggleErroLabel(errorLabelEmail, false);

        } else if (!verifyCode(code)) {
            AlertUtils.showAlert(Background, "Code invalid!");
        }
        else AlertUtils.showAlert(Background, "Code Verified.", this::showNewPassword);
    }


     //Navega para outra tela FXML
    private void GoToScreen(String fxmlPath) {
        try {
            Main.setRoot(fxmlPath, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "Unable to load screen: \n" + fxmlPath);
        }
    }


    @FXML
    private void GoLogin() {
        GoToScreen("Login.fxml");
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

    //Processa a alteração de senha
    @FXML
    public void changePassword(ActionEvent event) {
        String novaSenha = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();
        String email = emailField.getText();

        // Validação da senha
        String senhaErro = validatePassword(novaSenha);
        if (senhaErro != null) {
            AlertUtils.showAlert(Background, "Password error");
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            AlertUtils.showAlert(Background,  "Email not available.");
            return;
        }

        try {
            // Gera hash da senha
            String hashedSenha = PasswordUtils.hashPassword(novaSenha);

            // Comando SQL para atualizar senha do usuário pelo email
            String sql = "UPDATE User SET Password = ? WHERE Email = ?";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedSenha);
                stmt.setString(2, email);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    AlertUtils.showAlert(Background,"Password changed with sucess!", () -> GoToScreen("Login.fxml"));

                } else {
                    AlertUtils.showAlert(Background,  "Check your credentials.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            mensagemErro += "10 characters minimum!\n";
        } else {
            strength += 0.4;
        }

        if (password.equals(password.toLowerCase())) {
            mensagemErro += "At least 1 uppercase letter (A-Z)!\n";
        } else {
            strength += 0.2;
        }

        if (!password.matches(".*\\d.*")) {
            mensagemErro += "At least 1 number (0-9)!\n";
        } else {
            strength += 0.2;
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[\\]{};:'\",.<>/?].*")) {
            mensagemErro += "At least 1 special character (!@#$%^&* etc.)!\n";
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
        return "Check your credencials.";
        }
        if (password.length() < 10) {
            return "Check the requisites.";
        }
        if (password.equals(password.toLowerCase())) {
            return "Check the requisites.";
        }
        if (!password.matches(".*\\d.*")) {
            return "Check the requisites.";
        }
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[\\]{};:'\",.<>/?].*")) {
            return "Check the requisites.";
        }
        return null;
    }



}
