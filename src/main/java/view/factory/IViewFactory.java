package view.factory;

import controller.*;
import view.collection.ICollectionView;
import view.collectorhomepage.ICollectorHPView;
import view.login.ILoginView;
import view.managetrade.IManageTradeView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHPView;
import view.trade.ILiveTradeView;
import view.negotiation.INegotiationView;

public interface IViewFactory {
    ILoginView createLoginView(LoginController controller);
    IRegistrationView createRegistrationView(RegistrationController controller);
    ICollectorHPView createCollectorHomePageView(CollectorHPController controller);
    IStoreHPView createStoreHomePageView(StoreHPController controller);
    ICollectionView createCollectionView(CollectionController controller);
    ILiveTradeView createTradeView(LiveTradeController controller);
    INegotiationView createNegotiationView(NegotiationController controller);
    IManageTradeView createManageTradeView(ManageTradeController controller);
}


