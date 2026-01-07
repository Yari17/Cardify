package model.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserBeanTest {

    @Test
    void testValidCollectorUserBean_IsValid() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertTrue(userBean.isValid());
        assertNull(userBean.getValidationError());
    }

    @Test
    void testValidStoreUserBean_IsValid() {
        
        UserBean userBean = new UserBean("storeuser", "storepass123", UserBean.USER_TYPE_STORE);

        
        assertTrue(userBean.isValid());
        assertNull(userBean.getValidationError());
    }

    @Test
    void testEmptyUsername_IsInvalid() {
        
        UserBean userBean = new UserBean("", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertNotNull(userBean.getValidationError());
        assertTrue(userBean.getValidationError().contains("username"));
    }

    @Test
    void testShortUsername_IsInvalid() {
        
        UserBean userBean = new UserBean("ab", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertEquals("L'username deve contenere almeno 3 caratteri", userBean.getValidationError());
    }

    @Test
    void testEmptyPassword_IsInvalid() {
        
        UserBean userBean = new UserBean("validuser", "", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertTrue(userBean.getValidationError().contains("password"));
    }

    @Test
    void testShortPassword_IsInvalid() {
        
        UserBean userBean = new UserBean("validuser", "12345", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertEquals("La password deve contenere almeno 6 caratteri", userBean.getValidationError());
    }

    @Test
    void testInvalidUserType_IsInvalid() {
        
        UserBean userBean = new UserBean("validuser", "password123");
        userBean.setUserType("InvalidType");

        
        assertFalse(userBean.isValid());
        assertTrue(userBean.getValidationError().contains("tipo di utente"));
    }

    @Test
    void testNullUserType_IsInvalid() {
        
        UserBean userBean = new UserBean("validuser", "password123");
        userBean.setUserType(null);

        
        assertFalse(userBean.isValid());
        assertTrue(userBean.getValidationError().contains("tipo di utente"));
    }

    @Test
    void testNullUsername_IsInvalid() {
        
        UserBean userBean = new UserBean(null, "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertTrue(userBean.getValidationError().contains("username"));
    }

    @Test
    void testNullPassword_IsInvalid() {
        
        UserBean userBean = new UserBean("validuser", null, UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isValid());
        assertTrue(userBean.getValidationError().contains("password"));
    }

    @Test
    void testIsUsernameValid_ValidUsername_ReturnsTrue() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertTrue(userBean.isUsernameValid());
    }

    @Test
    void testIsUsernameValid_ShortUsername_ReturnsFalse() {
        
        UserBean userBean = new UserBean("ab", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isUsernameValid());
    }

    @Test
    void testIsPasswordValid_ValidPassword_ReturnsTrue() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertTrue(userBean.isPasswordValid());
    }

    @Test
    void testIsPasswordValid_ShortPassword_ReturnsFalse() {
        
        UserBean userBean = new UserBean("validuser", "12345", UserBean.USER_TYPE_COLLECTOR);

        
        assertFalse(userBean.isPasswordValid());
    }

    @Test
    void testIsUserTypeValid_CollectorType_ReturnsTrue() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_COLLECTOR);

        
        assertTrue(userBean.isUserTypeValid());
    }

    @Test
    void testIsUserTypeValid_StoreType_ReturnsTrue() {
        
        UserBean userBean = new UserBean("validuser", "password123", UserBean.USER_TYPE_STORE);

        
        assertTrue(userBean.isUserTypeValid());
    }

    @Test
    void testIsUserTypeValid_InvalidType_ReturnsFalse() {
        
        UserBean userBean = new UserBean("validuser", "password123");
        userBean.setUserType("InvalidType");

        
        assertFalse(userBean.isUserTypeValid());
    }

    @Test
    void testToString_DoesNotContainPassword() {
        
        UserBean userBean = new UserBean("testuser", "secretpassword", UserBean.USER_TYPE_COLLECTOR);

        
        String toString = userBean.toString();
        assertTrue(toString.contains("testuser"));
        assertFalse(toString.contains("secretpassword"));
        assertTrue(toString.contains(UserBean.USER_TYPE_COLLECTOR));
    }

    @Test
    void testGettersAndSetters() {
        
        UserBean userBean = new UserBean();

        
        userBean.setUsername("testuser");
        userBean.setPassword("testpass123");
        userBean.setUserType(UserBean.USER_TYPE_STORE);

        
        assertEquals("testuser", userBean.getUsername());
        assertEquals("testpass123", userBean.getPassword());
        assertEquals(UserBean.USER_TYPE_STORE, userBean.getUserType());
    }

    @Test
    void testDefaultConstructor_CreatesValidBean() {
        
        UserBean userBean = new UserBean();

        
        assertNotNull(userBean);
        assertNull(userBean.getUsername());
        assertNull(userBean.getPassword());
    }

    @Test
    void testTwoParameterConstructor_SetsDefaultCollectorType() {
        
        UserBean userBean = new UserBean("user", "pass123");

        
        assertEquals("user", userBean.getUsername());
        assertEquals("pass123", userBean.getPassword());
        assertEquals(UserBean.USER_TYPE_COLLECTOR, userBean.getUserType());
    }
}
