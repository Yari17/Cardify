package view;

public interface IView {
    void display();
    void close();
    void refresh();
    void showError(String errorMessage);

    
    static String themeCssPath() { return "/styles/theme.css"; }
}
