package model.dao;

import model.domain.TradeSession;
import model.domain.User;
import java.util.List;

/**
 * Interfaccia per la gestione delle sessioni di scambio fisico (Trade
 * Sessions).
 * Coordina le operazioni effettuate presso lo Store, monitorando arrivi e
 * ispezioni.
 */
public interface ITradeSessionDao {
    /**
     * Recupera una sessione di scambio tramite il suo ID univoco.
     * 
     * @param id ID della sessione.
     * @return La sessione {@link TradeSession} trovata o null.
     */
    TradeSession getTradeSessionById(int id);

    /**
     * Recupera tutte le transazioni di scambio attive per un determinato utente
     * (proponente o ricevente).
     * 
     * @param user L'utente coinvolto nello scambio.
     * @return Lista di sessioni di scambio attive.
     */
    List<TradeSession> getUserTradeSessions(User user);

    /**
     * Ricerca le transazioni pianificate per un negozio specifico, filtrando per ID
     * se fornito.
     * 
     * @param user    L'utente {@link User} con profilo STORE.
     * @return Lista di sessioni pianificate presso il negozio.
     */
    List<TradeSession> getStoreScheduledTrades(User user);

    /**
     * Recupera le sessioni attualmente in corso (partecipanti presenti) presso un
     * negozio.
     * 
     * @param user L'utente STORE titolare.
     * @return Lista di sessioni in fase di ispezione o verifica.
     */
    List<TradeSession> getStoreInProgressTrades(User user);

    /**
     * Recupera lo storico degli scambi processati e chiusi da un negozio.
     * 
     * @param user L'utente STORE titolare.
     * @return Lista di sessioni completate presso il negozio.
     */
    List<TradeSession> getStoreCompletedTrades(User user);

    /**
     * Aggiorna lo stato di avanzamento di una sessione (es. conferma arrivo, esito
     * ispezione).
     * 
     * @param tradeSession La sessione con i dati aggiornati.
     */
    void updateTradeSessionStatus(TradeSession tradeSession);

    /**
     * Salva una nuova sessione di scambio nel sistema di persistenza.
     * 
     * @param tradeSession La sessione da creare.
     */
    void saveTradeSession(TradeSession tradeSession);

}
