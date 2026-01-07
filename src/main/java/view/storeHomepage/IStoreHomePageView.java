package view.storeHomepage;

import controller.StoreHomePageController;
import view.IView;

public interface IStoreHomePageView extends IView {
    void setController(StoreHomePageController controller);


    void showWelcomeMessage(String username);
}
