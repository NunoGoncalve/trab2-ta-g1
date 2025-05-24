package com.example.catcoins;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.sql.*;

public class ViewBalanceController {

    @FXML
    private Label balanceLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private HBox UserMenu;

    @FXML
    private StackPane UserMenuPane;

    @FXML
    private ImageView MenuBttn;

    public void setUser(User LoggedUser) {
        if (LoggedUser instanceof Client) {
            String sql = "SELECT Balance FROM Client INNER JOIN Wallet ON Client.Wallet = Wallet.ID WHERE Client.ID = ?";

            try (Connection conn = DatabaseConnection.getConnection()){
                 PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setInt(1,LoggedUser.getId());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String Name = LoggedUser.getName();
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
        }else{
            try (Connection conn = DatabaseConnection.getConnection()){
                String sql = "SELECT Name From User WHERE User.ID = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, LoggedUser.getId());
                ResultSet rs = stmt.executeQuery();
                usernameLabel.setText(LoggedUser.getName());

            } catch (SQLException e) {
                e.printStackTrace();
                usernameLabel.setText("Erro");
                balanceLabel.setText("Erro");
            }

        }
    }

    public void OpenUserMenu() {
        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), UserMenu);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        UserMenuPane.setManaged(true);
        UserMenuPane.setVisible(true);
        UserMenu.setVisible(true);
        MenuBttn.setOnMouseClicked(event -> {CloseUserMenu();});
    }

    public void CloseUserMenu() {
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(500), UserMenu);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();
        UserMenuPane.setManaged(false);
        UserMenuPane.setVisible(false);
        CloseTransition.setOnFinished(event -> {UserMenu.setVisible(false);
            });
        MenuBttn.setOnMouseClicked(event -> {OpenUserMenu();});
    }

    public void setUserMenu(HBox UserMenu, StackPane UserMenuPane) {
        this.UserMenu = UserMenu;
        this.UserMenuPane = UserMenuPane;
        this.UserMenuPane.setManaged(false);
    }


}
