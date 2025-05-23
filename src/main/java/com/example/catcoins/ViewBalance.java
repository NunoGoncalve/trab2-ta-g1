package com.example.catcoins;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.example.catcoins.DatabaseConnection;

import java.sql.*;

public class ViewBalance {

    @FXML
    private Label balanceLabel;
    @FXML
    private Label usernameLabel;

    private final int USER_ID = 1;

    @FXML
    public void initialize() {
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        String sql = "SELECT Name, Balance FROM User INNER JOIN Client ON User.ID = Client.ID  INNER JOIN Wallet ON Client.Wallet = Wallet.ID WHERE User.ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1,1);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String Name = rs.getString("Name");
                double Balance = rs.getDouble("Balance");

                usernameLabel.setText(Name);

                if (Balance <= 0) {
                    balanceLabel.setText("0.00€");
                    //  mostrar uma mensagem em outro label, alerta, ou até trocar o texto do usernameLabel
                    //  usar o usernameLabel para mostrar a mensagem:
                    usernameLabel.setText(Name + " - Sem saldo disponível");
                } else {
                    balanceLabel.setText(String.format("%.2f€", Balance));
                }

            } else {
                usernameLabel.setText("Desconhecido");
                balanceLabel.setText("0.00€");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            usernameLabel.setText("Erro");
            balanceLabel.setText("Erro");
        }
    }

}
