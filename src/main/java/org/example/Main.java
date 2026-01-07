package org.example;

import org.example.config.AppConfig;
import org.example.controller.ApplicationController;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application entry point.
 * Minimal and clean - delegates all logic to ApplicationController.
 *
 * Usage:
 *   java org.example.Main                    (interactive mode - asks for GUI type)
 *   java org.example.Main [viewType] [daoType]  (command-line mode)
 *
 * Interactive Mode Examples:
 *   java org.example.Main
 *     → Shows menu to choose JavaFX/CLI and JSON/JDBC
 *
 * Command-line Mode Examples:
 *   java org.example.Main javafx json
 *   java org.example.Main cli jdbc
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            String viewType;
            String daoType;

            // If no arguments provided, use interactive configuration
            if (args.length == 0) {
                String[] config = AppConfig.interactiveConfiguration();
                viewType = config[0];
                daoType = config[1];
            } else {
                // Parse arguments from command line
                viewType = args.length > 0 ? args[0] : AppConfig.DEFAULT_VIEW_TYPE;
                daoType = args.length > 1 ? args[1] : AppConfig.DEFAULT_DAO_TYPE;
            }

            // Create application controller
            ApplicationController controller = new ApplicationController(viewType, daoType);

            // Start application based on view type
            if (AppConfig.VIEW_TYPE_JAVAFX.equals(viewType)) {
                // Launch JavaFX application
                ApplicationController.launch(ApplicationController.class, args);
            } else {
                // Start CLI application
                controller.startCli();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting the application", e);
            System.err.println("Error starting the application: " + e.getMessage());
            System.exit(1);
        }
    }
}
