package view.factory;

import controller.CollectorHomePageController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHomePageController;
import view.InputManager;
import view.collectorHomepage.CliCollectorHomePageView;
import view.collectorHomepage.ICollectorHomePageView;
import view.login.CliILoginView;
import view.login.ILoginView;
import view.registration.CliRegistrationView;
import view.registration.IRegistrationView;
import view.storeHomepage.CliStoreHomePageView;
import view.storeHomepage.IStoreHomePageView;

public class CliIViewFactory implements IViewFactory {

    private final InputManager inputManager;

    public CliIViewFactory(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public ILoginView createLoginView(LoginController controller) {
        CliILoginView view = new CliILoginView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public IRegistrationView createRegistrationView(RegistrationController controller) {
        CliRegistrationView view = new CliRegistrationView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public ICollectorHomePageView createCollectorHomePageView(CollectorHomePageController controller) {
        CliCollectorHomePageView view = new CliCollectorHomePageView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public IStoreHomePageView createStoreHomePageView(StoreHomePageController controller) {
        CliStoreHomePageView view = new CliStoreHomePageView(inputManager);
        view.setController(controller);
        return view;
    }
}