package controller;

import view.storehomepage.IStoreHPView;

import java.util.logging.Logger;

public class StoreHPController {
    private static final Logger LOGGER = Logger.getLogger(StoreHPController.class.getName());

    private IStoreHPView view;
    private final String username;
    private final Navigator navigator;

    public StoreHPController(String username, Navigator navigator) {
        this.username = username;
        this.navigator = navigator;
    }

    public void setView(IStoreHPView view) {
        this.view = view;
    }

    public String getUsername() {
        return username;
    }

    public void onLogoutRequested() {
        LOGGER.info("Store user " + username + " logging out");
        navigator.logout();
    }

    public void onExitRequested() {
        System.exit(0);
    }
}
