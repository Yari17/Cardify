package view.collection;

import model.domain.Binder;
import view.IView;

import java.util.List;
import java.util.Map;

/**
 * Interface for Collection View implementations.
 * Defines all operations needed to display and manage a user's card collection,
 * decoupled from the specific presentation layer (CLI, JavaFX, etc.).
 */
public interface ICollectionView extends IView {

    /**
     * Sets the welcome message for the user.
     *
     * @param username the username to display
     */
    void setWelcomeMessage(String username);

    /**
     * Displays the user's collection organized by sets.
     * The controller is responsible for obtaining persistence data and passes
     * both the binders and a map of setId->cards for rendering. The view must
     * not call DAOs directly.
     *
     * @param bindersBySet map of setId to Binder containing the user's collection
     * @param setCardsMap map of setId to list of Card instances (from persistence)
     */
    void displayCollection(Map<String, Binder> bindersBySet, Map<String, List<model.domain.Card>> setCardsMap);

    /**
     * Updates a single card in the UI without full refresh.
     *
     * @param setId  the set identifier
     * @param cardId the card identifier to update
     */
    void updateCardInSet(String setId, String cardId);

    /**
     * Shows or hides the save button.
     *
     * @param visible true to show, false to hide
     */
    void setSaveButtonVisible(boolean visible);

    /**
     * Shows a success message to the user.
     *
     * @param message the success message
     */
    void showSuccess(String message);

    /**
     * Shows an error message to the user.
     *
     * @param message the error message
     */
    void showError(String message);
}
