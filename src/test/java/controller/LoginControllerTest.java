package controller;

import model.bean.UserBean;
import model.dao.IUserDao;
import model.domain.User;
import model.domain.enumerations.PersistenceType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    static class FakeUserDao implements IUserDao {
        private final Map<String, User> users = new HashMap<>();
        private final Map<String, String> passwords = new HashMap<>();

        void seedUser(String username, String password, String userType) {
            User u = new User(username, 0, 0);
            u.setUserType(userType);
            users.put(username, u);
            passwords.put(username, password);
        }

        @Override
        public Optional<User> get(long id) { return Optional.empty(); }

        @Override
        public void save(User user) { users.put(user.getName(), user); }

        @Override
        public void update(User user, String[] params) { /* not used */ }

        @Override
        public void delete(User user) { users.remove(user.getName()); }

        @Override
        public Optional<User> findByName(String name) { return Optional.ofNullable(users.get(name)); }

        @Override
        public boolean authenticate(String username, String password) {
            String pw = passwords.get(username);
            return pw != null && Objects.equals(pw, password);
        }

        @Override
        public Optional<User> authenticateAndGetUser(String username, String password) {
            if (authenticate(username, password)) return Optional.of(users.get(username));
            return Optional.empty();
        }

        @Override
        public void register(String username, String password, String userType) {
            if (users.containsKey(username)) throw new IllegalArgumentException("User already exists");
            User u = new User(username, 0, 0);
            u.setUserType(userType);
            users.put(username, u);
            passwords.put(username, password);
        }

        @Override
        public List<String> findAllUsernames() { return new ArrayList<>(users.keySet()); }
    }

    static class FakeNavController extends ApplicationController {
        public boolean navigated = false;
        public UserBean lastUser = null;

        @Override
        public void handleRoleBasedNavigation(UserBean user) {
            this.navigated = true;
            this.lastUser = user;
        }

        @Override
        public void navigateToRegistration() { /* not used */}

        @Override
        public void navigateToRegistrationWithDao(IUserDao dao) { /* not used */}

        @Override
        public void navigateToLogin() {/* not used */ }
    }

    static class FakeLoginView implements view.ILoginView {
        private final String username;
        private final String password;
        public boolean closed = false;
        public String lastSuccess = null;
        public String lastInputError = null;
        public String lastError = null;
        private PersistenceType persistenceType = null;

        FakeLoginView(String username, String password) { this.username = username; this.password = password; }

        @Override
        public model.bean.UserBean getUserCredentials() {
            return new model.bean.UserBean(username, password);
        }

        @Override
        public PersistenceType getPersistenceType() { return persistenceType; }

        public void setPersistenceType(PersistenceType p) { this.persistenceType = p; }

        @Override
        public void showInputError(String message) { this.lastInputError = message; }

        @Override
        public void showSuccess(String message) { this.lastSuccess = message; }

        @Override
        public void setController(controller.LoginController controller) { /* not used */}

        @Override
        public void display() { /* not used */}

        @Override
        public void close() { this.closed = true; }

        @Override
        public void showError(String errorMessage) { this.lastError = errorMessage; }

        @Override
        public void refresh() { /* not used */}
    }

    // Verifica che un login valido chiuda la view e invochi la navigazione basata sul ruolo
    @Test
    void successfulLogin_callsNavigationAndClosesView() {
        FakeUserDao dao = new FakeUserDao();
        dao.seedUser("alice", "password", config.AppConfig.USER_TYPE_COLLECTOR);
        FakeNavController nav = new FakeNavController();
        LoginController lc = new LoginController(dao, nav);

        FakeLoginView view = new FakeLoginView("alice", "password");
        lc.setView(view);

        lc.onLoginRequested();

        assertTrue(view.closed, "View should be closed after successful login");
        assertNotNull(view.lastSuccess);
        assertTrue(nav.navigated, "Navigation should be triggered after successful login");
        assertNotNull(nav.lastUser);
        assertEquals("alice", nav.lastUser.getUsername());
    }

    // Verifica che credenziali non valide mostrino un errore di input e non navighino
    @Test
    void invalidCredentials_showInputError() {
        FakeUserDao dao = new FakeUserDao();
        dao.seedUser("bob", "secret", config.AppConfig.USER_TYPE_COLLECTOR);
        FakeNavController nav = new FakeNavController();
        LoginController lc = new LoginController(dao, nav);

        FakeLoginView view = new FakeLoginView("bob", "wrongpass");
        lc.setView(view);

        lc.onLoginRequested();

        assertNull(view.lastSuccess);
        assertNotNull(view.lastInputError);
        assertFalse(nav.navigated);
    }
}
