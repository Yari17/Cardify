package view;

import model.bean.CardBean;

import java.util.List;

/**
 * Interfaccia per la Home Page dedicata ai collezionisti.
 * Gestisce la navigazione tra i set, la ricerca delle carte e la
 * visualizzazione dei dettagli.
 */
public interface ICollectorHPView extends IView {
    /** Mostra la lista delle carte all'utente. */
    void displayCardList(List<CardBean> cardList);

    /**
     * Restituisce il filtro sul nome della carta inserito dall'utente.
     * 
     * @return Il filtro testuale o null/vuoto se non applicato.
     */
    String getCardNameFilter();

    /**
     * Restituisce l'ID del set selezionato per il filtraggio.
     * 
     * @return L'ID del set o null se non selezionato.
     */
    String getSetFilter();

    /**
     * Mostra i dettagli di una carta con l'opzione di proporre uno scambio.
     * 
     * @param card La carta di cui mostrare i dettagli.
     */
    void displayCardOverview(CardBean card);

    /**
     * Avvia il flusso di creazione di una proposta di scambio per la carta
     * specificata.
     * 
     * @param card La carta target per lo scambio.
     */
    void onProposeTrade(CardBean card);
}
