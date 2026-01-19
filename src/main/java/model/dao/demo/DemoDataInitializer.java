package model.dao.demo;

import model.bean.CardBean;
import model.domain.Binder;
import model.domain.TradeTransaction;
import model.domain.Proposal;
import model.domain.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Istanzia degli oggetti "dummy" per la versione demo dell'applicazione in modo da mostrare il funzionamento anche
* senza una vera e propria persistenza */
public final class DemoDataInitializer {
    private static final Logger LOGGER = Logger.getLogger(DemoDataInitializer.class.getName());

    // Seed constants to avoid duplicated literals
    private static final String SEED_USER1 = "user1";
    private static final String SEED_USER2 = "user2";
    private static final String SEED_USER3 = "user3";
    private static final String SEED_STORE1 = "Store1";
    private static final String SEED_CICCIO = "CiccioGamer89TCG";
    private static final String SEED_PASSWORD = "password";

    private DemoDataInitializer() {}

    public static void init(DemoUserDao userDao, DemoBinderDao binderDao, DemoProposalDao proposalDao, DemoTradeDao tradeDao) {
        try {
            if (userDao != null) {
                Map<String, String> creds = new HashMap<>();
                List<User> users = new ArrayList<>();

                User u1 = new User(SEED_USER1, 0, 0);
                u1.setUserType(config.AppConfig.USER_TYPE_COLLECTOR);
                users.add(u1); creds.put(SEED_USER1, SEED_PASSWORD);

                User u2 = new User(SEED_USER2, 0, 0);
                u2.setUserType(config.AppConfig.USER_TYPE_COLLECTOR);
                users.add(u2); creds.put(SEED_USER2, SEED_PASSWORD);

                User u3 = new User(SEED_USER3, 0, 0);
                u3.setUserType(config.AppConfig.USER_TYPE_COLLECTOR);
                users.add(u3); creds.put(SEED_USER3, SEED_PASSWORD);

                User store1 = new User(SEED_STORE1, 0, 0);
                store1.setUserType(config.AppConfig.USER_TYPE_STORE);
                users.add(store1); creds.put(SEED_STORE1, SEED_PASSWORD);

                User ciccio = new User(SEED_CICCIO, 0, 0);
                ciccio.setUserType(config.AppConfig.USER_TYPE_STORE);
                users.add(ciccio); creds.put(SEED_CICCIO, SEED_PASSWORD);

                userDao.loadFromCollection(users, creds);
            }

            if (binderDao != null) {
                List<Binder> binders = new ArrayList<>();

                Binder b1 = new Binder(SEED_USER1, "pl4", "Platinum Legends");
                CardBean cb1 = new CardBean(); cb1.setId("pl4-1"); cb1.setQuantity(1); cb1.setGameType(model.domain.enumerations.CardGameType.POKEMON);
                CardBean cb2 = new CardBean(); cb2.setId("pl4-3"); cb2.setQuantity(2); cb2.setGameType(model.domain.enumerations.CardGameType.POKEMON);
                b1.addCard(cb1); b1.addCard(cb2);
                binders.add(b1);

                Binder b2 = new Binder(SEED_USER2, "base5", "Team Rocket");
                CardBean cb3 = new CardBean(); cb3.setId("base5-9"); cb3.setQuantity(1); cb3.setGameType(model.domain.enumerations.CardGameType.POKEMON);
                b2.addCard(cb3);
                binders.add(b2);

                Binder b3 = new Binder(SEED_STORE1, "sv08", "Store Set");
                binders.add(b3);
                binderDao.loadFromCollection(binders);
            }

            if (proposalDao != null) {
                List<Proposal> proposals = new ArrayList<>();

                Proposal p1 = new Proposal();
                p1.setProposalId("1");
                p1.setProposerId(SEED_USER3);
                p1.setReceiverId(SEED_USER1);
                p1.setStatus(model.domain.enumerations.ProposalStatus.ACCEPTED);
                p1.setMeetingPlace(SEED_CICCIO);
                p1.setMeetingDate(LocalDateTime.now().toLocalDate().toString());
                p1.setMeetingTime("22:20");
                proposals.add(p1);

                Proposal p2 = new Proposal();
                p2.setProposalId("2");
                p2.setProposerId(SEED_USER1);
                p2.setReceiverId(SEED_USER3);
                p2.setStatus(model.domain.enumerations.ProposalStatus.REJECTED);
                p2.setMeetingPlace(SEED_STORE1);
                proposals.add(p2);

                proposalDao.loadFromCollection(proposals);
            }

            if (tradeDao != null) {
                List<TradeTransaction> trades = new ArrayList<>();

                TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants(SEED_USER3, SEED_USER1, SEED_CICCIO);
                List<model.domain.Card> offered = new ArrayList<>();
                model.domain.Card c = new model.domain.Card("A2a-047","Garchomp ex","", model.domain.enumerations.CardGameType.POKEMON);
                c.setQuantity(1); offered.add(c);
                TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), offered, Collections.emptyList());
                TradeTransaction t1 = new TradeTransaction(1, model.domain.enumerations.TradeStatus.COMPLETED, participants, details);
                trades.add(t1);

                tradeDao.loadFromCollection(trades);
            }

            LOGGER.info(() -> "DemoDataInitializer: demo seed data initialized in-memory");
        } catch (Exception ex) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "DemoDataInitializer failed to initialize demo data", ex);
            }
         }
     }
 }
