package controller;

import model.bean.UserBean;
import model.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import view.registration.IRegistrationView;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private IRegistrationView mockView;

    @Mock
    private UserDao mockUserDao;

    @Mock
    private NavigationController mockNavigationController;

    private RegistrationController registrationController;

    @BeforeEach
    void setUp() {
        registrationController = new RegistrationController(null, mockUserDao, mockNavigationController);
        registrationController.setView(mockView);
    }

    @Test
    void testOnRegisterRequested_ValidCollectorData_RegistersSuccessfully() {
        
        UserBean userBean = new UserBean("newuser", "password123", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);
        doNothing().when(mockUserDao).register(anyString(), anyString(), anyString());

        
        registrationController.onRegisterRequested();

        
        verify(mockUserDao).register("newuser", "password123", UserBean.USER_TYPE_COLLECTOR);
        verify(mockView).showSuccess(contains("Registrazione completata"));
        verify(mockNavigationController).navigateToLogin();
    }

    @Test
    void testOnRegisterRequested_ValidStoreData_RegistersSuccessfully() {
        
        UserBean userBean = new UserBean("newstore", "storepass123", UserBean.USER_TYPE_STORE);
        when(mockView.getUserData()).thenReturn(userBean);
        doNothing().when(mockUserDao).register(anyString(), anyString(), anyString());

        
        registrationController.onRegisterRequested();

        
        verify(mockUserDao).register("newstore", "storepass123", UserBean.USER_TYPE_STORE);
        verify(mockView).showSuccess(contains("Registrazione completata"));
        verify(mockNavigationController).navigateToLogin();
    }

    @Test
    void testOnRegisterRequested_DuplicateUsername_ShowsError() {
        
        UserBean userBean = new UserBean("existinguser", "password123", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);
        doThrow(new IllegalArgumentException("Username già esistente"))
            .when(mockUserDao).register(anyString(), anyString(), anyString());

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError("Username già esistente");
        verify(mockNavigationController, never()).navigateToLogin();
    }

    @Test
    void testOnRegisterRequested_EmptyUsername_ShowsValidationError() {
        
        UserBean userBean = new UserBean("", "password123", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("username"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_ShortUsername_ShowsValidationError() {
        
        UserBean userBean = new UserBean("ab", "password123", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("almeno 3 caratteri"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_ShortPassword_ShowsValidationError() {
        
        UserBean userBean = new UserBean("validuser", "12345", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("almeno 6 caratteri"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_EmptyPassword_ShowsValidationError() {
        
        UserBean userBean = new UserBean("validuser", "", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("password"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_InvalidUserType_ShowsValidationError() {
        
        UserBean userBean = new UserBean("validuser", "password123");
        userBean.setUserType("InvalidType");
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("tipo di utente"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_NullUserType_ShowsValidationError() {
        
        UserBean userBean = new UserBean("validuser", "password123");
        userBean.setUserType(null);
        when(mockView.getUserData()).thenReturn(userBean);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError(contains("tipo di utente"));
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_NullUserBean_ShowsError() {
        
        when(mockView.getUserData()).thenReturn(null);

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError("Impossibile recuperare i dati utente");
        verify(mockUserDao, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void testOnRegisterRequested_UnexpectedException_ShowsGenericError() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_COLLECTOR);
        when(mockView.getUserData()).thenReturn(userBean);
        doThrow(new RuntimeException("Unexpected database error"))
            .when(mockUserDao).register(anyString(), anyString(), anyString());

        
        registrationController.onRegisterRequested();

        
        verify(mockView).showInputError("Si è verificato un errore. Riprova.");
        verify(mockNavigationController, never()).navigateToLogin();
    }

    @Test
    void testOnBackToLoginRequested_NavigatesToLogin() {
        
        registrationController.onBackToLoginRequested();

        
        verify(mockNavigationController).navigateToLogin();
    }
}
