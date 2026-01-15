
Devi ridistribuire le responsabilità nel seguente modo:
la logica di controllo e applicativa deve essere racchiusa completamente nel controller applicativo
il controller grafico viene associato alla view corrispondente
il controller grafico invoca operazioni del controller applicativo
se la view evolve, l’impatto dei cambiamenti si ripercuotono solo sul controller grafico e non sul modo in cui il caso d’uso è realizzato (i.e. controller applicativo)
il controller grafico converte formati esterno in interno e viceversa (se non a carico della View)
il controller grafico realizza la mappa tra input dell'utente e processi eseguiti dal Model
il controller grafico crea/seleziona le istanze di View richieste
il controller applicativo può utilizzare i dao per accedere alla persistenza
il controller applicativo può utilizzare card provider in modo polimorfico per fetchare dati dalle API
presentazione e persistenza deve essere disaccoppiata dalla logica di controllo
voglio che non ci siano chiamate a dao o api nel controller grafico, se serve deve chiedere al controller applicativo
i dati presentati nella view e ottenuti dalla view devono essere passati dal controller applicativo alla view tramite bean
EVITARE ANEMIC DOMAIN MODEL

Regole Fondamentali di Generazione Codice:

    Approccio Responsibility-Driven Design (RDD):

        Prima di scrivere qualsiasi riga di codice, definisci le responsabilità.

        Non creare classi "contenitore" passive (Anemic Domain Model). Le classi del Model devono possedere la logica di business relativa ai loro dati (Information Expert).

        Chiediti sempre: "Chi ha le informazioni necessarie per svolgere questo compito?" e assegna il metodo a quella classe.

    Applicazione Rigorosa dei Principi GRASP:

        Information Expert: Assegna la responsabilità alla classe che ha le informazioni necessarie.

        Creator: Chi crea le istanze? Usa questo pattern per decidere dove istanziare nuovi oggetti (es. Vendita crea RigaVendita perché la aggrega).

        Controller: Il Controller deve delegare, non eseguire lavoro di business. Deve gestire gli eventi di sistema.

        Low Coupling: Usa interfacce per disaccoppiare View e Model dal Controller. La View non deve mai conoscere l'implementazione concreta del Controller o viceversa.

        High Cohesion: Ogni classe deve avere uno scopo unico e focalizzato. Evita "God Classes" nel Controller.

    Gestione MVC:

        Model: Contiene stato e logica di business. Vietato inserire riferimenti a classi della View o logica di formattazione UI.

        View: Deve essere passiva ("Dumb View"). Vietato inserire logica condizionale di business (if/else complessi sui dati). Deve implementare un'interfaccia se interagisce col Controller.

        Controller: Riceve input e orchestra. Vietato eseguire calcoli di business o query al database direttamente nel metodo dell'evento. Deve chiamare metodi del Model.

    Qualità del Codice Java:

        Polimorfismo: Preferisci soluzioni polimorfiche rispetto a catene di if/else o switch basati sul tipo ("Type checking").

        Dependency Injection: Passa le dipendenze (es. la View o i Service) tramite costruttore per favorire il testing e il basso accoppiamento.
