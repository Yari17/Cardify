package controller;

import model.dao.demo.DemoUserDao;
import model.domain.User;
import view.ILoginView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per LoginController usando JUnit.
 * Usa DemoUserDao reale e stub view.
 */
class LoginControllerTest {

    private LoginController controller;
    private StubLoginView stubView;
    private DemoUserDao demoUserDao;

    // Stub semplice per ILoginView
    private static class StubLoginView implements ILoginView {
        String usernameInput = "";
        String passwordInput = "";
        String lastError = null;
        boolean loginSuccessShown = false;
        boolean loginFailureShown = false;
        boolean registrationSuccessShown = false;
        String lastFailureMessage = null;

        @Override
        public void display() {
            // Stub per test
        }

        @Override
        public void setController(Object controller) {
            // Stub per test
        }

        @Override
        public void close() {
            // Stub per test
        }

        @Override
        public void refresh() {
            // Stub per test
        }

        @Override
        public String getUsername() {
            return usernameInput;
        }

        @Override
        public String getPassword() {
            return passwordInput;
        }

        @Override
        public void showError(String message) {
            this.lastError = message;
        }

        @Override
        public void showLoginSuccess() {
            this.loginSuccessShown = true;
        }

        @Override
        public void showLoginFailure(String message) {
            this.loginFailureShown = true;
            this.lastFailureMessage = message;
        }

        @Override
        public void showRegistrationSuccess() {
            this.registrationSuccessShown = true;
        }

        @Override
        public void showRegistrationFailure(String message) {
            // Stub per test
        }
    }

    @BeforeEach
    void setUp() {
        stubView = new StubLoginView();
        demoUserDao = new DemoUserDao();

        // Crea controller senza ApplicationController (non necessario per questi test)
        controller = new LoginController(null, demoUserDao);
        controller.setView(stubView);
    }

    @Test
    void testHandleLoginConCredenzialiValide() {
        // Usa utenti pre-registrati in DemoUserDao
        stubView.usernameInput = "collector1";
        stubView.passwordInput = "password";

        controller.handleLogin();

        assertTrue(stubView.loginSuccessShown);
    }

    @Test
    void testHandleLoginConCredenzialiNonValide() {
        stubView.usernameInput = "wronguser";
        stubView.passwordInput = "wrongpass";

        controller.handleLogin();

        assertTrue(stubView.loginFailureShown);
        assertEquals("Credenziali non valide", stubView.lastFailureMessage);
        assertFalse(stubView.loginSuccessShown);
    }

    @Test
    void testHandleLoginConCampiVuoti() {
        stubView.usernameInput = "";
        stubView.passwordInput = "";

        controller.handleLogin();

        assertEquals("Campo vuoto: inserisci username e password", stubView.lastError);
        assertFalse(stubView.loginSuccessShown);
    }

    @Test
    void testHandleRegistrationConDatiValidi() {
        stubView.usernameInput = "newuser";
        stubView.passwordInput = "newpass";

        controller.handleRegistrationAsCollector();

        assertTrue(stubView.registrationSuccessShown);

        // Verifica che utente sia stato registrato
        User registeredUser = demoUserDao.authenticateAndGetUser("newuser", "newpass");
        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
    }

    @Test
    void testHandleRegistrationConCampiVuoti() {
        stubView.usernameInput = "";
        stubView.passwordInput = "";

        controller.handleRegistrationAsStore();

        assertEquals("Campo vuoto: inserisci username e password", stubView.lastError);
        assertFalse(stubView.registrationSuccessShown);
    }
}
