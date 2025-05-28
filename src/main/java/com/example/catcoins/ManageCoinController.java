package com.example.catcoins;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.sql.*;

public class ManageCoinController {

    @FXML private VBox coinListVBox;
    @FXML private TextField coinNameField;
    @FXML private TextField coinPriceField;
    @FXML private VBox newCoinForm;
    @FXML private BorderPane MainPanel;
    @FXML private TextField editCoinNameField;
    @FXML private TextField editCoinPriceField;
    @FXML private VBox editCoinForm;
    @FXML private Label editingCoinLabel;
    @FXML private VBox Stack;
    @FXML
    public void initialize() {
        carregarMoedas();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MainPanel.setLeft(menu);
    }

    public void setUser(User LoggedUser) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent menu = null;
        try {
            menu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MenuController controller = loader.getController();
        controller.setUser(LoggedUser);
        MainPanel.setLeft(menu);

        loader = new FXMLLoader(getClass().getResource("UserMenu.fxml"));
        Parent usermenu = null;
        try {
            usermenu = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserMenuController controller2 = loader.getController();
        controller2.setUser(LoggedUser);
        MainPanel.setRight(usermenu);

        loader = new FXMLLoader(getClass().getResource("ViewBalance.fxml"));
        Parent Balance = null;
        try {
            Balance = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewBalanceController controller3 = loader.getController();
        controller3.setUser(LoggedUser);
        controller3.setUserMenu((HBox)usermenu.lookup("#UserMenu"), (StackPane)usermenu.lookup("#UserMenuPane"));
        Stack.getChildren().add(0, Balance);


    }

    @FXML
    public void newCoinForm() {
        boolean isVisible = newCoinForm.isVisible();
        newCoinForm.setVisible(!isVisible);
        newCoinForm.setManaged(!isVisible);
    }

    public void carregarMoedas() {
        String sql = "SELECT id, Name, Value FROM Coin";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Remove coin entries but keep the header
            coinListVBox.getChildren().removeIf(node ->
                    node instanceof GridPane && node.getStyleClass().contains("coin-entry"));

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("value");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER_LEFT);
                grid.setHgap(20);
                grid.getStyleClass().add("coin-entry");

                // Define column constraints with percentage widths
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(15);
                col1.setHalignment(HPos.CENTER);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(25);
                col2.setHalignment(HPos.CENTER);

                ColumnConstraints col3 = new ColumnConstraints();
                col3.setPercentWidth(25);
                col3.setHalignment(HPos.CENTER);

                ColumnConstraints col4 = new ColumnConstraints();
                col4.setPercentWidth(25);
                col4.setHalignment(HPos.CENTER);

                ColumnConstraints col5 = new ColumnConstraints();
                col5.setPercentWidth(10);
                col5.setHalignment(HPos.CENTER);

                grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5);

                // Logo (usando emoji padrão para moedas)
                Label logoLabel = new Label("💰");
                logoLabel.getStyleClass().add("coin-logo");
                grid.add(logoLabel, 0, 0);

                // Nome da moeda
                Label nameLabel = new Label(name);
                nameLabel.getStyleClass().add("coin-label");
                grid.add(nameLabel, 1, 0);

                // Preço da moeda
                Label priceLabel = new Label(String.format("%.2f$", price));
                priceLabel.getStyleClass().add("coin-price");
                grid.add(priceLabel, 2, 0);

                // Botões de ação
                HBox buttonsBox = new HBox(10);
                buttonsBox.setAlignment(Pos.CENTER);

                Button editButton = new Button("✏");
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> editarMoeda(id, name, price));

                Button deleteButton = new Button("✖");
                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> deletarMoeda(id));

                buttonsBox.getChildren().addAll(editButton, deleteButton);
                grid.add(buttonsBox, 4, 0);

                coinListVBox.getChildren().add(grid);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao carregar moedas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void CriarMoeda() {
        String nome = coinNameField.getText().trim();
        String precoText = coinPriceField.getText().trim();

        // Validação básica
        if (nome.isEmpty() || precoText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Por favor, preencha todos os campos.");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoText.replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Formato Inválido", "O preço deve ser um número válido.");
            return;
        }

        String sql = "INSERT INTO Coin (Name, Value) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setDouble(2, preco);
            stmt.executeUpdate();

            coinNameField.clear();
            coinPriceField.clear();

            // Fechar o formulário após adicionar com sucesso
            newCoinForm.setVisible(false);
            newCoinForm.setManaged(false);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Moeda adicionada com sucesso!");
            carregarMoedas();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao adicionar moeda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Variável para armazenar o ID da moeda a ser editada
    private int currentEditCoinId;

    /**
     * Abre o formulário de edição de moeda preenchido com os dados da moeda selecionada
     */
    private void editarMoeda(int id, String nome, double preco) {
        currentEditCoinId = id;

        editCoinNameField.setText(nome);
        editCoinPriceField.setText(String.valueOf(preco));
        editingCoinLabel.setText("• " + nome);

        editCoinForm.setVisible(true);
        editCoinForm.setManaged(true);

        newCoinForm.setVisible(false);
        newCoinForm.setManaged(false);
    }

    /**
     * Método para confirmar a edição da moeda
     */
    @FXML
    public void confirmarEdicaoMoeda() {
        String nome = editCoinNameField.getText().trim();
        String precoText = editCoinPriceField.getText().trim();

        // Validação básica
        if (nome.isEmpty() || precoText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Por favor, preencha todos os campos.");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoText.replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Formato Inválido", "O preço deve ser um número válido.");
            return;
        }
        if (nome.equalsIgnoreCase(editingCoinLabel.getText().replace("• ", "")) &&
                preco == Double.parseDouble(editCoinPriceField.getText().replace(",", "."))) {
            showAlert(Alert.AlertType.INFORMATION, "Sem Alterações", "Nenhuma modificação foi feita.");
            return;
        }
        // Atualiza a moeda no banco de dados
        String sql = "UPDATE Coin SET Name = ?, Value = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setDouble(2, preco);
            stmt.setInt(3, currentEditCoinId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Moeda atualizada com sucesso!");

                // Limpa os campos e fecha o formulário de edição
                cancelarEdicao();

                // Recarrega a lista de moedas
                carregarMoedas();
            } else {
                showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhuma moeda foi atualizada.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao atualizar moeda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para cancelar a edição
     */
    @FXML
    public void cancelarEdicao() {
        editCoinNameField.clear();
        editCoinPriceField.clear();
        editingCoinLabel.setText("");

        editCoinForm.setVisible(false);
        editCoinForm.setManaged(false);

        currentEditCoinId = -1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmarAcao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        ButtonType botaoSim = new ButtonType("Sim", ButtonBar.ButtonData.YES);
        ButtonType botaoNao = new ButtonType("Não", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(botaoSim, botaoNao);

        return alert.showAndWait().filter(resposta -> resposta == botaoSim).isPresent();
    }

    public void deletarMoeda(int id) {
        if (!confirmarAcao("Confirmação", "Tem certeza que deseja deletar esta moeda?")) {
            return;
        }

        String sql = "DELETE FROM Coin WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Moeda removida com sucesso!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhuma moeda foi removida.");
            }

            carregarMoedas();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao deletar moeda: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


