package view.factory;

import controller.CollectorHPController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHPController;
import view.InputManager;
import view.collectorhomepage.CliCollectorHPView;
import view.collectorhomepage.ICollectorHPView;
import view.login.CliILoginView;
import view.login.ILoginView;
import view.registration.CliRegistrationView;
import view.registration.IRegistrationView;
import view.storehomepage.CliStoreHPView;
import view.storehomepage.IStoreHPView;

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
    public ICollectorHPView createCollectorHomePageView(CollectorHPController controller) {
        CliCollectorHPView view = new CliCollectorHPView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public IStoreHPView createStoreHomePageView(StoreHPController controller) {
        CliStoreHPView view = new CliStoreHPView(inputManager);
        view.setController(controller);
        return view;
    }
}