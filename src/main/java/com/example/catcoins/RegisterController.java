package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

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

    private UserDAO UsrDao = new UserDAO();

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

        try {
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                ToggleErroLabel(errorLabelFields, true);
                errorLabelFields.setText("All fields are required!");
            } else if (!acceptedTerms) {
                ToggleErroLabel(errorLabelTerms, true);
                errorLabelTerms.setText("You must accept the terms and conditions!");
                // Verifica se o e-mail já está registrado
            } else if (UsrDao.CheckIfEmail(email)) {
                AlertUtils.showAlert(Background, "This email is already linked to an account", () -> GoLogin()); // redireciona só depois do OK

            } else if (passwordStrengthBar.getProgress() == 1 && !errorLabelEmail.isVisible()) {
                String PasswordHash = null;
                PasswordHash = PasswordUtils.hashPassword(password);


                String NewIDs = NewUser(username, email, PasswordHash);
                String[] partes = NewIDs.split(";");

                int UserID = Integer.parseInt(partes[0]);
                int WalletID = Integer.parseInt(partes[1]);

                if (UserID != 0) {
                    AlertUtils.showAlert(Background, "Record saved successfully!");
                    limparCampos();

                    Registered(WalletID, UserID, username, email, PasswordHash);

                } else {
                    AlertUtils.showAlert(Background, "Failed to save data");
                }
            } else {
                ToggleErroLabel(errorLabelEmail, false);
                ToggleErroLabel(errorLabelFields, false);
                ToggleErroLabel(errorLabelTerms, false);
            }
        }catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }catch (IOException e) {
            AlertUtils.showAlert(Background, "Error");
            e.printStackTrace();
        }catch (Exception e) {
            AlertUtils.showAlert(Background, "Error");
            e.printStackTrace();
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
        String UserSql = "INSERT INTO User (Name, Email, Password) VALUES (?, ?, ?)",
                WalletSql  = "INSERT INTO Wallet (balance) VALUES (0.00)",
                ClientSql = "INSERT INTO Client (ID, Wallet) VALUES (?, ?)";
        int UserID, WalletID;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(UserSql, Statement.RETURN_GENERATED_KEYS)){
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.executeUpdate();
                ResultSet result = stmt.getGeneratedKeys();
                if (result.next()) UserID = result.getInt(1);
                else throw new SQLException("No user ID generated");

            }

            try (PreparedStatement  stmt = conn.prepareStatement(WalletSql,Statement.RETURN_GENERATED_KEYS)){
                stmt.executeUpdate();
                ResultSet result = stmt.getGeneratedKeys();
                if (result.next()) WalletID = result.getInt(1);
                else throw new SQLException("No wallet ID generated");
            }

            try (PreparedStatement  stmt = conn.prepareStatement(ClientSql)) {
                stmt.setInt(1, UserID);
                stmt.setInt(2, WalletID);
                stmt.executeUpdate();
            }
            conn.commit();
            return UserID+";"+WalletID;
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
            return "0";
        }

    }

    private void Registered(int WalletID, int UserID, String Username, String Email, String Password) throws Exception {
        Wallet ClientWallet = new Wallet(WalletID,0.00, 0.00,"EUR €");
        Client ClientRegistered = new Client(UserID, Username, Email, Password, Role.Client, Status.Active, ClientWallet);
        Main.setRoot("Main.fxml", ClientRegistered);
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
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "Unable to load screen: \n" + fxmlPath);
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
        try {
            Desktop.getDesktop().browse(new URI("http://foodsorter.fixstuff.net/CatCoins/Terms&Conditions.pdf"));
        } catch (Exception e) {
            AlertUtils.showAlert(Background,  "Sorry there was an error");
            e.printStackTrace();
        }

    }

}