package controller;

import model.dao.demo.DemoBinderDao;
import model.domain.*;
import model.domain.enumerations.CardGameType;
import model.bean.BinderBean;
import model.bean.CardBean;
import view.ICollectionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per CollectionController usando JUnit.
 */
class CollectionControllerTest {

    private CollectionController controller;
    private DemoBinderDao demoBinderDao;
    private User testUser;
    private StubCollectionView stubView;

    // Stub semplice per ICollectionView
    private static class StubCollectionView implements ICollectionView {
        String lastError = null;
        boolean saveButtonVisible = false;

        @Override
        public void display() {
            // Stub per test
        }

        @Override
        public void setController(Object controller) {
            // Stub per test
        }

        @Override
        public void close() {
            // Stub per test
        }

        @Override
        public void refresh() {
            // Stub per test
        }

        @Override
        public void showError(String message) {
            this.lastError = message;
        }

        @Override
        public void displayUserBinders(List<BinderBean> binders) {
            // Stub per test
        }

        @Override
        public void onCreateBinder() {
            // Stub per test
        }

        @Override
        public void onAddCard(CardBean card) {
            // Stub per test
        }

        @Override
        public void onRemoveCard(CardBean card) {
            // Stub per test
        }

        @Override
        public void setSaveButtonVisible(boolean visible) {
            this.saveButtonVisible = visible;
        }

        @Override
        public void showAvailableSets(Map<String, String> availableSets) {
            // Stub per test
        }
    }

    @BeforeEach
    void setUp() {
        demoBinderDao = new DemoBinderDao();
        testUser = new User("testuser", "password", "Collezionista");
        stubView = new StubCollectionView();

        controller = new CollectionController(null, testUser, demoBinderDao);
        controller.setView(stubView);
    }

    @Test
    void testCreateDuplicateBinder() {
        // Crea binder iniziale
        Card testCard = new Card("Test Card", "card1", "set1", "image.png", CardGameType.POKEMON);
        List<CollectionItem> items = new ArrayList<>();
        items.add(new CollectionItem(testCard, 2));
        Binder binder = new Binder("testuser", "set1", "Test Set 1", items);

        demoBinderDao.createBinder("testuser", binder);

        // Tenta di creare duplicato
        controller.createNewBinder("set1", "Duplicate Set");

        // Verifica errore
        assertEquals("Set già presente nella collezione.", stubView.lastError);

        // Verifica che ci sia sempre solo 1 binder
        List<Binder> binders = demoBinderDao.getUserBinders("testuser");
        assertEquals(1, binders.size());
    }
}
