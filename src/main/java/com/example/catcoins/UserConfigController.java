package com.example.catcoins;

import com.example.catcoins.model.Client;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.User;
import com.example.catcoins.model.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;

public class UserConfigController extends MenuLoader {

    @FXML
    private BorderPane MainPanel;

    @FXML
    private TextField Email;

    @FXML
    private TextField Name;

    @FXML
    private Button EditButton;

    @FXML
    private StackPane Background;

   /* @FXML
    private ComboBox<String> currencyComboBox;

    @FXML
    private Label currencyLabel;

    @FXML
    private VBox Stack;

    private String selectedCurrency = "USD"; // USD padrão
    // Taxa de conversão conforme especificado: 1 USD = 0.88 EUR
    private final double USD_TO_EUR_RATE = 0.88;*/

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        LoadInfo();
    }

    public void LoadInfo() {
        Email.setText(super.getLoggedUser().getEmail());
        Name.setText(super.getLoggedUser().getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(super.getLoggedUser());
        MainPanel.setLeft(menu);

       /* if(super.getLoggedUser().getRole()== Role.Client){
            loadCurrencyPreference();  // Carrega a preferência de moeda do usuário
        }else{
            currencyComboBox.setManaged(false);
            currencyComboBox.setVisible(false);
            currencyLabel.setManaged(false);
        }*/

    }

    @FXML
    public void EditButtonOnAction() {
        if (EditButton.getText().equals("Edit")) {
            Name.setEditable(true);
            EditButton.setText("Save");
        }else{
            UserDAO UsrDao = new UserDAO();
            EditButton.setText("Edit");
            Name.setEditable(false);
            super.getLoggedUser().setName(Name.getText().toString());
           try{ UsrDao.Update(super.getLoggedUser()); }
           catch (SQLException e){
               DatabaseConnection.HandleConnectionError(Background, e);
           }
        }
    }

    /*@FXML
    public void initialize() {
        // Garante que o valor padrão do ComboBox seja USD $
        if (currencyComboBox != null) {
            currencyComboBox.setValue("USD $");
        }
    }

     //Atualiza a moeda selecionada e aplica as mudanças necessárias na interface.
     @FXML
     public void onCurrencyChanged(ActionEvent event) {
         // Obtém a moeda anterior para conversão
         String previousCurrency = selectedCurrency;

         // Obtém a nova moeda selecionada do ComboBox
         String comboValue = currencyComboBox.getValue();

         // Limpa os símbolos para uso interno
         if (comboValue.equals("USD $")) {
             selectedCurrency = "USD";
         } else if (comboValue.equals("EUR €")) {
             selectedCurrency = "EUR";
         } else {
             selectedCurrency = comboValue;
         }

         // Salva a preferência de moeda no banco de dados
         saveCurrencyPreference();

         // Atualiza a exibição de valores monetários com conversão
         updateCurrencyDisplay(previousCurrency, selectedCurrency);

         System.out.println("Moeda alterada de " + previousCurrency + " para: " + selectedCurrency);
     }

    public double convertCurrency(double value, String fromCurrency, String toCurrency) {
        // Se as moedas forem iguais, não é necessário converter
        if (fromCurrency.equals(toCurrency)) {
            return value;
        }

        // Conversão de EUR para USD
        if (fromCurrency.equals("EUR") && toCurrency.equals("USD")) {
            return value * EUR_TO_USD_RATE;
        }

        // Conversão de USD para EUR
        if (fromCurrency.equals("USD") && toCurrency.equals("EUR")) {
            return value * USD_TO_EUR_RATE;
        }

        // Caso padrão (não deveria ocorrer)
        return value;
    }

    /**
     * Salva a preferência de moeda do usuário no banco de dados.
     * Atualiza o campo Currency na tabela Wallet.

    private void saveCurrencyPreference() {
        // Verifica se o usuário está logado e é um cliente
        if (super.getLoggedUser() != null && super.getLoggedUser() instanceof Client) {
            ((Client) super.getLoggedUser()).getWallet().SetCurrency(selectedCurrency);
        }
    }

    // Carrega a preferência de moeda do usuário do banco de dados.
    // Lê o campo Currency da tabela Wallet.
    private void loadCurrencyPreference() {
        // Verifica se o usuário está logado e é um cliente
        if (super.getLoggedUser() != null && super.getLoggedUser() instanceof Client) {
            String currency = ((Client) super.getLoggedUser()).getWallet().getCurrency();;
            // Se a moeda não for nula, atualiza a seleção
            if (currency != null && !currency.isEmpty()) {
                selectedCurrency = currency;

                // Atualiza o ComboBox se já estiver inicializado
                if (currencyComboBox != null) {
                    // Adiciona os símbolos às moedas para exibição
                    if (selectedCurrency.equals("USD")) {
                        currencyComboBox.setValue("USD $");
                    } else if (selectedCurrency.equals("EUR")) {
                        currencyComboBox.setValue("EUR €");
                    } else {
                        currencyComboBox.setValue(selectedCurrency);
                    }
                }

                System.out.println("Preferência de moeda carregada do banco de dados: " + selectedCurrency);
            }
        }
    }

    // Atualizar a exibição de valores monetários na interface
    private void updateCurrencyDisplay(String fromCurrency, String toCurrency) {
        // Se o usuário estiver logado, atualiza as informações de saldo
        if (super.getLoggedUser() instanceof Client) {
            try {
                // Tenta encontrar e atualizar o ViewBalanceController
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
                try {
                    Parent viewBalance = loader.load();
                    ViewBalanceController balanceController = loader.getController();

                    // Atualiza a moeda no controlador de saldo com conversão
                  //  balanceController.updateCurrencyWithConversion(toCurrency, fromCurrency, this);
                } catch (IOException e) {
                    // Ignora se não conseguir carregar o controlador
                    System.out.println("Não foi possível atualizar o ViewBalanceController");
                }
            } catch (Exception e) {
                // Ignora exceções gerais ao tentar atualizar outros controladores
            }
        }
    }*/

    @FXML
    public void goBack(){
        if(super.getLoggedUser().getRole()== Role.Admin){
            super.GoTo("Main.fxml", super.getLoggedUser(), Background);
        }else{
            super.GoTo("Userpanel.fxml", super.getLoggedUser(), Background);
        }
    }

}
