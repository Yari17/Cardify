package controller;

import model.dao.ITradeDao;
import model.dao.factory.DaoFactory;
import model.domain.TradeTransaction;
import model.domain.Binder;
import model.domain.enumerations.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveTradeControllerTest {

    private ApplicationController nav;
    @Mock
    private ITradeDao tradeDao;

    private LiveTradeController controllerUser1;
    private LiveTradeController controllerUser2;

    @BeforeEach
    void setUp() {
        // Crea una DaoFactory anonima che restituisce il tradeDao mockato
        DaoFactory daoFactory = new DaoFactory() {
            @Override
            public model.dao.IUserDao createUserDao() { return null; }
            @Override
            public model.dao.IBinderDao createBinderDao() { return null; }
            @Override
            public model.dao.IProposalDao createProposalDao() { return null; }
            @Override
            public model.dao.ITradeDao createTradeDao() { return tradeDao; }
        };

        // Crea un ApplicationController anonimo che restituisce la DaoFactory
        nav = new ApplicationController() {
            @Override
            public model.dao.factory.DaoFactory getDaoFactory() { return daoFactory; }
        };

        controllerUser1 = new LiveTradeController("user1", nav);
        controllerUser2 = new LiveTradeController("user2", nav);
    }

    @Test
    void confirmPresence_firstCollector_setsPartiallyArrived() {
        // Prepara i dati per la transazione
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(1, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        when(tradeDao.getTradeTransactionById(1)).thenReturn(tx);

        // Simula la conferma presenza del primo collezionista
        int code = controllerUser1.confirmPresence(1);
        assertTrue(code > 0, "Il session code deve essere generato");
        assertEquals(TradeStatus.PARTIALLY_ARRIVED, tx.getTradeStatus());
        verify(tradeDao, atLeastOnce()).save(tx);
    }

    @Test
    void confirmPresence_secondCollector_setsBothArrived() {
        // Prepara i dati per la transazione
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(2, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        when(tradeDao.getTradeTransactionById(2)).thenReturn(tx);

        // Primo collezionista arriva
        int code1 = controllerUser1.confirmPresence(2);
        assertTrue(code1 > 0);
        assertEquals(TradeStatus.PARTIALLY_ARRIVED, tx.getTradeStatus());

        // Secondo collezionista arriva
        int code2 = controllerUser2.confirmPresence(2);
        assertTrue(code2 > 0);
        assertEquals(TradeStatus.BOTH_ARRIVED, tx.getTradeStatus());

        verify(tradeDao, atLeast(2)).save(tx);
    }

    @Test
    void verifySessionCode_movesToInspectionPhase_whenBothCodesMatch() {
        // Prepara i dati per la transazione
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(3, TradeStatus.BOTH_ARRIVED, participants, details);
        tx.confirmPresence("user1");
        tx.confirmPresence("user2");

        when(tradeDao.getTradeTransactionById(3)).thenReturn(tx);

        // Verifica il codice del proposer
        boolean ok1 = controllerUser1.verifySessionCode(3, tx.getProposerSessionCode());
        assertTrue(ok1);
        // Verifica il codice del receiver
        boolean ok2 = controllerUser1.verifySessionCode(3, tx.getReceiverSessionCode());
        assertTrue(ok2);

        // Dopo aver accettato i codici, lo stato deve essere INSPECTION_PHASE
        assertEquals(TradeStatus.INSPECTION_PHASE, tx.getTradeStatus());
    }

    @Test
    void fullTradeFlow_performCardExchange_updatesBinders() {
        // Mock IBinderDao e CardProvider
        var binderDao = mock(model.dao.IBinderDao.class);
        var cardProvider = mock(model.api.ICardProvider.class);
        var daoFactory = new DaoFactory() {
            @Override public model.dao.IUserDao createUserDao() { return null; }
            @Override public model.dao.IBinderDao createBinderDao() { return binderDao; }
            @Override public model.dao.IProposalDao createProposalDao() { return null; }
            @Override public model.dao.ITradeDao createTradeDao() { return tradeDao; }
        };
        var navWithBinders = new ApplicationController() {
            @Override public model.dao.factory.DaoFactory getDaoFactory() { return daoFactory; }
            @Override public model.api.ICardProvider getCardProvider() { return cardProvider; }
        };
        var controller = new LiveTradeController("user1", navWithBinders);
        // Prepara trade con carte di set diversi
        var c = new model.domain.Card("SET1-001", "Charizard", "url1", null);
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(c),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(0, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        // Simula che nessun binder esista
        when(binderDao.getUserBinders("user2")).thenReturn(List.of());
        when(binderDao.getUserBinders("user1")).thenReturn(List.of());
        when(cardProvider.getAllSets()).thenReturn(java.util.Map.of("SET1", "Set Uno"));
        // Simula creazione binder
        doNothing().when(binderDao).createBinder(anyString(), anyString(), anyString());
        doNothing().when(binderDao).save(any());
        // Simula che dopo la creazione il binder esista
        when(binderDao.getUserBinders("user2")).thenReturn(List.of(new Binder("user2", "SET1", "Set Uno")));
        when(binderDao.getUserBinders("user1")).thenReturn(List.of());
        // Esegui lo scambio
        controller.performCardExchange(tx);
        // Verifica che i binders siano aggiornati
        verify(binderDao, atLeastOnce()).save(any(Binder.class));
        verify(binderDao, atLeastOnce()).createBinder(anyString(), anyString(), anyString());
    }
}
