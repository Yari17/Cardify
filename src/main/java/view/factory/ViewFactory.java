package view.factory;

import model.domain.enumerations.ViewType;
import view.ICollectionView;
import view.ICollectorHPView;
import view.ILoginView;
import view.IStoreHPView;
import view.ITradeProposalView;
import view.ICollectorTradeView;
import view.IStoreTradeView;
import view.IManageProposalsView;
import view.INotificationsView;

/**
 * Factory astratta per la creazione dei componenti della vista.
 * Implementa il pattern Abstract Factory per permettere lo switch tra diverse
 * tecnologie di interfaccia (CLI, JavaFX) mantenendo il disaccoppiamento dai
 * controller.
 */
public abstract class ViewFactory {
    /**
     * Ritorna l'istanza della factory concreta in base al tipo di vista richiesto.
     * 
     * @param viewType Il tipo di interfaccia da istanziare (CLI o JAVAFX).
     * @return Una sottoclasse concreta di ViewFactory.
     */
    public static ViewFactory getFactory(ViewType viewType) {
        return switch (viewType) {
            case JAVAFX -> new FXViewFactory();
            case CLI -> new CliViewFactory();
        };
    }

    /** @return La vista di login e registrazione. */
    public abstract ILoginView getLoginView();

    /** @return La home page per gli utenti collezionisti. */
    public abstract ICollectorHPView getCollectorHPView();

    /** @return La dashboard per gli utenti store (negozi). */
    public abstract IStoreHPView getStoreHPView();

    /**
     * @return La vista per la gestione della collezione personale e dei
     *         raccoglitori.
     */
    public abstract ICollectionView getCollectionView();

    /** @return La vista per la creazione di una nuova proposta di scambio. */
    public abstract ITradeProposalView getTradeProposalView();

    /** @return La vista per monitorare gli scambi lato collezionista. */
    public abstract ICollectorTradeView getCollectorTradeView();

    /**
     * @return La vista per la gestione degli scambi lato store (verifica codici,
     *         ispezione).
     */
    public abstract IStoreTradeView getStoreTradeView();

    /** @return La vista per gestire l'accettazione o il rifiuto delle proposte. */
    public abstract IManageProposalsView getManageProposalsView();

    /** @return La vista per la consultazione delle notifiche di sistema. */
    public abstract INotificationsView getNotificationsView();
}
