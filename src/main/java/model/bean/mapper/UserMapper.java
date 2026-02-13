package model.bean.mapper;

import model.bean.UserBean;
import model.domain.User;

/**
 * Classe di utility per la conversione tra l'entità {@link User} e il relativo
 * {@link UserBean}.
 * Garantisce l'isolamento del modello di dominio dalle View.
 */
public class UserMapper {

    /**
     * Costruttore privato per prevenire l'istanziazione di una classe di utility.
     */
    private UserMapper() {
    }

    /**
     * Map un oggetto di dominio {@link User} in un {@link UserBean}.
     * 
     * @param user L'utente da mappare.
     * @return Il Bean popolato o null.
     */
    public static UserBean toBean(User user) {
        if (user == null) {
            return null;
        }
        UserBean bean = new UserBean();
        bean.setUsername(user.getUsername());
        bean.setPassword(user.getPassword());
        bean.setUserType(user.getUserType());
        return bean;
    }

    /**
     * Map un {@link UserBean} nell'oggetto di dominio {@link User}.
     * 
     * @param bean Il Bean contenente i dati utente.
     * @return L'entità di dominio creata o null.
     */
    public static User toDomain(UserBean bean) {
        if (bean == null) {
            return null;
        }
        return new User(bean.getUsername(), bean.getPassword(), bean.getUserType());
    }
}
