package view;

import javafx.stage.Stage;

public interface IView {
    void display();
    void close();
    void refresh();
    void showError(String errorMessage);

    // Optional: allow view factories to set the JavaFX Stage. Views that don't need it can ignore.
    default void setStage(Stage stage) { /* no-op by default */ }

    // Centralized location for the application's theme stylesheet path
    static String themeCssPath() { return "/styles/theme.css"; }
}
