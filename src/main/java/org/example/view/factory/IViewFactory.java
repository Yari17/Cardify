package org.example.view.factory;

import org.example.controller.HomePageController;
import org.example.controller.LoginController;
import org.example.controller.RegistrationController;
import org.example.view.homepage.IHomePageView;
import org.example.view.login.ILoginView;
import org.example.view.registration.IRegistrationView;

/**
 * Abstract Factory for creating views.
 * Controllers are injected as parameters following Dependency Injection principle.
 */
public interface IViewFactory {
    ILoginView createLoginView(LoginController controller);
    IRegistrationView createRegistrationView(RegistrationController controller);
    IHomePageView createHomePageView(HomePageController controller);
}
