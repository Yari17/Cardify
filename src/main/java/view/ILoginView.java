package view;

import controller.LoginController;
import model.bean.UserBean;
import model.domain.enumerations.PersistenceType;

public interface ILoginView extends IView {
    UserBean getUserCredentials();
    PersistenceType getPersistenceType();

    void showInputError(String message);
    void showSuccess(String message);
    void setController(LoginController controller);
}
