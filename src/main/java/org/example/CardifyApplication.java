package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.controller.LoginController;
import org.example.model.dao.DaoFactory;
import org.example.model.dao.UserDao;
import org.example.model.dao.json.JsonDaoFactory;
import org.example.view.ILoginView;
import org.example.view.IViewFactory;
import org.example.view.javafx.JavaFxViewFactory;


public class CardifyApplication extends Application {
    private LoginController controller;

    @Override
    public void start(Stage primaryStage) {
        Main.Config cfg = Main.parseArgs(getParameters().getRaw());

        // Set global persistence type for UI display
        AppConfig.setPersistenceType(cfg.daoType);

        DaoFactory daoFactory = AppConfig.DAO_TYPE_JDBC.equals(cfg.daoType)
                ? new org.example.model.dao.jdbc.JdbcDaoFactory()
                : new JsonDaoFactory();

        UserDao userDao = daoFactory.createUserDao();
        IViewFactory IViewFactory = new JavaFxViewFactory();
        ILoginView ILoginView = IViewFactory.createLoginView();

        controller = new LoginController(ILoginView, userDao);

        // Wire the application controller into the view via the interface (no casting/reflection)
        ILoginView.setController(controller);

        controller.show();
    }


    @Override
    public void stop() {
        if (controller != null) {
            controller.cleanup();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
