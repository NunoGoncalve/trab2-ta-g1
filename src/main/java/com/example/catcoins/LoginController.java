package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {


    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private Hyperlink esqueceuSenhaLink;
    @FXML
    private Label errorLabelEmail;
    @FXML
    private Label errorLabelFields;
    @FXML
    private Label errorLabelEnd;
    @FXML
    private StackPane Background;


    private User verificarUsuario(String email, String password) {
        try {
            password = PasswordUtils.hashPassword(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try{
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM User WHERE email = ? AND password = ? and Status = 'Active'";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet UserResult = stmt.executeQuery();

            if (UserResult.next()) {

                if(UserResult.getString("Role").equals("Client")) {
                    sql = "SELECT Wallet.* FROM Client inner join Wallet on Client.Wallet = Wallet.ID WHERE Client.ID = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, UserResult.getInt("ID"));

                    ResultSet Walletresult = stmt.executeQuery();
                    if(Walletresult.next()){
                        Wallet ClientWallet = new Wallet(
                                Walletresult.getInt("ID"),
                                Walletresult.getDouble("Balance"),
                                Walletresult.getDouble("PendingBalance"),
                                Walletresult.getString("Currency"));
                        Client LoggedClient = new Client(Integer.parseInt(UserResult.getString("ID")), UserResult.getString("Name"), email, Role.Client, Status.Active, ClientWallet);
                        return LoggedClient;
                    }
                }
                else{
                    User LoggedAdmin = new User(Integer.parseInt(UserResult.getString("ID")), UserResult.getString("Name"), email, Role.Admin, Status.Active);
                    return LoggedAdmin;
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void handlelogin() {
        String email = emailField.getText();
        String password = passwordField.isVisible()
                ? passwordField.getText()
                : passwordVisibleField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            ToggleErroLabel(errorLabelFields, true);
            errorLabelFields.setText("All fields are required!");
            esqueceuSenhaLink.setVisible(false);
            esqueceuSenhaLink.setManaged(false);
            ToggleErroLabel(errorLabelEmail, false);


        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            ToggleErroLabel(errorLabelEmail, true);
            errorLabelEmail.setText("Invalid email!");

        }else {
            ToggleErroLabel(errorLabelEmail, false);
            User usuario = verificarUsuario(email, password);

            if (usuario != null) {
                limparCampos();

                try {
                    trocarCena("Main.fxml", usuario);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "Verify the credentials entered");
                esqueceuSenhaLink.setVisible(true);
               esqueceuSenhaLink.setManaged(true);
            }
        }

    }

    public void HandleEmail() {
        String email = emailField.getText();
        if (email.isEmpty()) {
            ToggleErroLabel(errorLabelEmail, false);
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            ToggleErroLabel(errorLabelEmail, true);
            errorLabelEmail.setText("Invalid email!");
            esqueceuSenhaLink.setVisible(false);
            esqueceuSenhaLink.setManaged(false);
        } else {
            ToggleErroLabel(errorLabelEmail, false);
        }
    }

    @FXML
    private void handleEsqueceuSenha() {
        try {
            Main.setRoot("RecoverPassword.fxml", null);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "It wasn't possible to load the password recovery page.");
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

    public void trocarCena(String fxmlPath, User loggedInUser) throws IOException {
        Main.setRoot(fxmlPath, loggedInUser);
    }

    @FXML
    private void GoRegister() {
        try {
            trocarCena("registo.fxml", null);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, Alert.AlertType.ERROR, "ERROR", "It wasn't possible to load the register page.");
        }
    }

    private void limparCampos() {
        emailField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
    }

    @FXML
    private void handleEnter(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER){
            handlelogin();
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
}