package controller;

import model.bean.UserBean;
import model.dao.UserDao;
import view.IView;
import view.collectorhomepage.ICollectorHomePageView;
import view.factory.IViewFactory;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHomePageView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Navigator {
    private static final Logger LOGGER = Logger.getLogger(Navigator.class.getName());

    private final Deque<IView> viewStack = new ArrayDeque<>();
    private final UserDao userDao;
    private final IViewFactory viewFactory;
    private final ApplicationController applicationController;

    public Navigator(UserDao userDao, IViewFactory viewFactory, ApplicationController applicationController) {
        this.userDao = userDao;
        this.viewFactory = viewFactory;
        this.applicationController = applicationController;
    }

    public void navigateToLogin() {
        try {
            LoginController controller = new LoginController(userDao, applicationController);
            ILoginView view = viewFactory.createLoginView(controller);
            controller.setView(view);
            displayView(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to login", e);
            handleNavigationError(e);
        }
    }

    public void logout() {
        LOGGER.info("Logging out: clearing history and navigating to login");
        closeAll();
        navigateToLogin();
    }

    public void navigateToRegistration() {
        try {
            RegistrationController controller = new RegistrationController(userDao, applicationController);
            IRegistrationView view = viewFactory.createRegistrationView(controller);
            controller.setView(view);
            displayView(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to registration", e);
            handleNavigationError(e);
        }
    }

    public void navigateToCollectorHomePage(UserBean user) {
        try {
            CollectorHomePageController controller = new CollectorHomePageController(user.getUsername(), applicationController);
            ICollectorHomePageView view = viewFactory.createCollectorHomePageView(controller);
            controller.setView(view);
            displayView(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to collector home page", e);
            handleNavigationError(e);
        }
    }

    public void navigateToStoreHomePage(UserBean user) {
        try {
            StoreHomePageController controller = new StoreHomePageController(user.getUsername(), applicationController);
            IStoreHomePageView view = viewFactory.createStoreHomePageView(controller);
            controller.setView(view);
            displayView(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to store home page", e);
            handleNavigationError(e);
        }
    }

    public void handleRoleBasedNavigation(UserBean loggedInUser) {
        if (UserBean.USER_TYPE_COLLECTOR.equals(loggedInUser.getUserType())) {
            navigateToCollectorHomePage(loggedInUser);
        } else if (UserBean.USER_TYPE_STORE.equals(loggedInUser.getUserType())) {
            navigateToStoreHomePage(loggedInUser);
        } else {
            LOGGER.warning(() -> "Unknown user type: " + loggedInUser.getUserType());
            navigateToLogin();
        }
    }

    private void displayView(IView newView) {
        viewStack.addLast(newView);
        try {
            newView.display();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying view", e);
        }
    }

    public void back() {
        if (viewStack.size() > 1) {
            IView currentView = viewStack.removeLast();
            closeView(currentView);

            IView previousView = viewStack.getLast();
            try {
                previousView.display();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error displaying previous view", e);
            }
        } else {
            LOGGER.info("Cannot go back, already at root view");
        }
    }

    public void closeAll() {
        while (!viewStack.isEmpty()) {
            IView view = viewStack.removeLast();
            closeView(view);
        }
    }

    private void closeView(IView view) {
        if (view != null) {
            try {
                view.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing view", e);
            }
        }
    }

    private void handleNavigationError(Exception e) {
        LOGGER.log(Level.SEVERE, "Navigation error occurred", e);
        navigateToLogin();
    }
}
