package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.Status;
import com.example.catcoins.model.Wallet;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
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
    private Label errorLabelPassword;
    @FXML
    private Label errorLabelEmail;
    @FXML
    private Label errorLabelFields;
    @FXML
    private Label errorLabelTerms;
    @FXML
    private StackPane Background;

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
            errorLabelEmail.setText("Invalid Email!");
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
            errorLabelFields.setText("All fields are required!");
        } else if (!acceptedTerms) {
            ToggleErroLabel(errorLabelTerms, true);
            errorLabelTerms.setText("You must accept the terms and conditions!");
            // Verifica se o e-mail já está registrado
        } else if (CheckIfEmail(email)) {
            if (CheckIfEmail(email)) {
                //showAlert(Alert.AlertType.INFORMATION, "Email já registrado", "Este email já está registrado.\nVá para o Login", () -> GoLogin());  // redireciona só depois do OK
                AlertUtils.showAlert(Background,"This email is already linked to an account", () -> GoLogin());
            }

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
                AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "Success", "Record saved successfully!");
                limparCampos();

                try {
                    Registered(WalletID,UserID , username, email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Failed to save data");
            }
        }else{
            ToggleErroLabel(errorLabelEmail, false);
            ToggleErroLabel(errorLabelFields, false);
            ToggleErroLabel(errorLabelTerms, false);
        }

    }

    private void showAlert() {
        AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Verify the credentials entered");

    }

    //Verifica se o email existe no banco de dados
    private boolean CheckIfEmail(String email) {
        String query = "SELECT 1 FROM User WHERE Email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            return stmt.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Error verifying email.");
            return false;
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
            mensagemErro += "Minimum 10 characters!\n";
        } else {
            strength += 0.4;
        }

        if (password.equals(password.toLowerCase())) {
            mensagemErro += "At least 1 capital letter (A-Z)!\n";
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
        Wallet ClientWallet = new Wallet(WalletID,0.00, 0.00,"EUR €");
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


    @FXML
    private void GoLogin() {
        GoToScreen("Login.fxml");
    }


    private void GoToScreen(String fxmlPath) {
        try {
            Main.setRoot(fxmlPath, null);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Unable to load screen: \n" + fxmlPath);
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

    @FXML
    public void TermsConditions(){
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://foodsorter.fixstuff.net/CatCoins/Terms&Conditions.pdf"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop is not supported!");
        }
    }

}