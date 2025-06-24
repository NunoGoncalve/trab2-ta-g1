package com.example.catcoins;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @FXML private Button uploadImageButton;
    @FXML private Label imageFileNameLabel;
    @FXML private StackPane Background;
    private String localFilePath;

    @FXML
    public void initialize() {
        LoadCoins();
        uploadImageButton.setOnAction(event -> SelectIMG());
    }

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
    }


    @FXML
    public void newCoinForm() {
        boolean isVisible = newCoinForm.isVisible();
        newCoinForm.setVisible(!isVisible);
        newCoinForm.setManaged(!isVisible);
    }

    public void LoadCoins() {
        String sql = "SELECT id, Name, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value FROM Coin where Status='Active'";

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
                editButton.setOnAction(e -> EditCoin(id, name, price));

                Button deleteButton = new Button("✖");
                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> DisableCoin(id));

                buttonsBox.getChildren().addAll(editButton, deleteButton);
                grid.add(buttonsBox, 4, 0);

                coinListVBox.getChildren().add(grid);
            }

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    private void SelectIMG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a .PNG file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagem PNG", "*.png")
        );

        File file = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (file != null) {
            localFilePath = file.getAbsolutePath();
            imageFileNameLabel.setText(file.getName());
        }
    }

    @FXML
    public void NewCoin() {
        String nome = coinNameField.getText().trim();
        String precoText = coinPriceField.getText().trim();
        double Value;

        if (nome.isEmpty() || precoText.isEmpty()) {
            AlertUtils.showAlert(Background, "Coin name cannot be empty!");
            return;
        }

        try {
            Value = Double.parseDouble(precoText.replace(",", "."));
        } catch (NumberFormatException e) {
            AlertUtils.showAlert(Background, "Coin value must be positive!");
            return;
        }

        if (localFilePath == null || localFilePath.isEmpty()) {
            AlertUtils.showAlert(Background, "Please, add a .png file!");
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM Coin WHERE Name = ?";
        String insertSql = "INSERT INTO Coin (Name) VALUES (?)";
        String ValueInsertSql = "INSERT INTO CoinHistory (Coin, Value) VALUES (?,?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkSql)){
            checkStmt.setString(1, nome);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                AlertUtils.showAlert(Background, "There is a coin with that name, please choose another one!");
                return;
            }

            int coinId = -1;

            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, nome);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                coinId = generatedKeys.getInt(1);
                PreparedStatement InsertStmt = conn.prepareStatement(ValueInsertSql);
                InsertStmt.setInt(1, coinId);
                InsertStmt.setDouble(2, Value);
                InsertStmt.executeUpdate();
            }

            // Copiar imagem para imgs/logo/coinId.png
            Path sourcePath = Paths.get(localFilePath);
            Path targetPath = Paths.get("imgs/logo/" + coinId + ".png");

            Files.createDirectories(targetPath.getParent()); // Garante que a pasta existe
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Enviar para o FTP
            FTPConfig.UploadFile(coinId);

            // Deleta imagem local após upload
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException e) {
                System.err.println("Error deleting image: " + e.getMessage());
            }

            coinNameField.clear();
            coinPriceField.clear();
            imageFileNameLabel.setText("");
            localFilePath = null;

            newCoinForm.setVisible(false);
            newCoinForm.setManaged(false);

            AlertUtils.showAlert(Background, "The coin was added with success!");
            LoadCoins();

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        } catch (IOException e) {
            AlertUtils.showAlert(Background, "There was an error saving the uploaded file\n Please try again later");
            e.printStackTrace();
            System.err.println("File error: " + e.getMessage());
        }
    }

    /**
     * Abre o formulário de edição preenchido com os dados da moeda
     */
    @FXML
    private void EditCoin(int id, String nome, double preco) {
        currentEditCoinId = id;
        nomeOriginal = nome;
        precoOriginal = preco;

        editCoinNameField.setText(nome);
        editCoinPriceField.setText(String.format("%.2f", preco));
        editCoinPriceField.setEditable(false);
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
    public void ConfirmEdit() {
        String nome = editCoinNameField.getText().trim();

        if (nome.isEmpty()) {
            AlertUtils.showAlert(Background, "Please,fill all the fields.");
            return;
        }

        // Verifica se houve alterações REAIS
        if (nome.equalsIgnoreCase(nomeOriginal)) {
            AlertUtils.showAlert(Background, "No modification was made.");
            return;
        }

        String sql = "UPDATE Coin SET Name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setInt(2, currentEditCoinId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                AlertUtils.showAlert(Background, "Coin updated with success!");
                CancelEdit();
                LoadCoins();
            } else {
                AlertUtils.showAlert(Background, "The selected coin wasn't updated.");
            }

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }


    /**
     * Cancela a edição da moeda
     */
    @FXML
    public void CancelEdit() {
        editCoinNameField.clear();
        editCoinPriceField.clear();
        editingCoinLabel.setText("");

        editCoinForm.setVisible(false);
        editCoinForm.setManaged(false);

        currentEditCoinId = -1;
    }

    public void DisableCoin(int id) {
        if (!AlertUtils.ConfirmAction("Confirmation", "Are you sure you wish to deactivate this coin?")) {
            return;
        }

        String sql = "Update Coin Set Status='Disabled' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                AlertUtils.showAlert(Background, "Coin deactivated with success!");
            } else {
                AlertUtils.showAlert(Background, "The select coin wasn't deactivated");
            }

            LoadCoins();
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }
}


