package org.example;

import org.example.config.AppConfig;
import org.example.controller.LoginController;
import org.example.model.dao.DaoFactory;
import org.example.model.dao.UserDao;
import org.example.model.dao.jdbc.JdbcDaoFactory;
import org.example.model.dao.json.JsonDaoFactory;
import org.example.view.ILoginView;
import org.example.view.IViewFactory;
import org.example.view.cli.CliIViewFactory;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // Simple immutable config holder
    public static class Config {
        public final String viewType;
        public final String daoType;

        public Config(String viewType, String daoType) {
            this.viewType = viewType;
            this.daoType = daoType;
        }
    }

    // Parse from String[] for CLI entrypoint
    public static Config parseArgs(String[] args) {
        // Delegate to List-based parser
        return parseArgs(asList(args));
    }

    // Parse from List<String> for JavaFX Application params
    public static Config parseArgs(List<String> params) {
        String viewType = AppConfig.DEFAULT_VIEW_TYPE;
        String daoType = AppConfig.DEFAULT_DAO_TYPE;

        for (String p : params) {
            String lower = p.toLowerCase(Locale.ROOT);
            if (lower.startsWith(AppConfig.ARG_PREFIX_VIEW)) {
                viewType = lower.substring(AppConfig.ARG_PREFIX_VIEW.length());
            } else if (lower.startsWith(AppConfig.ARG_PREFIX_DAO)) {
                daoType = lower.substring(AppConfig.ARG_PREFIX_DAO.length());
            } else {
                if (AppConfig.VIEW_TYPE_CLI.equals(lower) || AppConfig.VIEW_TYPE_JAVAFX.equals(lower)) {
                    viewType = lower;
                }
                if (AppConfig.DAO_TYPE_JDBC.equals(lower) || AppConfig.DAO_TYPE_JSON.equals(lower)) {
                    daoType = lower;
                }
            }
        }
        return new Config(viewType, daoType);
    }

    public static void main(String[] args) {
        Config cfg = parseArgs(args);

        // Set global persistence type for UI display
        AppConfig.setPersistenceType(cfg.daoType);

        if (AppConfig.VIEW_TYPE_JAVAFX.equals(cfg.viewType)) {
            // delegate to JavaFX application
            CardifyApplication.main(args);
            return;
        }

        // CLI path: create DAO factory
        DaoFactory daoFactory = (AppConfig.DAO_TYPE_JDBC.equals(cfg.daoType))
                ? new JdbcDaoFactory()
                : new JsonDaoFactory();

        UserDao userDao = daoFactory.createUserDao();

        // CLI view factory
        IViewFactory viewFactory = new CliIViewFactory();

        ILoginView loginView = viewFactory.createLoginView();

        // Create controller and wire controller into the view
        LoginController controller = new LoginController(loginView, userDao);
        try {
            loginView.setController(controller);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to set controller on view", e);
        }

        // Run view
        controller.show();
    }
}
