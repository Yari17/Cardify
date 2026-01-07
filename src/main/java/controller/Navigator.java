package controller;

import view.IView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Navigator gestisce la navigazione tra le viste dell'applicazione.
 * Mantiene traccia della vista corrente e gestisce il ciclo di vita delle viste.
 */
public class Navigator {
    private static final Logger LOGGER = Logger.getLogger(Navigator.class.getName());
    private IView currentView;

    /**
     * Naviga verso una nuova vista, chiudendo quella corrente se presente.
     *
     * @param newView la nuova vista da visualizzare
     */
    public void navigateTo(IView newView) {
        if (currentView != null) {
            closeCurrentView();
        }
        currentView = newView;
        displayCurrentView();
    }

    /**
     * Visualizza la vista corrente.
     */
    private void displayCurrentView() {
        if (currentView != null) {
            try {
                currentView.display();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error displaying view", e);
            }
        }
    }

    /**
     * Chiude la vista corrente.
     */
    private void closeCurrentView() {
        if (currentView != null) {
            try {
                currentView.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing view", e);
            }
        }
    }

    /**
     * Chiude tutte le viste e pulisce le risorse.
     */
    public void closeAll() {
        closeCurrentView();
        currentView = null;
    }

    /**
     * Ottiene la vista corrente.
     *
     * @return la vista corrente o null se non presente
     */
    public IView getCurrentView() {
        return currentView;
    }
}

