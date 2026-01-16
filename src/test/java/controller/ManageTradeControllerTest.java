package controller;

import model.dao.ITradeDao;
import model.dao.IProposalDao;
import model.dao.factory.DaoFactory;
import model.domain.Proposal;
import model.domain.TradeTransaction;
import model.domain.enumerations.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageTradeControllerTest {

    private ApplicationController nav;
    @Mock
    private IProposalDao proposalDao;
    @Mock
    private ITradeDao tradeDao;

    private ManageTradeController controller;

    @BeforeEach
    public void setUp() {
        DaoFactory daoFactory = new DaoFactory() {
            @Override
            public model.dao.IUserDao createUserDao() { return null; }
            @Override
            public model.dao.IBinderDao createBinderDao() { return null; }
            @Override
            public model.dao.IProposalDao createProposalDao() { return proposalDao; }
            @Override
            public model.dao.ITradeDao createTradeDao() { return tradeDao; }
        };

        nav = new ApplicationController() {
            @Override
            public model.dao.factory.DaoFactory getDaoFactory() { return daoFactory; }
        };

        controller = new ManageTradeController("user1", nav);
    }

    @Test
    public void acceptProposal_createsTradeTransaction() {
        Proposal p = new Proposal();
        p.setProposalId("p1");
        p.setProposerId("user1");
        p.setReceiverId("user2");
        p.setCardsOffered(List.of());
        p.setCardsRequested(List.of());
        p.setMeetingDate(LocalDateTime.now().toLocalDate().toString());
        when(proposalDao.getById("p1")).thenReturn(Optional.of(p));

        boolean res = controller.acceptProposal("p1");
        assertTrue(res);
        verify(tradeDao, atLeastOnce()).save(any(TradeTransaction.class));
    }
}
