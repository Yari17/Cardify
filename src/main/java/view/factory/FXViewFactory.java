package view.factory;

import view.ICollectionView;
import view.ICollectorHPView;
import view.ILoginView;
import view.IStoreHPView;
import view.ITradeProposalView;
import view.IManageProposalsView;
import view.INotificationsView;
import view.javafx.FXCollectionView;
import view.javafx.FXCollectorHPView;
import view.javafx.FXLoginView;
import view.javafx.FXStoreHPView;
import view.javafx.FXTradeProposalView;
import view.javafx.FXManageProposalsView;
import view.javafx.FXNotificationsView;

/**
 * Implementazione concreta della factory per l'interfaccia grafica JavaFX.
 * Istanzia i componenti della vista basati su finestre, scene e controlli
 * grafici.
 */
public class FXViewFactory extends ViewFactory {
    public ILoginView getLoginView() {
        return new FXLoginView();
    }

    @Override
    public ICollectorHPView getCollectorHPView() {
        return new FXCollectorHPView();
    }

    @Override
    public IStoreHPView getStoreHPView() {
        return new FXStoreHPView();
    }

    @Override
    public ICollectionView getCollectionView() {
        return new FXCollectionView();
    }

    @Override
    public ITradeProposalView getTradeProposalView() {
        return new FXTradeProposalView();
    }

    @Override
    public view.ICollectorTradeView getCollectorTradeView() {
        return new view.javafx.FXCollectorTradeView();
    }

    @Override
    public view.IStoreTradeView getStoreTradeView() {
        return new view.javafx.FXStoreTradeView();
    }

    @Override
    public IManageProposalsView getManageProposalsView() {
        return new FXManageProposalsView();
    }

    @Override
    public INotificationsView getNotificationsView() {
        return new FXNotificationsView();
    }
}
