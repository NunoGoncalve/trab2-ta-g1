package com.example.catcoins;
import com.example.catcoins.model.Role;
import com.example.catcoins.model.Status;
import com.example.catcoins.model.User;
import com.example.catcoins.model.UserDAO;
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
import java.util.ArrayList;
import java.util.List;

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
    @FXML private BorderPane editUserHeader;
    @FXML private VBox editUserForm;
    @FXML private Label editingUserLabel;
    @FXML private VBox Stack;
    @FXML private StackPane Background;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private int currentPage = 0;
    private int usersPerPage = 4;
    private int totalUsers = 0;
    private int currentEditUserIndex;
    private UserDAO UsrDao=new UserDAO();
    private ArrayList<User> users;

    @Override
    public void setLoggedUser(User user) {
        super.setLoggedUser(user);
        super.LoadMenus(Stack, MainPanel, Background);
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
        try {
            totalUsers=UsrDao.LoadTotalUsers();
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    public void LoadUsersPage(int pagina) {

        int offset = pagina * usersPerPage;

        try {

            // Remove user entries but keep the header
            userListVBox.getChildren().removeIf(node ->
                    node instanceof GridPane && node.getStyleClass().contains("user-entry"));
            users = UsrDao.GetAll();
            for(int i=offset;i<usersPerPage+offset;i++) {
                int finalI = i;
                String name = users.get(i).getName();
                String email = users.get(i).getEmail();
                String role = users.get(i).getRole().toString();
                String status = users.get(i).getStatus().toString();

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
                detailsButton.setOnAction(e -> UserDetails(finalI));

                Button editButton = new Button("✏");
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> EditUser(finalI));

                Button deleteButton = new Button("✖");
                deleteButton.getStyleClass().add("delete-btn");
                deleteButton.setOnAction(e -> DisableUser(finalI));

                buttonsBox.getChildren().addAll(detailsButton, editButton, deleteButton);
                grid.add(buttonsBox, 5, 0);

                userListVBox.getChildren().add(grid);
            }

            // Atualiza a informação da página
            updatePageInfo();

        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
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
        String name = userNameField.getText().trim();
        String email = userEmailField.getText().trim();
        String password = userPasswordField.getText().trim();
        String role = userRoleField.getValue();
        String status = userStatusField.getValue();

        // Validação básica
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || status == null) {
            AlertUtils.showAlert(Background, "Please fill all the fields!");
            return;
        }

        // Validação do email
        if (!isValidEmail(email)) {
            AlertUtils.showAlert(Background, "Invalid email!.");
            return;
        }

        try{
            // Verificar se o email já existe
            if (UsrDao.CheckIfEmail(email)) {
                AlertUtils.showAlert(Background, "This email is already registered by another user.");
                return;
            }

            password = PasswordUtils.hashPassword(password);
            User NewUser = new User (name, email, password, Role.Admin, Status.Active);
            users.add(UsrDao.Add(NewUser));

            userNameField.clear();
            userEmailField.clear();
            userPasswordField.clear();
            userRoleField.setValue("User");
            userStatusField.setValue("Active");

            // Fechar o formulário após adicionar com sucesso
            newUserForm.setVisible(false);
            newUserForm.setManaged(false);

            AlertUtils.showAlert(Background, "User added successfully!");
            LoadTotalUsers();
            LoadUsersPage(currentPage);
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "There was an error");
        }
    }

    public void EditUser(int index) {
        User EditUser = users.get(index);
        currentEditUserIndex = index;
        editUserNameField.setText(EditUser.getName());
        editUserEmailField.setText(EditUser.getEmail());
        editUserPasswordField.setText(EditUser.getPassword());
        editUserRoleField.setValue(EditUser.getRole().toString());
        editUserStatusField.setValue(EditUser.getStatus().toString());
        editingUserLabel.setText("• " + EditUser.getName());

        editUserHeader.setVisible(true);
        editUserHeader.setManaged(true);
        editUserForm.setVisible(true);
        editUserForm.setManaged(true);

        newUserForm.setVisible(false);
        newUserForm.setManaged(false);
    }

    @FXML
    public void ConfirmEdit() {
        String name = editUserNameField.getText().trim();
        String email = editUserEmailField.getText().trim();
        String password = editUserPasswordField.getText().trim();
        String role = editUserRoleField.getValue();
        String status = editUserStatusField.getValue();

        // Validação básica
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || status == null) {
            AlertUtils.showAlert(Background, "Please fill in all fields.");
            return;
        }

        // Validação do email
        if (!isValidEmail(email)) {
            AlertUtils.showAlert(Background, "Please enter a valid email");
            return;
        }

        // Verificar se o email já existe (exceto para o utilizador atual)
        if (emailExistsExceptCurrent(email, users.get(currentEditUserIndex).getID())) {
            AlertUtils.showAlert(Background, "This email is already registered by another user.");
            return;
        }

        try{
            users.get(currentEditUserIndex).setName(name);

            if (UsrDao.Update(users.get(currentEditUserIndex))) {
                AlertUtils.showAlert(Background, "User updated successfully!");

                // Limpa os campos e fecha o formulário de edição
                CancelEdit();

                // Recarrega a lista de utilizadores
                LoadUsersPage(currentPage);
            } else {
                AlertUtils.showAlert(Background,"Error, No user was updated.");
            }
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    @FXML
    public void CancelEdit() {
        editUserNameField.clear();
        editUserEmailField.clear();
        editUserPasswordField.clear();
        editUserRoleField.setValue(null);
        editUserStatusField.setValue(null);
        editingUserLabel.setText("");

        editUserHeader.setVisible(false);
        editUserHeader.setManaged(false);
        editUserForm.setVisible(false);
        editUserForm.setManaged(false);

        currentEditUserIndex = -1;
    }

    public void UserDetails(int Index) {
        try {
            Main.setRoot("UserDetails.fxml", getLoggedUser(), users.get(Index));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void DisableUser(int index) {
        if (!AlertUtils.ConfirmAction("Confimation", "Are you sure you want to deactivate this user?")) {
            return;
        }

        try{
            users.get(index).setStatus(Status.Disabled);

            if (UsrDao.Update(users.get(index))) {
                AlertUtils.showAlert(Background, "User successfully removed!");
            } else {
                AlertUtils.showAlert(Background, "No user was removed");
            }

            LoadTotalUsers();
            // Ajusta a página atual se a última página ficou vazia
            if (currentPage > 0 && currentPage * usersPerPage >= totalUsers) {
                currentPage--;
            }
            LoadUsersPage(currentPage);
        } catch (SQLException e) {
            DatabaseConnection.HandleConnectionError(Background, e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
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
            DatabaseConnection.HandleConnectionError(Background, e);
        }
        return false;
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

        FileWriter writer = new FileWriter(filePath);
        writer.write("Name;Email;Role;Status\n");

        for (User ur : users) {
            String line = String.format("\"%s\";\"%s\";\"%s\";\"%s\"\n", ur.getName(), ur.getEmail(), ur.getRole().toString(), ur.getStatus().toString());
            writer.write(line);
        }

        // Enviar o e-mail com o CSV
        String subject = "CatCoins Users";
        String content = "Data related to system users";
        AlertUtils.showAlert(Background, "The information has been sent to your email");
        EmailConfig.SendEmailAttach(super.getLoggedUser().getEmail(), content, subject, filePath);

        // Limpa o ficheiro temporário
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println("Error deleting file: " + e.getMessage());
        }
    }

    @FXML
    public void goBack(){
        try {
            Main.setRoot("Main.fxml", super.getLoggedUser());
        } catch (Exception e) {
            AlertUtils.showAlert(Background, "There was an error while trying to open the main page");
        }
    }
}