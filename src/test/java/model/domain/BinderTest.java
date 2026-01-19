package model.domain;

import model.bean.CardBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinderTest {

    @Test
    void addCard_mergesSameId() {
        Binder binder = new Binder();

        CardBean c1 = new CardBean();
        c1.setId("base5-9");
        c1.setQuantity(1);

        binder.addCard(c1);

        assertEquals(1, binder.getCards().size(), "Binder should contain one entry after first add");
        assertEquals(1, binder.getCards().get(0).getQuantity(), "Quantity should be 1 after first add");

        CardBean c2 = new CardBean();
        c2.setId("base5-9");
        c2.setQuantity(1);

        binder.addCard(c2);

        assertEquals(1, binder.getCards().size(), "Binder must still contain one entry after adding same id");
        assertEquals(2, binder.getCards().get(0).getQuantity(), "Quantity should be incremented to 2");
    }

    @Test
    void addCard_differentIdsCreateDistinctEntries() {
        Binder binder = new Binder();

        CardBean a = new CardBean();
        a.setId("a-1");
        a.setQuantity(1);
        binder.addCard(a);

        CardBean b = new CardBean();
        b.setId("b-2");
        b.setQuantity(3);
        binder.addCard(b);

        assertEquals(2, binder.getCards().size(), "Two different card ids should create two entries");
    }

    @Test
    void addCard_usesDefensiveCopy() {
        Binder binder = new Binder();

        CardBean original = new CardBean();
        original.setId("copy-1");
        original.setQuantity(1);

        binder.addCard(original);
        
        original.setQuantity(99);

        
        assertEquals(1, binder.getCards().size());
        assertEquals(1, binder.getCards().get(0).getQuantity(), "Binder must keep a defensive copy and not reflect changes to original object");
    }
}
