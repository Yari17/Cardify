package view.factory;

import controller.CollectorHomePageController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHomePageController;
import view.collectorhomepage.ICollectorHomePageView;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHomePageView;

public interface IViewFactory {
    ILoginView createLoginView(LoginController controller);
    IRegistrationView createRegistrationView(RegistrationController controller);
    ICollectorHomePageView createCollectorHomePageView(CollectorHomePageController controller);
    IStoreHomePageView createStoreHomePageView(StoreHomePageController controller);
}
