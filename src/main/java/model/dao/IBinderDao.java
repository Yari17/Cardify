package model.dao;

import model.domain.Binder;

import java.util.List;

/**
 * Interface for Binder Data Access Object.
 * Provides methods to manage user collections organized by sets (binders).
 * Each binder represents a set of cards that a collector owns.
 *
 * This interface extends IDao to inherit basic CRUD operations and adds
 * binder-specific functionality decoupled from the presentation layer.
 */
public interface IBinderDao extends IDao<Binder> {

    /**
     * Retrieves all binders owned by a specific user.
     *
     * @param owner the username of the binder owner
     * @return list of binders owned by the user
     */
    List<Binder> getUserBinders(String owner);

    /**
     * Creates a new binder for a user with the specified set information.
     *
     * @param owner the username of the binder owner
     * @param setId the unique identifier of the card set
     * @param setName the display name of the card set
     */
    void createBinder(String owner, String setId, String setName);

    /**
     * Deletes a binder identified by its unique identifier.
     *
     * @param binderId the unique identifier of the binder to delete
     */
    void deleteBinder(String binderId);
}
