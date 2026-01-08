package view.factory;

import controller.CollectorHPController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHPController;
import view.collectorhomepage.ICollectorHPView;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHPView;

public interface IViewFactory {
    ILoginView createLoginView(LoginController controller);
    IRegistrationView createRegistrationView(RegistrationController controller);
    ICollectorHPView createCollectorHomePageView(CollectorHPController controller);
    IStoreHPView createStoreHomePageView(StoreHPController controller);
}
