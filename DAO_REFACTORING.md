# Refactoring DAO - Utilizzo Solo Metodi dell'Interfaccia

## 🎯 Obiettivo
Garantire che il codice utilizzi **solo i metodi definiti nell'interfaccia `IBinderDao`**, evitando dipendenze da implementazioni concrete.

---

## 📋 Metodi Definiti nell'Interfaccia

### Da `IDao<Binder>` (ereditati)
```java
Optional<Binder> get(long id);
List<Binder> getAll();
void save(Binder binder);
void update(Binder binder, String[] params);
void delete(Binder binder);
```

### Da `IBinderDao` (specifici)
```java
List<Binder> getUserBinders(String owner);
void addCardToBinder(String binderId, String cardId);
void createBinder(String owner, String setId, String setName);
```

---

## ✅ Modifiche Implementate

### 1. **JdbcBinderDao** - Implementazione Completa

**Prima:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    return List.of();  // ❌ Stub vuoto
}

@Override
public void createBinder(String owner, String setId, String setName) {
    // ❌ Vuoto
}
```

**Dopo:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    String sql = "SELECT * FROM binders WHERE owner = ?";
    // ... implementazione JDBC completa
    return binders;  // ✅ Filtra per owner nel DB
}

@Override
public void createBinder(String owner, String setId, String setName) {
    Binder binder = new Binder(owner, setId, setName);
    save(binder);  // ✅ Usa metodo dell'interfaccia
}

@Override
public void addCardToBinder(String binderId, String cardId) {
    // TODO: Implementare quando avremo tabella carte
    LOGGER.warning("addCardToBinder not yet implemented");
}
```

### 2. **JsonBinderDao** - Implementazione Completa

**Prima:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    return List.of();  // ❌ Stub vuoto
}
```

**Dopo:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    List<Binder> userBinders = bindersByOwner.get(owner);
    if (userBinders == null) {
        return new ArrayList<>();
    }
    return new ArrayList<>(userBinders);  // ✅ Usa mappa esistente
}

@Override
public void createBinder(String owner, String setId, String setName) {
    Binder binder = new Binder(owner, setId, setName);
    save(binder);  // ✅ Riutilizza save esistente
}
```

### 3. **DemoBinderDao** - Implementazione Completa

**Prima:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    return List.of();  // ❌ Stub vuoto
}
```

**Dopo:**
```java
@Override
public List<Binder> getUserBinders(String owner) {
    return findByOwner(owner);  // ✅ Riutilizza metodo esistente
}

@Override
public void createBinder(String owner, String setId, String setName) {
    Binder binder = new Binder(owner, setId, setName);
    save(binder);  // ✅ Usa save dell'interfaccia
}
```

### 4. **CollectionController** - Uso Solo Metodi Interfaccia

**Prima (SBAGLIATO):**
```java
public void loadUserBinders() {
    List<Binder> userBinders = binderDao.getAll().stream()  // ❌ Usa getAll()
            .filter(binder -> username.equals(binder.getOwner()))  // ❌ Filtro manuale
            .collect(Collectors.toList());
    // ...
}

public void createBinder(String setId, String setName) {
    Binder newBinder = new Binder(username, setId, setName);  // ❌ Crea Binder manualmente
    binderDao.save(newBinder);  // ❌ Usa save direttamente
}
```

**Dopo (CORRETTO):**
```java
public void loadUserBinders() {
    List<Binder> userBinders = binderDao.getUserBinders(username);  // ✅ Usa metodo interfaccia
    // ...
}

public void createBinder(String setId, String setName) {
    binderDao.createBinder(username, setId, setName);  // ✅ Usa metodo interfaccia
    // ...
}
```

---

## 🎯 Vantaggi del Refactoring

### 1. **Separazione delle Responsabilità**
- ❌ Prima: Controller creava oggetti Binder e chiamava save
- ✅ Dopo: Controller delega tutto al DAO

### 2. **Riutilizzo del Codice**
- ❌ Prima: Filtro manuale con stream nel controller
- ✅ Dopo: Filtro implementato nel DAO (riutilizzabile)

### 3. **Efficienza Database**
```java
// Prima (JDBC):
SELECT * FROM binders;  // Carica TUTTI i binder
// Poi filtro in memoria

// Dopo (JDBC):
SELECT * FROM binders WHERE owner = ?;  // Filtra nel DB ✅
```

### 4. **Consistenza tra Implementazioni**
Tutti i DAO (JDBC, JSON, Demo) implementano gli stessi metodi in modo coerente.

### 5. **Facilità di Testing**
```java
// Mock dell'interfaccia
IBinderDao mockDao = mock(IBinderDao.class);
when(mockDao.getUserBinders("mario")).thenReturn(testBinders);
```

---

## 📊 Metodi Utilizzati dal Controller

### Prima del Refactoring
```
Controller → DAO:
├─ getAll()              ❌ Non specifico
├─ save(Binder)          ❌ Troppo generico
└─ Stream filtering      ❌ Logica nel controller
```

### Dopo il Refactoring
```
Controller → DAO:
├─ getUserBinders(owner) ✅ Metodo specifico interfaccia
└─ createBinder(...)     ✅ Metodo specifico interfaccia
```

---

## 🔍 Verifica Compliance

### Metodi IBinderDao - Stato Implementazione

| Metodo | JdbcBinderDao | JsonBinderDao | DemoBinderDao | Usato da Controller |
|--------|---------------|---------------|---------------|---------------------|
| `getUserBinders()` | ✅ Implementato | ✅ Implementato | ✅ Implementato | ✅ Sì |
| `createBinder()` | ✅ Implementato | ✅ Implementato | ✅ Implementato | ✅ Sì |
| `addCardToBinder()` | ⚠️ TODO | ⚠️ TODO | ⚠️ TODO | ❌ No (futuro) |

### Metodi IDao - Stato Uso

| Metodo | Implementato | Usato da Controller |
|--------|--------------|---------------------|
| `get(id)` | ✅ Tutti | ❌ Non ancora (futuro: apri binder) |
| `getAll()` | ✅ Tutti | ❌ **Rimosso** (ora usa getUserBinders) |
| `save(T)` | ✅ Tutti | ❌ **Rimosso** (ora usa createBinder) |
| `update(T, params)` | ✅ Tutti | ❌ Non ancora (futuro: modifica binder) |
| `delete(T)` | ✅ Tutti | ❌ Non ancora (futuro: elimina binder) |

---

## 📝 Import Rimossi

**CollectionController.java**
```java
// Rimosso:
import java.util.stream.Collectors;  // ❌ Non più necessario
```

**JsonBinderDao.java**
```java
// Rimosso:
import java.util.stream.Collectors;  // ❌ Non più necessario
```

---

## ✅ Checklist Completamento

- [x] `getUserBinders()` implementato in JdbcBinderDao
- [x] `getUserBinders()` implementato in JsonBinderDao
- [x] `getUserBinders()` implementato in DemoBinderDao
- [x] `createBinder()` implementato in JdbcBinderDao
- [x] `createBinder()` implementato in JsonBinderDao
- [x] `createBinder()` implementato in DemoBinderDao
- [x] `addCardToBinder()` stub con TODO in tutti i DAO
- [x] Controller usa `getUserBinders()` invece di `getAll()`
- [x] Controller usa `createBinder()` invece di `save()`
- [x] Import inutilizzati rimossi
- [x] Nessun errore di compilazione
- [x] Code smell ridotti

---

## 🚀 Prossimi Passi

### Metodi da Implementare in Futuro

1. **`addCardToBinder()`**
   - Richiede tabella di associazione binder-carte
   - Schema DB: `binder_cards(binder_id, card_id)`

2. **Uso di altri metodi IDao**
   - `get(id)` - Quando implementeremo navigazione a binder singolo
   - `update()` - Per modificare nome/proprietà binder
   - `delete()` - Per eliminare binder

---

## 📊 Risultato Finale

### Codice Più Pulito
```java
// Prima - 4 righe, stream, filtro
List<Binder> userBinders = binderDao.getAll().stream()
        .filter(binder -> username.equals(binder.getOwner()))
        .collect(Collectors.toList());

// Dopo - 1 riga, chiaro e conciso
List<Binder> userBinders = binderDao.getUserBinders(username);
```

### Migliore Efficienza
- **JDBC**: Query filtrata nel database
- **JSON**: Lookup diretto nella mappa
- **Demo**: Lookup diretto nella mappa

### Manutenibilità
- Logica di filtraggio centralizzata nel DAO
- Controller più leggero e focalizzato
- Facile aggiungere nuove implementazioni DAO

---

## ✨ Conclusione

Il refactoring garantisce che:

✅ **Tutti i DAO implementano SOLO metodi dell'interfaccia**  
✅ **Il Controller usa SOLO metodi dell'interfaccia**  
✅ **Nessuna dipendenza da implementazioni concrete**  
✅ **Codice più pulito, efficiente e manutenibile**  

Il sistema ora rispetta completamente il principio di **Dependency Inversion** (SOLID) e il pattern **DAO** correttamente!

