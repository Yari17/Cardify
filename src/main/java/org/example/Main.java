package org.example;

import controller.ApplicationController;

public class Main {
    public static void main(String[] args) {
        try {
            ApplicationController applicationController = new ApplicationController();

            applicationController.start();
        } catch (Exception e) {
            System.err.println("Error starting the application: " + e.getMessage());
        }
    }
}
