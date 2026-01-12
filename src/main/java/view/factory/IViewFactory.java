package view.factory;

import controller.CollectionController;
import controller.CollectorHPController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHPController;
import controller.TradeController;
import view.collection.ICollectionView;
import view.collectorhomepage.ICollectorHPView;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHPView;
import view.trade.ITradeView;

public interface IViewFactory {
    ILoginView createLoginView(LoginController controller);
    IRegistrationView createRegistrationView(RegistrationController controller);
    ICollectorHPView createCollectorHomePageView(CollectorHPController controller);
    IStoreHPView createStoreHomePageView(StoreHPController controller);
    ICollectionView createCollectionView(CollectionController controller);
    ITradeView createTradeView(TradeController controller);
}
