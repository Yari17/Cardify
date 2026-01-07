package controller;

import view.collectorhomepage.ICollectorHomePageView;

import java.util.logging.Logger;

public class CollectorHomePageController {
    private static final Logger LOGGER = Logger.getLogger(CollectorHomePageController.class.getName());

    private final String username;
    private final ApplicationController applicationController;

    public CollectorHomePageController(String username, ApplicationController applicationController) {
        this.username = username;
        this.applicationController = applicationController;
    }

    public void setView(ICollectorHomePageView view) {
    }

    public String getUsername() {
        return username;
    }

    public void onLogoutRequested() {
        LOGGER.info("User " + username + " logging out");
        applicationController.logout();
    }

    public void onExitRequested() {
        applicationController.exit();
    }
}
