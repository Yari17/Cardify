package view.factory;

import view.*;
import view.INotificationsView;
import view.ITradeProposalView;
import view.cli.CliCollectionView;
import view.cli.CliCollectorHPView;
import view.cli.CliCollectorTradeView;
import view.cli.CliLoginView;
import view.cli.CliStoreHPView;
import view.cli.CliStoreTradeView;
import view.cli.CliManageProposalsView;
import view.cli.CliNotificationsView;

/**
 * Implementazione concreta della factory per l'interfaccia a riga di comando
 * (CLI).
 * Istanzia i componenti della vista basati su console e interazione testuale.
 */
public class CliViewFactory extends ViewFactory {
    public ILoginView getLoginView() {
        return new CliLoginView();
    }

    @Override
    public ICollectorHPView getCollectorHPView() {
        return new CliCollectorHPView();
    }

    @Override
    public IStoreHPView getStoreHPView() {
        return new CliStoreHPView();
    }

    @Override
    public ICollectionView getCollectionView() {
        return new CliCollectionView();
    }

    @Override
    public ITradeProposalView getTradeProposalView() {
        return new view.cli.CliTradeProposalView();
    }

    @Override
    public view.ICollectorTradeView getCollectorTradeView() {
        return new CliCollectorTradeView();
    }

    @Override
    public view.IStoreTradeView getStoreTradeView() {
        return new CliStoreTradeView();
    }

    @Override
    public IManageProposalsView getManageProposalsView() {
        return new CliManageProposalsView();
    }

    @Override
    public INotificationsView getNotificationsView() {
        return new CliNotificationsView();
    }

}
