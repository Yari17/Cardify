
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
