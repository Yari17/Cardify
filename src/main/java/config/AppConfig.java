package config;

import model.domain.enumerations.PersistenceType;
import model.domain.enumerations.ViewType;

/**
 * Gestisce le configurazioni globali dell'applicazione Cardify.
 * Fornisce costanti e metodi statici per centralizzare la gestione dei tipi di
 * utente,
 * dei giochi supportati e della modalità di persistenza/UI corrente.
 */
public final class AppConfig {

    private AppConfig() {
        // costruttore privato poichè l'istanziazione non è necessaria
    }

    public static final String USER_TYPE_COLLECTOR = "Collezionista";
    public static final String USER_TYPE_STORE = "Store";

    private static PersistenceType currentPersistenceType = PersistenceType.JDBC;
    private static PersistenceType binderPersistenceType = null; // Sovrascrittura personalizzata
    private static ViewType currentUiType = ViewType.JAVAFX;

    public static void setPersistenceType(PersistenceType type) {
        currentPersistenceType = type;
    }

    // Logica di toggle per la persistenza del Binder
    public static void setBinderPersistenceType(PersistenceType type) {
        binderPersistenceType = type;
    }

    public static PersistenceType getBinderPersistenceType() {
        // Se è impostato un override, usalo; altrimenti fallback al globale
        return binderPersistenceType != null ? binderPersistenceType : currentPersistenceType;
    }

    public static void setUiType(ViewType uiType) {
        currentUiType = uiType;
    }

    public static PersistenceType getPersistenceType() {
        return currentPersistenceType;
    }

    public static ViewType getUiType() {
        return currentUiType;
    }

}
