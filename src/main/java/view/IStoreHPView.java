package view;

import controller.StoreHPController;

public interface IStoreHPView extends IView {
    void setController(StoreHPController controller);


    void showWelcomeMessage(String username);
}
