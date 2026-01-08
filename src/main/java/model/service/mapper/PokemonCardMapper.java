package model.service.mapper;

import model.domain.card.PokemonCard;
import model.bean.PokemonCardBean;

import java.util.ArrayList;
import java.util.List;

public class PokemonCardMapper {

    public PokemonCard toDomain(PokemonCardBean bean) {
        if (bean == null) {
            return null;
        }

        PokemonCard card = new PokemonCard(bean.getId(), bean.getName());

        card.setHp(bean.getHp());

        if (bean.getTypes() != null) {
            card.setTypes(new ArrayList<>(bean.getTypes()));
        }

        card.setEvolvesFrom(bean.getEvolvesFrom());

        card.setRarity(bean.getRarity());

        card.setArtist(bean.getArtist());

        if (bean.getSet() != null) {
            card.setSetName(bean.getSet().getName());
        }
        card.setNumber(bean.getNumber());

        if (bean.getImages() != null) {
            String imageUrl = bean.getImages().getLarge() != null
                ? bean.getImages().getLarge()
                : bean.getImages().getSmall();
            card.setImageUrl(imageUrl);
        }

        if (bean.getAttacks() != null) {
            List<PokemonCard.Attack> attacks = new ArrayList<>();
            for (PokemonCardBean.AttackBean attackBean : bean.getAttacks()) {
                PokemonCard.Attack attack = new PokemonCard.Attack(
                    attackBean.getName(),
                    attackBean.getDamage()
                );
                attack.setText(attackBean.getText());
                attacks.add(attack);
            }
            card.setAttacks(attacks);
        }

        return card;
    }

    public List<PokemonCard> toDomainList(List<PokemonCardBean> beans) {
        if (beans == null) {
            return new ArrayList<>();
        }

        List<PokemonCard> cards = new ArrayList<>();
        for (PokemonCardBean bean : beans) {
            cards.add(toDomain(bean));
        }
        return cards;
    }
}

