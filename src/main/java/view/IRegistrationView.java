package view;

import controller.RegistrationController;
import model.bean.UserBean;
import model.domain.enumerations.PersistenceType;

public interface IRegistrationView extends IView {
    UserBean getUserData();
    void showInputError(String message);
    void showSuccess(String message);
    void setController(RegistrationController controller);

    // Persistence choice for registration (may be null to use app default)
    PersistenceType getPersistenceType();
}
