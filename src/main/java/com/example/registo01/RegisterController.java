package com.example.registo01;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private Hyperlink termsLink;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabelPassword;
    @FXML
    private Label errorLabelEmail;

    @FXML
    private void initialize() {
        /*passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            CalculateStrength(newValue);
        });*/

        /*registerButton.setOnAction(event -> {
            try {
                handleRegister();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao registar utilizador.");
            }
        });*/
    }

    public void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean acceptedTerms = termsCheckBox.isSelected();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabelPassword.setVisible(true);
            errorLabelPassword.setManaged(true);
            errorLabelPassword.setText("Todos os campos obrigatórios !");
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabelEmail.setVisible(true);
            errorLabelEmail.setManaged(true);
            errorLabelEmail.setText("Email Inválido !");
            return;
        } else {
            errorLabelEmail.setVisible(false);
            errorLabelEmail.setManaged(false);
            errorLabelEmail.setText(null);
        }

        if (!acceptedTerms) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Deve aceitar os termos e condições.");
            return;
        }

        String salt = PasswordUtils.generateSalt();
        String PasswordHash = null;
        try {
            PasswordHash = PasswordUtils.hashPassword(password, salt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (guardarUtilizador(username, email, PasswordHash)) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Registo guardado com sucesso!");
            limparCampos();

            try {
                Registered(username, email);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao guardar os dados.");
        }
    }

    public void CalculateStrength() {
        String password = passwordField.getText().toString();
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0.0);
        } else {
            double strength = 0.0;

            /*boolean HasUpper = false;
            boolean HasDigit = false;
            boolean HasSpecial = false;
            boolean IsLongEnough = false;*/

            if (password.length() >= 10) {
                strength += 0.4;
                errorLabelPassword.setText("Mínimo de 10 caracteres !");
                //IsLongEnough = true;
            }
            else if (!password.equals(password.toLowerCase())) {
                strength += 0.2;
                errorLabelPassword.setText("Pelo menos 1 letra maiúscula (A-Z) ! ");
                //HasUpper = true;
            }
            else if (password.matches(".*\\d.*")) {
                strength += 0.2;
                errorLabelPassword.setText("Pelo menos 1 número (0-9) !");

                //HasDigit = true;
            }
            else if (password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[\\]{};:'\",.<>/?].*")) {
                strength += 0.2;

                errorLabelPassword.setText("Pelo menos 1 caractere especial (!@#$%^&* etc.) !");
                //HasSpecial = true;
            }

            passwordStrengthBar.setProgress(strength);

            if (strength < 1.0) {
                errorLabelPassword.setVisible(true);
                errorLabelPassword.setManaged(true);

                /*if (!IsLongEnough) {
                    errorLabelPassword.setText("Mínimo de 10 caracteres !");
                } else if (!HasSpecial) {
                    errorLabelPassword.setText("Pelo menos 1 caractere especial (!@#$%^&* etc.) !");
                } else if (!HasUpper) {
                    errorLabelPassword.setText("Pelo menos 1 letra maiúscula (A-Z) ! ");
                } else if (!HasDigit) {
                    errorLabelPassword.setText("Pelo menos 1 número (0-9) !");
                }*/
            } else {
                errorLabelPassword.setVisible(false);
                errorLabelPassword.setManaged(false);
            }
        }
    }

    private boolean guardarUtilizador(String username, String email, String password) {
        String sql = "INSERT INTO User (Name, Email, Password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate();
            sql = "INSERT INTO Wallet (balance) VALUES (0.00)";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

            sql = "INSERT INTO Client (ID, Wallet) VALUES (1,1)";// arranjar forma de buscar os ID dos objetos recem criados, se n der buscar o id pelo email/registo mais recente
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void limparCampos() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        passwordStrengthBar.setProgress(0);
        termsCheckBox.setSelected(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void Registered(String Username, String Email) throws IOException {
        Client ClientRegistred = new Client(Username, Email, Role.Client, Status.Active);
        Wallet ClientWallet = new Wallet(0.00, "$");
        ClientRegistred.SetWallet(ClientWallet);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("paginaPrincipal.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Bem-vindo");
        stage.show();
    }
}