package controller;

import model.dao.IUserDao;
import model.domain.User;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;



class RegistrationControllerTest {

    static class FakeUserDao implements IUserDao {
        private final Map<String, User> users = new HashMap<>();
        private final Map<String, String> passwords = new HashMap<>();

        @Override
        public Optional<User> get(long id) { return Optional.empty(); }

        @Override
        public void save(User user) { users.put(user.getName(), user); }

        @Override
        public void update(User user, String[] params) { /* not used */}

        @Override
        public void delete(User user) { users.remove(user.getName()); }

        @Override
        public Optional<User> findByName(String name) { return Optional.ofNullable(users.get(name)); }

        @Override
        public boolean authenticate(String username, String password) { return false; }

        @Override
        public Optional<User> authenticateAndGetUser(String username, String password) { return Optional.empty(); }

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

    static class FakeRegView implements view.IRegistrationView {
        private final String username;
        private final String password;
        private final String userType;
        public String lastSuccess = null;
        public String lastInputError = null;
        FakeRegView(String username, String password, String userType) {
            this.username = username; this.password = password; this.userType = userType;
        }

        @Override
        public model.bean.UserBean getUserData() {
            return new model.bean.UserBean(username, password, userType);
        }

        @Override
        public void showInputError(String message) { this.lastInputError = message; }

        @Override
        public void showSuccess(String message) { this.lastSuccess = message; }

        @Override
        public void setController(RegistrationController controller) { /* not used */}

        @Override
        public void showError(String errorMessage) { /* not used */}

        @Override
        public void display() {
            /* not used */
        }

        @Override
        public void close() {/* not used */ }

        @Override
        public void refresh() { /* not used */}

        @Override
        public model.domain.enumerations.PersistenceType getPersistenceType() { return null; }
    }

    static class FakeNav extends ApplicationController {
        public boolean navigatedToLogin = false;
        @Override
        public void navigateToLogin() { navigatedToLogin = true; }
    }

    // Verifica che una registrazione valida ritorni al login e persista l'utente
    @Test
    void successfulRegistration_invokesNavigationToLogin() {
        FakeUserDao dao = new FakeUserDao();
        FakeNav nav = new FakeNav();
        RegistrationController rc = new RegistrationController(dao, nav);

        FakeRegView v = new FakeRegView("newuser", "pwd123", config.AppConfig.USER_TYPE_COLLECTOR);
        rc.setView(v);

        rc.onRegisterRequested();

        assertNotNull(v.lastSuccess);
        assertTrue(nav.navigatedToLogin, "Should navigate back to login after successful registration");
        assertTrue(dao.findAllUsernames().contains("newuser"));
    }

    // Verifica che una registrazione duplicata mostri un errore di input
    @Test
    void duplicateRegistration_showsInputError() {
        FakeUserDao dao = new FakeUserDao();
        
        dao.register("existing", "pwd", config.AppConfig.USER_TYPE_COLLECTOR);
        FakeNav nav = new FakeNav();
        RegistrationController rc = new RegistrationController(dao, nav);

        FakeRegView v = new FakeRegView("existing", "pwd2", config.AppConfig.USER_TYPE_COLLECTOR);
        rc.setView(v);

        rc.onRegisterRequested();

        assertNull(v.lastSuccess);
        assertNotNull(v.lastInputError);
    }
}
