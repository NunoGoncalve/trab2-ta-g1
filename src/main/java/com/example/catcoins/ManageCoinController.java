package com.example.catcoins;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;

public class ManageCoinController extends MenuLoader {
    private int currentEditCoinId;
    private String nomeOriginal;
    private double precoOriginal;
    // Isto é utilizado na funcao de edicao, para comparacao com dados atuais - BD (TEMP)

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
    @FXML private VBox editCoinSection;
    @FXML
    public void initialize() {
        carregarMoedas();
    }

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }


    @FXML
    public void newCoinForm() {
        boolean isVisible = newCoinForm.isVisible();
        newCoinForm.setVisible(!isVisible);
        newCoinForm.setManaged(!isVisible);
    }

    @FXML
    private BorderPane editCoinHeader;


    public void carregarMoedas() {
        String sql = "SELECT id, Name, Value FROM Coin";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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

        String checkSql = "SELECT COUNT(*) FROM Coin WHERE Name = ?";
        String insertSql = "INSERT INTO Coin (Name, Value) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // Verifica se já existe uma moeda com o mesmo nome
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, nome);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Moeda Já Existe", "Já existe uma moeda com esse nome.");
                    return;
                }
            }

            // Insere a nova moeda
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, nome);
                insertStmt.setDouble(2, preco);
                insertStmt.executeUpdate();
            }

            coinNameField.clear();
            coinPriceField.clear();

            newCoinForm.setVisible(false);
            newCoinForm.setManaged(false);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Moeda adicionada com sucesso!");
            carregarMoedas();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", "Erro ao adicionar moeda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre o formulário de edição preenchido com os dados da moeda
     */
    @FXML
    private void editarMoeda(int id, String nome, double preco) {
        currentEditCoinId = id;
        nomeOriginal = nome;
        precoOriginal = preco;

        editCoinNameField.setText(nome);
        editCoinPriceField.setText(String.format("%.2f", preco));  // ou use vírgula se preferir
        editingCoinLabel.setText("• " + nome);

        editCoinForm.setVisible(true);
        editCoinForm.setManaged(true);

        newCoinForm.setVisible(false);
        newCoinForm.setManaged(false);
    }




    /**
     * Confirma a edição da moeda
     */
    @FXML
    public void confirmarEdicaoMoeda() {
        String nome = editCoinNameField.getText().trim();
        String precoText = editCoinPriceField.getText().trim();

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

        // Verifica se houve alterações REAIS
        if (nome.equalsIgnoreCase(nomeOriginal) && Double.compare(preco, precoOriginal) == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Sem Alterações", "Nenhuma modificação foi feita.");
            return;
        }

        String sql = "UPDATE Coin SET Name = ?, Value = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setDouble(2, preco);
            stmt.setInt(3, currentEditCoinId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Moeda atualizada com sucesso!");
                cancelarEdicao();
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
     * Cancela a edição da moeda
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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


