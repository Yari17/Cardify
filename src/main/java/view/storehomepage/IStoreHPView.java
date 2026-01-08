package view.storehomepage;

import controller.StoreHPController;
import view.IView;

public interface IStoreHPView extends IView {
    void setController(StoreHPController controller);


    void showWelcomeMessage(String username);
}
