package org.example.view.factory;

import org.example.controller.HomePageController;
import org.example.controller.LoginController;
import org.example.controller.RegistrationController;
import org.example.view.homepage.CliHomePageView;
import org.example.view.homepage.IHomePageView;
import org.example.view.login.CliILoginView;
import org.example.view.login.ILoginView;
import org.example.view.registration.CliRegistrationView;
import org.example.view.registration.IRegistrationView;

public class CliIViewFactory implements IViewFactory {

    @Override
    public ILoginView createLoginView(LoginController controller) {
        CliILoginView view = new CliILoginView();
        view.setController(controller);
        return view;
    }

    @Override
    public IRegistrationView createRegistrationView(RegistrationController controller) {
        CliRegistrationView view = new CliRegistrationView();
        view.setController(controller);
        return view;
    }

    @Override
    public IHomePageView createHomePageView(HomePageController controller) {
        CliHomePageView view = new CliHomePageView();
        view.setController(controller);
        return view;
    }
}