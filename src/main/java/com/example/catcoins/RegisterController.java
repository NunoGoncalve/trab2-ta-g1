package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.Status;
import com.example.catcoins.model.Wallet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabelPassword;
    @FXML
    private Label errorLabelEmail;
    @FXML
    private Label errorLabelFields;
    @FXML
    private Label errorLabelTerms;

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

    public void HandleEmail() {
        String email = emailField.getText();
        if(email.isEmpty()) {
            ToggleErroLabel(errorLabelEmail, false);
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            ToggleErroLabel(errorLabelEmail, true);
            errorLabelEmail.setText("Email Inválido !");
        }else {
            ToggleErroLabel(errorLabelEmail, false);
        }

    }

    public void HandleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean acceptedTerms = termsCheckBox.isSelected();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            ToggleErroLabel(errorLabelFields, true);
            errorLabelFields.setText("Todos os campos obrigatórios !");
        } else if (!acceptedTerms) {
            ToggleErroLabel(errorLabelTerms, true);
            errorLabelTerms.setText("Deve aceitar os termos e condições !");
        }else if (passwordStrengthBar.getProgress() == 1 && !errorLabelEmail.isVisible()) {
            String salt = PasswordUtils.generateSalt();
            String PasswordHash = null;

            try {
                PasswordHash = PasswordUtils.hashPassword(password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String NewIDs = NewUser(username, email, PasswordHash);
            String[] partes = NewIDs.split(";");

            int UserID = Integer.parseInt(partes[0]);
            int WalletID = Integer.parseInt(partes[1]);

            if (UserID!=0) {
                ShowAlert(Alert.AlertType.INFORMATION, "Sucesso", "Registo guardado com sucesso!");
                limparCampos();

                try {
                    Registered(UserID ,WalletID, username, email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ShowAlert(Alert.AlertType.ERROR, "Erro", "Falha ao guardar os dados.");
            }
        }else{
            ToggleErroLabel(errorLabelEmail, false);
            ToggleErroLabel(errorLabelFields, false);
            ToggleErroLabel(errorLabelTerms, false);
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


    private String NewUser(String username, String email, String password) {
        String sql = "INSERT INTO User (Name, Email, Password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate();
            ResultSet result = stmt.getGeneratedKeys();
            result.next();
            int UserID = result.getInt(1);

            sql = "INSERT INTO Wallet (balance) VALUES (0.00)";
            stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            result = stmt.getGeneratedKeys();
            result.next();
            int Wallet = result.getInt(1);


            sql = "INSERT INTO Client (ID, Wallet) VALUES (?,?)";// arranjar forma de buscar os ID dos objetos recem criados, se n der buscar o id pelo email/registo mais recente
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, UserID);
            stmt.setInt(2, Wallet);
            stmt.executeUpdate();

            return UserID+";"+Wallet;

        } catch (SQLException e) {
            e.printStackTrace();
            return "0";
        }
    }

    protected void Registered(int WalletID, int UserID, String Username, String Email) throws IOException {
        Wallet ClientWallet = new Wallet(WalletID,0.00, "USD $");
        Client ClientRegistred = new Client(UserID, Username, Email, Role.Client, Status.Active, ClientWallet);
        Main.setRoot("Main.fxml", ClientRegistred);
    }

    private void limparCampos() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        passwordStrengthBar.setProgress(0);
        termsCheckBox.setSelected(false);
    }

    private void ShowAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void GoLogin(ActionEvent event) {
        try {
            Main.setRoot("Login.fxml", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}