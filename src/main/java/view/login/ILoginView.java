package view.login;

import controller.LoginController;
import model.bean.UserBean;
import view.IView;

public interface ILoginView extends IView {
    UserBean getUserCredentials();

    void showInputError(String message);
    void showSuccess(String message);
    void setController(LoginController controller);
}
