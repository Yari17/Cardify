package model.bean.mapper;

import model.bean.NotificationBean;
import model.domain.Notification;

/**
 * Classe di utility per la conversione tra l'entità {@link Notification} e
 * {@link NotificationBean}.
 * Gestisce il passaggio dei dati di stato e dei messaggi per la
 * visualizzazione.
 */
public class NotificationMapper {

    /**
     * Costruttore privato per nascondere quello pubblico di default.
     */
    private NotificationMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converte un'entità di dominio Notification in un NotificationBean.
     * 
     * @param entity L'entità da convertire.
     * @return Il bean corrispondente.
     */
    public static NotificationBean toBean(Notification entity) {
        if (entity == null) {
            return null;
        }
        NotificationBean bean = new NotificationBean();
        bean.setId(entity.getId());
        bean.setUserId(entity.getUserId());
        bean.setMessage(entity.getMessage());
        bean.setRead(entity.isRead());
        return bean;
    }

    /**
     * Converte un NotificationBean in un'entità di dominio Notification.
     * 
     * @param bean Il bean da convertire.
     * @return L'entità di dominio corrispondente.
     */
    public static Notification toEntity(NotificationBean bean) {
        if (bean == null) {
            return null;
        }
        return new Notification(bean.getId(), bean.getUserId(), bean.getMessage(), bean.isRead());
    }
}
