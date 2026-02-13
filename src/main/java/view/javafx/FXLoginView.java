package view.javafx;

import controller.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import view.ILoginView;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementazione JavaFX della vista di login.
 * Gestisce l'interazione grafica per l'autenticazione e la registrazione degli
 * utenti,
 * interfacciandosi con il {@link LoginController} e caricando il layout da file
 * FXML.
 */
public class FXLoginView implements ILoginView {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(FXLoginView.class.getName());

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerStoreButton;
    @FXML
    private Button registerCollectorButton;
    @FXML
    private Label errorLabel; // Label per mostrare gli errori

    private LoginController loginController;

    // Riferimento allo stage principale per consentire la chiusura dopo il login
    private Stage stage;

    private static final String ERR_MSG_CONTROLLER_MISSING = "Controller mancante";
    private static final String ERR_MSG_LOGIN_CONTROLLER_NOT_SET = "LoginController non impostato";
    private static final String TITLE_LOGIN = "Login";
    private static final String CSS_STYLE = "/styles/style.css";

    @FXML
    private void initialize() {
        setupButtonListeners();
        setupFieldResizing();
    }

    /**
     * Configura i listener per i pulsanti di login e registrazione.
     */
    private void setupButtonListeners() {
        if (loginButton != null) {
            loginButton.setMaxWidth(Double.MAX_VALUE);
            loginButton.setOnAction(evt -> handleAction(() -> loginController.handleLogin()));
        }

        setupRegisterButton(registerStoreButton, () -> loginController.handleRegistrationAsStore());
        setupRegisterButton(registerCollectorButton, () -> loginController.handleRegistrationAsCollector());
    }

    private void setupRegisterButton(Button button, Runnable action) {
        if (button != null) {
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.setOnAction(evt -> handleAction(action));
        }
    }

    private void handleAction(Runnable action) {
        if (loginController != null) {
            action.run();
        } else {
            showAlert(Alert.AlertType.WARNING, ERR_MSG_CONTROLLER_MISSING, ERR_MSG_LOGIN_CONTROLLER_NOT_SET);
        }
    }

    private void setupFieldResizing() {
        if (usernameField != null)
            usernameField.setMaxWidth(Double.MAX_VALUE);
        if (passwordField != null)
            passwordField.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public String getUsername() {
        return usernameField != null ? usernameField.getText() : "";
    }

    @Override
    public String getPassword() {
        return passwordField != null ? passwordField.getText() : "";
    }

    @Override
    public void showLoginSuccess() {
        // Nessuna operazione: la navigazione e la chiusura della vista sono gestite da
        // ApplicationController.navigateTO
    }

    @Override
    public void showLoginFailure(String errorMessage) {
        showAlert(Alert.AlertType.ERROR, "Login failed", errorMessage);
    }

    @Override
    public void showRegistrationSuccess() {
        showAlert(Alert.AlertType.INFORMATION, "Registration", "Registration successful");
    }

    @Override
    public void showRegistrationFailure(String errorMessage) {
        showAlert(Alert.AlertType.ERROR, "Registration failed", errorMessage);
    }

    /**
     * Mostra un alert di dialogo.
     * 
     * @param type  Il tipo di alert.
     * @param title Il titolo della finestra.
     * @param msg   Il messaggio da visualizzare.
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Carica il file FXML della pagina di login e inizializza la scena.
     * Assicura che l'operazione avvenga sul thread JavaFX (Platform.runLater se
     * necessario).
     */
    @Override
    public void display() {
        // Carica l'FXML e mostra la stage sulla JavaFX Application Thread
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginPage.fxml"));
                loader.setController(this);
                Parent root = loader.load();
                this.initialize();

                if (this.loginController != null) {
                    this.setLoginController(this.loginController);
                }

                Scene scene = new Scene(root);
                showScene(scene);

            } catch (IOException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Unable to load login FXML", e);
            }
        };

        if (Platform.isFxApplicationThread())

        {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    @Override
    public void refresh() {
        // Non richiesto per la vista di Login
    }

    @Override
    public void showError(String errorMessage) {
        // Mostra il messaggio nella label degli errori; fallback ad Alert se la label
        // non è disponibile
        Runnable r = () -> {
            if (errorLabel != null) {
                errorLabel.setText(errorMessage);
                errorLabel.setVisible(true);
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore", errorMessage);
            }
        };

        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public void setController(Object controller) {
        this.loginController = (LoginController) controller;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;

    }

    @Override
    public void close() {
        Runnable r = () -> {
            if (stage != null) {
                try {
                    stage.hide();
                } catch (Exception _) {
                    // Ignora errore chiusura stage
                }
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    private void showScene(Scene scene) {
        Optional<Window> existing = Window.getWindows().stream().filter(Window::isShowing).findFirst();
        if (existing.isPresent() && existing.get() instanceof Stage existingStage) {
            configureStage(existingStage, scene);
        } else {
            Stage st = new Stage();
            configureStage(st, scene);
            st.show();
        }
    }

    private void configureStage(Stage st, Scene scene) {
        st.setScene(scene);
        st.setTitle(TITLE_LOGIN);
        st.setWidth(1280);
        st.setHeight(800);
        st.centerOnScreen();
        this.stage = st;

        java.net.URL cssUrl = getClass().getResource(CSS_STYLE);
        if (cssUrl != null)
            st.getScene().getStylesheets().add(cssUrl.toExternalForm());
    }
}
