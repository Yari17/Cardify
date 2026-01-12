package controller;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreHPController {
    private static final Logger LOGGER = Logger.getLogger(StoreHPController.class.getName());

    private final String username;
    private final ApplicationController navigationController;

    public StoreHPController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public String getUsername() {
        return username;
    }

    public void onLogoutRequested() {
        LOGGER.log(Level.INFO, "Store user {0} logging out", username);
        navigationController.logout();
    }

    public void onExitRequested() {
        System.exit(0);
    }
}
