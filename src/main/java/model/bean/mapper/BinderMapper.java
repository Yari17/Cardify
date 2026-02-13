package model.bean.mapper;

import model.bean.BinderBean;
import model.domain.Binder;

/**
 * Classe di utility per la conversione tra l'entità {@link Binder} e
 * {@link BinderBean}.
 * Gestisce il passaggio del contenuto del raccoglitore mantenendo il
 * riferimento agli item.
 */
public class BinderMapper {

    /**
     * Costruttore privato per nascondere quello pubblico di default.
     */
    private BinderMapper() {
    }

    /**
     * Converte un oggetto di dominio {@link Binder} in un {@link BinderBean}.
     * Delega il riferimento alla lista di {@link CollectionItem} direttamente al
     * bean.
     * 
     * @param binder Il raccoglitore di dominio da mappare.
     * @return L'istanza di BinderBean popolata o null.
     */
    public static BinderBean toBean(Binder binder) {
        if (binder == null) {
            return null;
        }
        BinderBean bean = new BinderBean();
        bean.setOwner(binder.getOwner());
        bean.setSetID(binder.getSetID());
        bean.setSetName(binder.getSetName());
        // Passaggio diretto del riferimento per efficienza nel contesto corrente
        bean.setOwnedCards(binder.getOwnedCards());
        return bean;
    }

    /**
     * Converte un {@link BinderBean} nell'entità di dominio {@link Binder}.
     * 
     * @param bean Il bean contenente i dati del raccoglitore.
     * @return L'entità di dominio creata o null.
     */
    public static Binder toDomain(BinderBean bean) {
        if (bean == null) {
            return null;
        }
        return new Binder(bean.getOwner(), bean.getSetID(), bean.getSetName(), bean.getOwnedCards());
    }
}
