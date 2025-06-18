package com.example.catcoins;
import com.example.catcoins.model.User;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class ManageUserController extends MenuLoader {

    @FXML private VBox userListVBox;
    @FXML private TextField userNameField;
    @FXML private TextField userEmailField;
    @FXML private TextField userPasswordField;
    @FXML private ComboBox<String> userRoleField;
    @FXML private ComboBox<String> userStatusField;
    @FXML private VBox newUserForm;
    @FXML private BorderPane MainPanel;
    @FXML private TextField editUserNameField;
    @FXML private TextField editUserEmailField;
    @FXML private TextField editUserPasswordField;
    @FXML private ComboBox<String> editUserRoleField;
    @FXML private ComboBox<String> editUserStatusField;
    @FXML private VBox editUserForm;
    @FXML private Label editingUserLabel;
    @FXML private VBox Stack;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private int currentPage = 0;
    private int usersPerPage = 4;
    private int totalUsers = 0;
    private int currentEditUserId;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel);
    }

    @FXML
    public void initialize() {
        LoadTotalUsers();
        LoadUsersPage(currentPage);
        initializeComboBoxes();
        // Configurar eventos dos botões de paginação
        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                LoadUsersPage(currentPage);
            }
        });

        nextPageButton.setOnAction(e -> {
            if ((currentPage + 1) * usersPerPage < totalUsers) {
                currentPage++;
                LoadUsersPage(currentPage);
            }
        });
    }

    private void LoadTotalUsers() {
        String sql = "SELECT COUNT(*) FROM User";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                totalUsers = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void LoadUsersPage(int pagina) {

        int offset = pagina * usersPerPage;
        String sql = "SELECT ID, Name, Email, Password, Role, Status FROM User ORDER BY Status ASC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usersPerPage);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            // Remove user entries but keep the header
            userListVBox.getChildren().removeIf(node ->
                    node instanceof GridPane && node.getStyleClass().contains("user-entry"));

            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("Name");
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                String role = rs.getString("Role");
                String status = rs.getString("Status");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER_LEFT);
                grid.setHgap(20);
                grid.getStyleClass().add("user-entry");

                // Define column constraints with percentage widths
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(10);
                col1.setHalignment(HPos.CENTER);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(20);
                col2.setHalignment(HPos.CENTER);

                ColumnConstraints col3 = new ColumnConstraints();
                col3.setPercentWidth(25);
                col3.setHalignment(HPos.CENTER);

                ColumnConstraints col4 = new ColumnConstraints();
                col4.setPercentWidth(15);
                col4.setHalignment(HPos.CENTER);

                ColumnConstraints col5 = new ColumnConstraints();
                col5.setPercentWidth(15);
                col5.setHalignment(HPos.CENTER);

                ColumnConstraints col6 = new ColumnConstraints();
                col6.setPercentWidth(15);
                col6.setHalignment(HPos.CENTER);

                grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

                // Avatar (usando emoji padrão para utilizadores)
                Label avatarLabel = new Label("👤");
                avatarLabel.getStyleClass().add("user-avatar");
                grid.add(avatarLabel, 0, 0);

                // Nome do utilizador
                Label nameLabel = new Label(name);
                nameLabel.getStyleClass().add("user-label");
                grid.add(nameLabel, 1, 0);

                // Email do utilizador
                Label emailLabel = new Label(email);
                emailLabel.getStyleClass().add("user-email");
                grid.add(emailLabel, 2, 0);

                // Role do utilizador
                Label roleLabel = new Label(role);
                roleLabel.getStyleClass().add("user-role");
                grid.add(roleLabel, 3, 0);

                // Status do utilizador
                Label statusLabel = new Label(status);
                statusLabel.getStyleClass().add("user-status");
                // Adicionar classe CSS baseada no status
                statusLabel.getStyleClass().add(status.toLowerCase() + "-status");
                grid.add(statusLabel, 4, 0);

                //Botões de ação
                HBox buttonsBox = new HBox(10);
                buttonsBox.setAlignment(Pos.CENTER);

                Button detailsButton = new Button("🔍");
                detailsButton.getStyleClass().add("details-btn");
                detailsButton.setOnAction(e -> UserDetails(id));

                Button editButton = new Button("✏");
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> editarUtilizador(id, name, email, password, role, status));

                Button deleteButton = new Button("✖");
                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> deletarUtilizador(id));

                buttonsBox.getChildren().addAll(detailsButton, editButton, deleteButton);
                grid.add(buttonsBox, 5, 0);

                userListVBox.getChildren().add(grid);
            }

            // Atualiza a informação da página
            updatePageInfo();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Erro ao carregar utilizadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePageInfo() {
        int start = currentPage * usersPerPage + 1;
        int end = Math.min((currentPage + 1) * usersPerPage, totalUsers);
        pageInfoLabel.setText(start + "-" + end + " de " + totalUsers);

        // Desabilita os botões quando não há mais páginas
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable((currentPage + 1) * usersPerPage >= totalUsers);
    }

    private void initializeComboBoxes() {
        // Inicializar ComboBoxes com valores padrão
        userRoleField.getItems().addAll("Admin");
        userStatusField.getItems().addAll("Active");
        editUserRoleField.getItems().addAll("Client");
        editUserStatusField.getItems().addAll("Active", "Disabled");

        // Definir valores padrão
        userRoleField.setValue("Admin");
        userStatusField.setValue("Active");
    }

    @FXML
    public void newUserForm() {
        editUserForm.setVisible(false);
        editUserForm.setManaged(false);
        boolean isVisible = newUserForm.isVisible();
        newUserForm.setVisible(!isVisible);
        newUserForm.setManaged(!isVisible);

    }

    @FXML
    public void CreateUser() {
        String nome = userNameField.getText().trim();
        String email = userEmailField.getText().trim();
        String password = userPasswordField.getText().trim();
        String role = userRoleField.getValue();
        String status = userStatusField.getValue();

        // Validação básica
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Data", "Por favor, preencha todos os campos.");
            return;
        }

        // Validação do email
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please fill in all fields.");
            return;
        }

        // Verificar se o email já existe
        if (emailExists(email)) {
            showAlert(Alert.AlertType.WARNING, "Email already exists", "Please enter a valid email.");
            return;
        }

        try {
            password = PasswordUtils.hashPassword(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String sql = "INSERT INTO User (Name, Email, Password, Role, Status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.setString(5, status);
            stmt.executeUpdate();

            userNameField.clear();
            userEmailField.clear();
            userPasswordField.clear();
            userRoleField.setValue("User");
            userStatusField.setValue("Active");

            // Fechar o formulário após adicionar com sucesso
            newUserForm.setVisible(false);
            newUserForm.setManaged(false);

            showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
            LoadTotalUsers();
            LoadUsersPage(currentPage);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editarUtilizador(int id, String nome, String email, String password, String role, String status) {
        currentEditUserId = id;

        editUserNameField.setText(nome);
        editUserEmailField.setText(email);
        editUserPasswordField.setText(password);
        editUserRoleField.setValue(role);
        editUserStatusField.setValue(status);
        editingUserLabel.setText("• " + nome);

        editUserForm.setVisible(true);
        editUserForm.setManaged(true);

        newUserForm.setVisible(false);
        newUserForm.setManaged(false);
    }

    @FXML
    public void confirmarEdicaoUtilizador() {
        String nome = editUserNameField.getText().trim();
        String email = editUserEmailField.getText().trim();
        String password = editUserPasswordField.getText().trim();
        String role = editUserRoleField.getValue();
        String status = editUserStatusField.getValue();

        // Validação básica
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Data", "Please fill in all fields.");
            return;
        }

        // Validação do email
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email");
            return;
        }

        // Verificar se o email já existe (exceto para o utilizador atual)
        if (emailExistsExceptCurrent(email, currentEditUserId)) {
            showAlert(Alert.AlertType.WARNING, "Email already exists", "This email is already registered by another user.");
            return;
        }

        // Atualiza o utilizador no banco de dados
        String sql = "UPDATE User SET Name = ?, Email = ?, Password = ?, Role = ?, Status = ? WHERE ID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, password); // Em produção, deve ser hash da password
            stmt.setString(4, role);
            stmt.setString(5, status);
            stmt.setInt(6, currentEditUserId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");

                // Limpa os campos e fecha o formulário de edição
                cancelarEdicao();

                // Recarrega a lista de utilizadores
                LoadUsersPage(currentPage);
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No user was updated.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelarEdicao() {
        editUserNameField.clear();
        editUserEmailField.clear();
        editUserPasswordField.clear();
        editUserRoleField.setValue(null);
        editUserStatusField.setValue(null);
        editingUserLabel.setText("");

        editUserForm.setVisible(false);
        editUserForm.setManaged(false);

        currentEditUserId = -1;
    }

    public void UserDetails(int id) {
        try {
            Main.setRoot("UserDetails.fxml", getLoggedUser(), id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletarUtilizador(int id) {
        if (!confirmarAcao("Confimation", "Are you sure you want to deactivate this user?")) {
            return;
        }

        String sql = "UPDATE User SET Status = 'Disabled'  WHERE ID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User successfully removed!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No user was removed");
            }

            LoadTotalUsers();
            // Ajusta a página atual se a última página ficou vazia
            if (currentPage > 0 && currentPage * usersPerPage >= totalUsers) {
                currentPage--;
            }
            LoadUsersPage(currentPage);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM User WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean emailExistsExceptCurrent(String email, int currentUserId) {
        String sql = "SELECT COUNT(*) FROM User WHERE Email = ? AND ID != ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #28323E; -fx-padding: 5; -fx-border-radius: 10");

        Label Label = new Label(message);
        Label.setStyle("-fx-text-fill: white;");

        VBox content = new VBox(10, Label);
        dialogPane.setContent(content);
        alert.showAndWait();
    }

    private boolean confirmarAcao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        ButtonType botaoSim = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType botaoNao = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(botaoSim, botaoNao);

        return alert.showAndWait().filter(resposta -> resposta == botaoSim).isPresent();
    }

    @FXML
    private void exportAllUsersToCSV() throws IOException {
        // Garante que o diretório existe
        Path dirPath = Paths.get("src/main/resources/CSV");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Caminho do ficheiro
        String filePath = "src/main/resources/CSV/users.csv";

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Name;Email;Role;Status\n");

            String sql = "SELECT Name, Email, Role, Status FROM User ORDER BY Status";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String name = rs.getString("Name");
                    String email = rs.getString("Email");
                    String role = rs.getString("Role");
                    String status = rs.getString("Status");

                    String line = String.format("\"%s\";\"%s\";\"%s\";\"%s\"\n", name, email, role, status);
                    writer.write(line);
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "It wasn't possible to export the data: " + e.getMessage());
            e.printStackTrace();
        }

        // Enviar o e-mail com o CSV
        String subject = "CatCoins Users";
        String content = "Data related to system users";
        showAlert(Alert.AlertType.INFORMATION, "Export complete", "The information has been sent to your email");
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content, subject, filePath);

        // Limpa o ficheiro temporário
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println("Erro ao remover ficheiro: " + e.getMessage());
        }
    }
    @FXML
    public void goBack(){
        try {
            Main.setRoot("Main.fxml", super.getLoggedUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}