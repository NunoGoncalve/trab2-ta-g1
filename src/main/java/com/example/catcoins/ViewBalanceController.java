package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ViewBalanceController {

    @FXML
    private Label balanceLabel;

    @FXML
    private Label PendingbalanceLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private HBox UserMenu;

    @FXML
    private StackPane UserMenuPane;

    @FXML
    private ImageView MenuBttn;

    @FXML
    private Label currencyLabel;

    @FXML
    private Label WalletIcon;

    @FXML
    private HBox UserInfo;

    @FXML
    private StackPane Stack;

    // Moeda atual para exibição (padrão é USD)
    private String currentCurrency = "USD $";

    // Símbolo da moeda atual
    private String currencySymbol = "$";

    // Valor do saldo atual em EUR (moeda base do banco de dados)
    private double currentBalanceInDol = 0.0;

    // Valor do saldo atual na moeda de exibição
    private double currentDisplayBalance = 0.0;

    public void setUser(User LoggedUser) {
        if (LoggedUser instanceof Client) {
            Client LoggedClient = (Client) LoggedUser;
            LoggedClient.getWallet().GetUpdatedBalance();
            double Balance = LoggedClient.getWallet().getBalance();
            double PendingBalance = LoggedClient.getWallet().getPendingBalance();
            /*currentDisplayBalance = currentBalanceInDol;

            String dbCurrency = LoggedClient.getWallet().getCurrency();
            if (dbCurrency != null && !dbCurrency.isEmpty()) {
                // Atualiza a moeda e converte o saldo se necessário
               // updateCurrency(dbCurrency);
            }*/

            usernameLabel.setText(LoggedClient.getName());

            if (Balance <= 0) {
                balanceLabel.setText("0.00");
            } else {
                balanceLabel.setText(String.format("%.2f", Balance));
            }
            PendingbalanceLabel.setText(String.format("%.2f", PendingBalance));

        }else{
            usernameLabel.setText(LoggedUser.getName());
            balanceLabel.setManaged(false);
            PendingbalanceLabel.setManaged(false);
            currencyLabel.setManaged(false);
            WalletIcon.setManaged(false);
            Stack.setMaxWidth(200);
        }
    }

    public void OpenUserMenu() {
        FadeTransition OpenTransition = new FadeTransition(Duration.millis(500), UserMenuPane);
        OpenTransition.setFromValue(0.0);
        OpenTransition.setToValue(1.0);
        OpenTransition.play();
        UserMenuPane.setManaged(true);
        UserMenuPane.setVisible(true);
        MenuBttn.setOnMouseClicked(event -> {CloseUserMenu();});
    }

    public void CloseUserMenu() {
        FadeTransition CloseTransition = new FadeTransition(Duration.millis(250), UserMenuPane);
        CloseTransition.setFromValue(1.0);
        CloseTransition.setToValue(0.0);
        CloseTransition.play();

        CloseTransition.setOnFinished(event -> {
            UserMenuPane.setManaged(false);
            UserMenuPane.setVisible(false);
        });


        MenuBttn.setOnMouseClicked(event -> {OpenUserMenu();});
    }

    public void setUserMenu(HBox UserMenu, StackPane UserMenuPane) {
        this.UserMenu = UserMenu;
        this.UserMenuPane = UserMenuPane;
        this.UserMenuPane.setManaged(false);
    }

    /*private void updateCurrencyLabel() { // Vai alterar o texto em cima
        // Se o label de moeda existir, atualiza seu texto
        if (currencyLabel != null) {
            if (currentCurrency.equals("USD")) {
                currencyLabel.setText("USD $");
            } else if (currentCurrency.equals("EUR")) {
                currencyLabel.setText("EUR €");
            } else {
                currencyLabel.setText(currentCurrency);
            }
        }

        // Se o balanceLabel já tiver um valor, atualiza o formato para incluir o nome da moeda
        if (balanceLabel != null && balanceLabel.getText() != null && !balanceLabel.getText().equals("Erro")) {
            if (currentDisplayBalance <= 0) {
                balanceLabel.setText("0.00" + currencySymbol);
            } else {
                // Formato: valor + símbolo (nome da moeda)
                balanceLabel.setText(String.format("%.2f%s", currentDisplayBalance, currencySymbol));
            }
        }
    }

     // Atualiza a moeda usada para exibição do saldo
     public void updateCurrency(String currency) {
         // Guarda a moeda anterior para conversão
         String previousCurrency = this.currentCurrency;

         // Atualiza a moeda atual
         this.currentCurrency = currency;

         // Atualiza o símbolo da moeda
         if ("USD".equals(currency)) {
             currencySymbol = "$";
         } else {
             currencySymbol = "€";
         }

         // Converte o valor do saldo
         if (!previousCurrency.equals(currency)) {
             if (currency.equals("USD") && previousCurrency.equals("EUR")) {
                 // Conversão de EUR para USD (1 EUR = 1/0.88 USD)
                 currentDisplayBalance = currentBalanceInDol * (1.0 / 0.88);
             } else if (currency.equals("EUR") && previousCurrency.equals("USD")) {
                 // Conversão de USD para EUR (1 USD = 0.88 EUR)
                 currentDisplayBalance = currentBalanceInDol;
             }
         }

         // Atualiza a exibição do saldo e o label de moeda
         updateCurrencyLabel();
     }


   //Atualiza a moeda usada para exibição do saldo com conversão de valores
    public void updateCurrencyWithConversion(String toCurrency, String fromCurrency, UserConfigController controller) {
        // Atualiza a moeda atual
        this.currentCurrency = toCurrency;

        // Atualiza o símbolo da moeda
        if ("USD".equals(toCurrency)) {
            currencySymbol = "$";
        } else {
            currencySymbol = "€";
        }

        // Converte o valor do saldo
        if (!fromCurrency.equals(toCurrency)) {
            // Se estamos mudando de EUR para USD ou vice-versa, convertemos o valor
            if (controller != null) {
                currentDisplayBalance = controller.convertCurrency(currentDisplayBalance, fromCurrency, toCurrency);
            } else {
                //conversão direta se o controller não estiver disponível (forçada)
                if (toCurrency.equals("USD") && fromCurrency.equals("EUR")) {
                    // Conversão de EUR para USD (1 EUR = 1/0.88 USD)
                    currentDisplayBalance = currentDisplayBalance * (1.0 / 0.88);
                } else if (toCurrency.equals("EUR") && fromCurrency.equals("USD")) {
                    // Conversão de USD para EUR (1 USD = 0.88 EUR)
                    currentDisplayBalance = currentDisplayBalance * 0.88;
                }
            }
        }

        // Atualiza a exibição do saldo e o label de moeda
        updateCurrencyLabel();
    }*/

}
