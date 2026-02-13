# Cardify - Sistema di Gestione Collezioni di Carte da Gioco

Cardify è un'applicazione Java per la gestione e lo scambio di collezioni di carte da gioco (Pokémon TCG, Yu-Gi-Oh!, Magic: The Gathering). Il sistema permette ai collezionisti di catalogare le proprie carte, proporre scambi con altri utenti, e ai negozi di facilitare gli incontri fisici per la finalizzazione degli scambi.

## 📋 Indice

- [Funzionalità Principali](#-funzionalità-principali)
- [Architettura del Sistema](#-architettura-del-sistema)
- [Design Patterns Applicati](#-design-patterns-applicati)
- [Tecnologie Utilizzate](#-tecnologie-utilizzate)
- [Requisiti di Sistema](#-requisiti-di-sistema)
- [Installazione ed Esecuzione](#-installazione-ed-esecuzione)
- [Modalità di Persistenza](#-modalità-di-persistenza)
- [Consigli per la Preparazione all'Orale](#-consigli-per-la-preparazione-allorale)

---

## 🎯 Funzionalità Principali

### Per i Collezionisti
- **Gestione Collezione**: Catalogazione delle carte possedute organizzate per set
- **Ricerca Carte**: Integrazione con API esterne (TCGdex) per cercare carte disponibili
- **Proposte di Scambio**: Creazione di proposte specificando carte offerte e richieste
- **Gestione Proposte**: Visualizzazione e accettazione/rifiuto delle proposte ricevute
- **Sistema di Notifiche**: Notifiche in tempo reale per nuove proposte e aggiornamenti

### Per i Negozi
- **Gestione Scambi**: Supervisione degli scambi fisici tra collezionisti
- **Verifica Arrivi**: Conferma dell'arrivo dei partecipanti tramite codici univoci
- **Ispezione Carte**: Validazione delle carte prima della finalizzazione dello scambio
- **Calendario Appuntamenti**: Visualizzazione degli scambi programmati

### Funzionalità Comuni
- **Autenticazione**: Sistema di login e registrazione per collezionisti e negozi
- **Doppia Interfaccia**: CLI (Command Line Interface) e JavaFX (interfaccia grafica)
- **Persistenza Flessibile**: Supporto per Demo (in-memory), JDBC (MySQL), e JSON

---

## 🏗️ Architettura del Sistema

Il progetto segue rigorosamente il pattern **Model-View-Controller (MVC)** con una chiara separazione delle responsabilità:

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌──────────────┐              ┌──────────────┐            │
│  │  CLI Views   │              │ JavaFX Views │            │
│  │  (Terminal)  │              │    (GUI)     │            │
│  └──────────────┘              └──────────────┘            │
└────────────┬────────────────────────┬────────────────────────┘
             │                        │
             │    IView Interface     │
             │                        │
┌────────────┴────────────────────────┴────────────────────────┐
│                    CONTROLLER LAYER                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ApplicationController (Navigation & Lifecycle)      │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │  Login   │ │Collection│ │  Trade   │ │ Proposal │      │
│  │Controller│ │Controller│ │Controller│ │Controller│ ...  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└────────────┬─────────────────────────────────────────────────┘
             │
             │    Bean Objects (Data Transfer)
             │
┌────────────┴─────────────────────────────────────────────────┐
│                      MODEL LAYER                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Domain Entities                         │    │
│  │  User, Card, Binder, Proposal, TradeSession, etc.   │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              DAO Interfaces                          │    │
│  │  IUserDao, IBinderDao, IProposalDao, etc.           │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │  Demo    │  │   JDBC   │  │   JSON   │                  │
│  │   DAO    │  │   DAO    │  │   DAO    │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└──────────────────────────────────────────────────────────────┘
```

### Componenti Principali

#### 1. **Presentation Layer** (View)
- **Interfacce View**: Contratti comuni (`ILoginView`, `ICollectionView`, ecc.)
- **Implementazioni CLI**: Viste testuali per terminale
- **Implementazioni JavaFX**: Viste grafiche con FXML

#### 2. **Controller Layer**
- **ApplicationController**: Gestisce la navigazione tra le viste e il ciclo di vita dell'applicazione
- **Controller Specifici**: Ogni funzionalità ha il proprio controller (Login, Collection, Trade, ecc.)
- **Bean Objects**: Oggetti di trasferimento dati tra Controller e View

#### 3. **Model Layer**
- **Domain Entities**: Rappresentano le entità di business (`User`, `Card`, `Binder`, `Proposal`, `TradeSession`)
- **DAO Interfaces**: Contratti per l'accesso ai dati
- **DAO Implementations**: Implementazioni concrete per Demo, JDBC, JSON

#### 4. **Configuration & Utilities**
- **AppConfig**: Configurazione globale (tipo UI, persistenza)
- **DatabaseConfig**: Configurazione database
- **Notification System**: Sistema di notifiche basato su Observer

---

## 🎨 Design Patterns Applicati

### 1. **Model-View-Controller (MVC)**
**Scopo**: Separazione delle responsabilità tra presentazione, logica di business e accesso ai dati.

**Implementazione**:
- **Model**: Entità di dominio + DAO
- **View**: Interfacce CLI e JavaFX
- **Controller**: Gestione della logica applicativa e coordinamento

**Esempio**:
```java
// Controller coordina View e Model
public class LoginController {
    private ILoginView view;
    private IUserDao userDao;
    
    public void handleLogin() {
        String username = view.getUsername();
        User user = userDao.authenticateAndGetUser(username, password);
        if (user != null) {
            view.showLoginSuccess();
        }
    }
}
```

### 2. **Data Access Object (DAO)**
**Scopo**: Astrazione dell'accesso ai dati, isolando la logica di persistenza.

**Implementazione**:
- Interfacce DAO (`IUserDao`, `IBinderDao`, ecc.)
- Implementazioni multiple (Demo, JDBC, JSON)

**Esempio**:
```java
public interface IUserDao {
    User authenticateAndGetUser(String username, String password);
    void register(User user);
    List<User> getStores();
}
```

### 3. **Abstract Factory**
**Scopo**: Creazione di famiglie di oggetti correlati (DAO, View) senza specificare le classi concrete.

**Implementazione**:
- `DaoFactory`: Crea DAO in base al tipo di persistenza
- `ViewFactory`: Crea View in base al tipo di UI

**Esempio**:
```java
public abstract class DaoFactory {
    public static DaoFactory getFactory(PersistenceType type) {
        return switch (type) {
            case DEMO -> new DemoDaoFactory();
            case JDBC -> new JdbcDaoFactory();
            case JSON -> new JsonDaoFactory();
        };
    }
    
    public abstract IUserDao createUserDao();
    public abstract IBinderDao createBinderDao();
}
```

### 4. **Observer**
**Scopo**: Notifica automatica di eventi a oggetti interessati.

**Implementazione**:
- `Subject`: Interfaccia per oggetti osservabili
- `Observer`: Interfaccia per oggetti osservatori
- `NotificationService`: Implementazione concreta dell'Observer

**Esempio**:
```java
public class TradeController implements Subject {
    private List<Observer> observers = new ArrayList<>();
    
    public void acceptProposal(String proposalId) {
        // ... logica di accettazione
        notifyObservers("Proposta accettata!", receiverUsername);
    }
    
    private void notifyObservers(String message, String userId) {
        for (Observer obs : observers) {
            obs.update(message, userId);
        }
    }
}
```

### 5. **Singleton**
**Scopo**: Garantire una sola istanza di una classe.

**Implementazione**:
- `DaoFactory`: Singleton per ogni tipo di persistenza
- `DBConnector`: Singleton per la connessione al database

**Esempio**:
```java
public class DBConnector {
    private static DBConnector instance;
    
    private static DBConnector getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnector();
        }
        return instance;
    }
}
```

### 6. **Dependency Injection**
**Scopo**: Inversione del controllo per favorire il disaccoppiamento.

**Implementazione**:
- I controller ricevono le dipendenze (DAO, View) tramite costruttore
- Facilita il testing e la manutenibilità

**Esempio**:
```java
public class LoginController {
    private final IUserDao userDao;
    
    public LoginController(ApplicationController appController, IUserDao userDao) {
        this.appController = appController;
        this.userDao = userDao; // Dependency Injection
    }
}
```

### 7. **Strategy**
**Scopo**: Definire una famiglia di algoritmi intercambiabili.

**Implementazione**:
- Persistenza intercambiabile (Demo, JDBC, JSON)
- UI intercambiabile (CLI, JavaFX)

---

## 🛠️ Tecnologie Utilizzate

### Core
- **Java 25**: Linguaggio principale
- **Maven**: Build automation e gestione dipendenze
- **JavaFX 23.0.1**: Framework per interfaccia grafica

### Database & Persistenza
- **MySQL Connector/J 9.3.0**: Driver JDBC per MySQL
- **Gson 2.10.1**: Serializzazione/deserializzazione JSON

### Testing
- **JUnit 5.10.1**: Framework di testing
- **Mockito 5.15.2**: Mocking per unit test

### Integrazione Esterna
- **TCGdex Java SDK 2.0.2**: API per dati carte Pokémon

### UI & Styling
- **Ikonli 12.3.1**: Libreria di icone per JavaFX
- **FontAwesomeFX 4.7.0**: Icone Font Awesome

---

## 💻 Requisiti di Sistema

- **JDK 25** o superiore
- **Maven 3.6+**
- **MySQL 8.0+** (solo per modalità JDBC)
- **Sistema Operativo**: Windows, macOS, Linux

---

## 🚀 Installazione ed Esecuzione

### 1. Clone del Repository
```bash
git clone <repository-url>
cd Cardify
```

### 2. Configurazione Database (Opzionale - solo per JDBC)
```bash
# Importa lo schema
mysql -u root -p < database/schema.sql
```

### 3. Compilazione
```bash
mvn clean compile
```

### 4. Esecuzione
```bash
mvn javafx:run
```

### 5. Selezione Modalità
All'avvio, il sistema chiederà:
1. **Tipo di persistenza**: 
   - `0`: Demo (in-memory, dati precaricati)
   - `1`: JDBC (MySQL)
2. **Tipo di interfaccia**:
   - `0`: CLI (terminale)
   - `1`: JavaFX (grafica)

---

## 💾 Modalità di Persistenza

### Demo Mode
- **Caratteristiche**: Dati in memoria, nessuna persistenza
- **Utenti precaricati**:
  - `collector1` / `password` (Collezionista)
  - `collector2` / `password` (Collezionista)
  - `store1` / `password` (Negozio)
  - `FrancescoTotti10` / `DAJEROMADAJE` (Collezionista con 10 Charizard)
- **Uso**: Testing rapido, demo, sviluppo

### JDBC Mode
- **Caratteristiche**: Persistenza su MySQL
- **Configurazione**: `src/main/java/config/DatabaseConfig.java`
- **Schema**: `database/schema.sql`
- **Uso**: Produzione, dati persistenti

### JSON Mode
- **Caratteristiche**: Persistenza su file JSON
- **File**: `database/binders.json`, `database/users.json`
- **Uso**: Persistenza leggera senza database

---

## 📚 Consigli per la Preparazione all'Orale

### ⚠️ Criticità Architetturali Rilevate

Durante la discussione orale, sono emerse alcune criticità architetturali che è importante conoscere e saper discutere:

#### 1. **ApplicationController come Navigatore**
**Problema**: L'uso di `ApplicationController` come gestore della navigazione tra schermate introduce un accoppiamento con la logica di paginazione.

**Soluzione Corretta**: 
- Distaccarsi completamente dalla logica di paginazione/schermate
- I controller grafici dovrebbero gestire autonomamente la presentazione
- L'`ApplicationController` dovrebbe limitarsi al ciclo di vita dell'applicazione, non alla navigazione

**Riflessione**: Questo design rappresenta una violazione della Legge di Demetra e limita la scalabilità del sistema ad altri ecosistemi.

#### 2. **Accesso delle View alle Classi di Dominio**
**Regola**: **Nessuna View deve mai accedere direttamente a istanze di classi di dominio o model.**

**Implementazione Corretta**:
- Le View ricevono **esclusivamente Bean** dai Controller
- I Bean sono oggetti di trasferimento dati (DTO) privi di logica di business
- Esempio: `UserBean`, `CardBean`, `ProposalBean`

**Esempio Corretto**:
```java
// ❌ SBAGLIATO
public void displayUser(User user) {
    nameLabel.setText(user.getUsername());
}

// ✅ CORRETTO
public void displayUser(UserBean userBean) {
    nameLabel.setText(userBean.getUsername());
}
```

#### 3. **Validazione degli Input**
**Regola**: **Assolutamente nessuna validazione degli input nel controller grafico.**

**Dove Validare**:
- ✅ **Nelle classi Bean**: Validazione sintattica e semantica
- ✅ **Nei Controller di Business**: Validazione delle regole di business
- ❌ **Mai nelle View**: Le view si limitano a raccogliere e mostrare dati

**Esempio**:
```java
// Bean con validazione
public class UserBean {
    private String username;
    
    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido");
        }
        this.username = username;
    }
}
```

#### 4. **Passaggio Dati Controller → View**
**Regola**: I controller passano i dati alle view **esclusivamente attraverso Bean**.

**Pattern "Push" vs "Pull"**:
- ✅ **Push Model**: Il controller chiama metodi della view passando Bean
- ❌ **Pull Model**: La view richiede dati al controller (accoppiamento forte)

**Esempio Push Model**:
```java
// Controller
public void loadProposals() {
    List<Proposal> proposals = proposalDao.getProposalsForUser(user);
    List<ProposalBean> beans = ProposalMapper.toBeanList(proposals);
    view.showProposals(beans); // Push dei dati
}

// View
public void showProposals(List<ProposalBean> proposals) {
    // Mostra i dati ricevuti
}
```

#### 5. **Logica di Business nelle View**
**Regola**: **Le view non contengono logica di business.**

**Cosa Può Fare una View**:
- ✅ Formattazione visuale (colori, layout)
- ✅ Gestione eventi UI (click, input)
- ✅ Validazione sintattica basilare (campo vuoto)

**Cosa NON Può Fare una View**:
- ❌ Calcoli di business (prezzi, sconti, disponibilità)
- ❌ Decisioni basate su regole di dominio
- ❌ Accesso diretto a DAO o database

#### 6. **Mappatura Foreign Key → Entità di Dominio**
**Regola**: Quando un DAO preleva una foreign key dalla persistenza, **deve mappare l'ID sull'entità di dominio corrispondente**.

**Esempio Scorretto**:
```java
// ❌ SBAGLIATO: Binder contiene solo l'ID della carta
public class Binder {
    private String ownerId;
    private List<String> cardIds; // Solo ID!
}
```

**Esempio Corretto**:
```java
// ✅ CORRETTO: Binder contiene oggetti Card completi
public class Binder {
    private String owner;
    private List<CollectionItem> ownedCards; // Oggetti completi
}

// Nel DAO
public Binder getBinderById(int binderId) {
    // 1. Preleva il binder
    Binder binder = loadBinderFromDB(binderId);
    
    // 2. Per ogni cardId, ricostruisce l'oggetto Card
    List<CollectionItem> items = new ArrayList<>();
    for (String cardId : cardIds) {
        Card card = cardDao.getCardById(cardId); // Ricostruzione
        items.add(new CollectionItem(card, quantity));
    }
    binder.setOwnedCards(items);
    
    return binder;
}
```

**Motivazione**: Questo garantisce che il dominio sia sempre consistente e che le entità siano complete, evitando "lazy loading" implicito e violazioni dell'incapsulamento.

#### 7. **Cache Volatile nei DAO**
**Consiglio**: Per ottimizzare le performance e ridurre il carico sulla persistenza, è opportuno implementare una **cache in memoria volatile** nei DAO.

**Implementazione Suggerita**:
```java
// ✅ CORRETTO: DAO con cache in memoria
public class JdbcUserDao implements IUserDao {
    private final Map<String, User> cache = new HashMap<>();
    
    @Override
    public User getUserByUsername(String username) {
        // 1. Controlla la cache
        if (cache.containsKey(username)) {
            return cache.get(username);
        }
        
        // 2. Se non in cache, preleva dal database
        User user = loadUserFromDatabase(username);
        
        // 3. Salva in cache per accessi futuri
        if (user != null) {
            cache.put(username, user);
        }
        
        return user;
    }
    
    @Override
    public void updateUser(User user) {
        // Aggiorna database
        updateUserInDatabase(user);
        
        // Invalida/aggiorna cache
        cache.put(user.getUsername(), user);
    }
}
```

**Vantaggi**:
- ✅ Riduzione drastica delle query al database per dati frequentemente acceduti
- ✅ Miglioramento delle performance dell'applicazione
- ✅ Riduzione del carico sul sistema di persistenza

**Considerazioni**:
- ⚠️ La cache è volatile: i dati vengono persi al riavvio dell'applicazione
- ⚠️ Necessaria una strategia di invalidazione per mantenere la coerenza
- ⚠️ Attenzione alla memoria: implementare politiche di eviction (es. LRU) per cache grandi

---

### ✅ Principi GRASP Applicati

Il progetto applica rigorosamente i principi GRASP (General Responsibility Assignment Software Patterns):

1. **Information Expert**: Ogni classe ha la responsabilità sui propri dati
2. **Creator**: Le Factory creano gli oggetti correlati
3. **Low Coupling**: Uso di interfacce e Dependency Injection
4. **High Cohesion**: Ogni classe ha una responsabilità ben definita
5. **Controller**: Controller separati per ogni funzionalità
6. **Polymorphism**: Interfacce DAO e View con implementazioni multiple
7. **Pure Fabrication**: Factory e Mapper sono classi di supporto
8. **Indirection**: DAO e Bean introducono livelli di indirezione
9. **Protected Variations**: Interfacce proteggono da variazioni implementative

---

### 🎓 Suggerimenti per l'Esame

1. **Preparati a Discutere le Scelte Architetturali**:
   - Perché MVC?
   - Perché il pattern DAO?
   - Come funziona l'Observer per le notifiche?

2. **Conosci i Limiti del Tuo Design**:
   - Sii onesto sulle criticità (es. ApplicationController)
   - Proponi soluzioni alternative
   - Dimostra consapevolezza critica

3. **Esempi Concreti**:
   - Prepara esempi di codice per ogni pattern
   - Spiega il flusso di un caso d'uso completo (es. creazione proposta)

4. **Estensibilità**:
   - Come aggiungeresti un nuovo tipo di persistenza?
   - Come aggiungeresti un nuovo tipo di carta (es. Magic)?

---

## 🎉 Conclusione

Cardify rappresenta un'applicazione completa che dimostra l'applicazione pratica dei principi di ingegneria del software, design patterns, e architettura a strati. Il sistema è estensibile, manutenibile, e testabile grazie alla rigorosa separazione delle responsabilità e all'uso di pattern consolidati.

**Buona fortuna per l'esame!** 🚀

![You Are The Next](src/main/resources/icons/you-are-the-next.jpg)

---

## 📄 Licenza

Questo progetto è stato sviluppato a scopo didattico per il corso di Ingegneria del Software.

## 👥 Autore

Sviluppato con passione per l'esame di Ingegneria del Software.
