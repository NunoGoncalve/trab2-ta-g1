package com.example.catcoins;

import com.example.catcoins.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

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

    private UserDAO UsrDao = new UserDAO();

    private User VerifyUser(String email, String password) {

        try{
            password = PasswordUtils.hashPassword(password);
            return UsrDao.Login(email, password);

        } catch (SQLException e) {
            AlertUtils.showAlert(Background, "There was an error while trying to login. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "There was an error while trying to login. Please try again.");
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

            User user = VerifyUser(email, password);
            if (user != null) {
                limparCampos();

                try {
                    trocarCena("Main.fxml", user);
                } catch (Exception e) {
                    AlertUtils.showAlert(Background, "It wasn't possible to load the requested page.");
                }
            }
            else{
                AlertUtils.showAlert(Background, "Verify the credentials entered");
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
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "It wasn't possible to load the password recovery page.");
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

    public void trocarCena(String fxmlPath, User loggedInUser) throws Exception {

        Main.setRoot(fxmlPath, loggedInUser);
    }

    @FXML
    private void GoRegister() {
        try {
            trocarCena("registo.fxml", null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Background, "It wasn't possible to load the register page.");
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