package view.factory;

import controller.*;
import config.InputManager;
import view.cli.CliCollectionView;
import view.ICollectionView;
import view.cli.CliCollectorHPView;
import view.ICollectorHPView;
import view.cli.CliILoginView;
import view.ILoginView;
import view.cli.CliManageTradeView;
import view.IManageTradeView;
import view.cli.CliRegistrationView;
import view.IRegistrationView;
import view.cli.CliStoreHPView;
import view.IStoreHPView;
import view.cli.CliLiveTradeView;
import view.ILiveTradeView;
import view.cli.CliNegotiationView;
import view.INegotiationView;

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

    @Override
    public ICollectionView createCollectionView(CollectionController controller) {
        CliCollectionView view = new CliCollectionView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public ILiveTradeView createTradeView(LiveTradeController controller) {
        CliLiveTradeView view = new CliLiveTradeView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public INegotiationView createNegotiationView(NegotiationController controller) {
        CliNegotiationView view = new CliNegotiationView(inputManager);
        view.setController(controller);
        return view;
    }

    @Override
    public IManageTradeView createManageTradeView(ManageTradeController controller) {
        CliManageTradeView view = new CliManageTradeView();
        view.setManageController(controller);
        // register callbacks
        view.registerOnAccept(controller::acceptProposal);
        view.registerOnDecline(controller::declineProposal);
        view.registerOnCancel(controller::declineProposal);
        view.registerOnTradeClick(controller::initiateTrade);
        view.registerOnTradeNowClick(controller::initiateTrade);
        return view;
    }
}
