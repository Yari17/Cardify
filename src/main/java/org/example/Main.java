package org.example;

import controller.ApplicationController;

public class Main {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            ApplicationController applicationController = new ApplicationController();

            applicationController.start();
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error starting the application: {0}", ex.getMessage());
        }
    }
}
