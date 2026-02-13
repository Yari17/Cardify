package view;

import model.bean.TradeSessionBean;

/**
 * Interfaccia per la gestione delle sessioni di scambio lato Negozio (Store).
 * Include metodi per la validazione dei codici dei partecipanti e l'esito
 * dell'ispezione delle carte.
 */
public interface IStoreTradeView extends IView {
    /**
     * Mostra i dettagli della sessione utilizzando un Bean per il disaccoppiamento.
     */
    void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName);

    /**
     * Registra e valida un codice sessione inserito da un partecipante.
     * 
     * @param code Il codice numerico da validare.
     */
    void registerCodeValidation(int code);

    /** Registra l'esito positivo dell'ispezione fisica delle carte. */
    void registerInspectionSuccess();

    /**
     * Registra l'esito negativo dell'ispezione (es. carte danneggiate o mancanti).
     */
    void registerInspectionFail();

    /**
     * Finalizza il trasferimento di proprietà delle carte dopo la validazione
     * completa.
     */
    void onFinalizeTrade();
}
