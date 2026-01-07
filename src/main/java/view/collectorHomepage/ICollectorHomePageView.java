package view.collectorhomepage;

import controller.CollectorHomePageController;
import view.IView;

public interface ICollectorHomePageView extends IView {
    void setController(CollectorHomePageController controller);


    void showWelcomeMessage(String username);
}
