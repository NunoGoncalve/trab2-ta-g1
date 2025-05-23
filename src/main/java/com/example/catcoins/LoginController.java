package com.example.catcoins;

import com.example.catcoins.DatabaseConnection;
import com.example.catcoins.User;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button LoginButton;
    @FXML private Button togglePasswordBtn;
    @FXML private Hyperlink esqueceuSenhaLink;

    @FXML
    private void initialize() {
        LoginButton.setOnAction(this::handlelogin);
    }

    public static class HashUtil {
        public static String sha256(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private User verificarUsuario(String email, String password, Event event) {
        String sql = "SELECT * FROM User WHERE email = ? AND password = ?";

        try {  //verificando hased
            PasswordUtils.hashPassword(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Role UserRole;
                Status UserStatus;

                if(rs.getString("role").equals("Client")) {
                    UserRole =  Role.Client;

                }
                else{UserRole = Role.Admin;}

                if(rs.getString("status").equals("Active")) {
                    UserStatus =  Status.Active;

                }
                else{UserStatus = Status.Disabled;}
                return new User (rs.getString("Name"), email, UserRole, UserStatus);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @FXML
    private void togglePasswordVisibility() {
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

    private void trocarCena(String fxmlPath, Event event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleEsqueceuSenha(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Recuperação de senha", "Por favor, contacte o suporte ou utilize o sistema de recuperação de senha.");
    }

    public void handlelogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.isVisible()
                ? passwordField.getText()
                : passwordVisibleField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Todos os campos são obrigatórios.");
            return;
        }

        if (!email.contains("@") || !(email.endsWith(".com") || email.endsWith(".pt"))) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Email inválido. Use um email válido como exemplo@dominio.com ou .pt");
            return;
        }

        User usuario = verificarUsuario(email, password, event);

        if (usuario != null) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Login realizado com sucesso!");
            limparCampos();

            try {
                trocarCena("paginaPrincipal.fxml", event); // vá para sua tela principal
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Dados incorretos. Confira seu email ou senha.");
            esqueceuSenhaLink.setVisible(true);
        }
    }

    @FXML
    private void GoRegister(ActionEvent event) {
        try {
            trocarCena("registo.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de registro.");
        }
    }

    private void limparCampos() {
        emailField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
