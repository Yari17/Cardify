package view;

import controller.ConfigurationManager;

public interface IConfigurationView extends IView {
    void display();
    String getInterfaceChoice();
    String getPersistenceChoice();
    void showInterfaceSelected(String interfaceType);
    void showPersistenceSelected(String persistenceType);
    void showInvalidChoice();
    void close();
    void setController(ConfigurationManager controller);
}

