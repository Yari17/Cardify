package view;

import controller.RegistrationController;
import model.bean.UserBean;
import model.domain.enumerations.PersistenceType;

public interface IRegistrationView extends IView {
    UserBean getUserData();
    void showInputError(String message);
    void showSuccess(String message);
    void setController(RegistrationController controller);

    
    PersistenceType getPersistenceType();
}
