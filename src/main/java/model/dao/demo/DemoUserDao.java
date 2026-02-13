package model.dao.demo;

import exception.UserAlreadyExistsException;
import model.dao.IUserDao;
import model.domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione in-memory del DAO per gli utenti (modalità Demo).
 * Simula un database utilizzando una lista caricata con utenti predefiniti per
 * test rapidi.
 */
public class DemoUserDao implements IUserDao {
    /** Cache locale degli utenti simulati. */
    private List<User> cachedUsers;
    private static final String DEFAULT_PASSWORD = "password";

    /**
     * Inizializza il DAO popolando la cache con utenti di test (collector e store).
     */
    public DemoUserDao() {
        cachedUsers = new ArrayList<>();
        // Utenti pre-registrati per facilitare i test dell'interfaccia
        cachedUsers.add(new User("collector1", DEFAULT_PASSWORD, "Collezionista"));
        cachedUsers.add(new User("collector2", DEFAULT_PASSWORD, "Collezionista"));
        cachedUsers.add(new User("store1", DEFAULT_PASSWORD, "Store"));
        cachedUsers.add(new User("FrancescoTotti10", "DAJEROMADAJE", "Collezionista"));
    }

    @Override
    public User authenticateAndGetUser(String username, String password) {
        for (User u : cachedUsers) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                // return the actual User instance stored in memory so callers can keep
                // reference
                return u;
            }
        }
        return null;
    }

    @Override
    public void register(User user) {
        for (User u : cachedUsers) {
            if (u.getUsername().equals(user.getUsername())) {
                throw new UserAlreadyExistsException("User with username " + user.getUsername() + " already exists");
            }
        }
        // Clone/create a new user to avoid external modification issues in a real
        // scenario
        // (though in demo it might not matter as much, keeping consistent with creating
        // a new entry)
        User newUser = new User(user.getUsername(), user.getPassword(), user.getUserType());
        cachedUsers.add(newUser);
    }

    @Override
    public List<User> getStores() {
        List<User> storeList = new ArrayList<>();
        for (User u : cachedUsers) {
            if ("STORE".equalsIgnoreCase(u.getUserType())) {
                storeList.add(u);
            }
        }
        return storeList;
    }
}
