package view.factory;

import controller.*;
import view.ICollectionView;
import view.ICollectorHPView;
import view.ILoginView;
import view.IManageTradeView;
import view.IRegistrationView;
import view.IStoreHPView;
import view.ILiveTradeView;
import view.INegotiationView;

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


