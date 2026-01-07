package controller;

import view.storeHomepage.IStoreHomePageView;

import java.util.logging.Logger;

public class StoreHomePageController {
    private static final Logger LOGGER = Logger.getLogger(StoreHomePageController.class.getName());

    private IStoreHomePageView view;
    private final String username;
    private final ApplicationController applicationController;

    public StoreHomePageController(String username, ApplicationController applicationController) {
        this.username = username;
        this.applicationController = applicationController;
    }

    public void setView(IStoreHomePageView view) {
        this.view = view;
    }

    public String getUsername() {
        return username;
    }

    public void onLogoutRequested() {
        LOGGER.info("Store user " + username + " logging out");
        applicationController.navigateToLogin();
    }

    public void onExitRequested() {
        applicationController.exit();
    }
}
