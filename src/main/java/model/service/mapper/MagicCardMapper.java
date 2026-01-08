package model.service.mapper;

import model.domain.card.MagicCard;
import model.bean.MagicCardBean;

import java.util.ArrayList;
import java.util.List;

public class MagicCardMapper {

    public MagicCard toDomain(MagicCardBean bean) {
        if (bean == null) {
            return null;
        }

        MagicCard card = new MagicCard(bean.getId(), bean.getName());

        card.setManaCost(bean.getManaCost());
        card.setType(bean.getType());
        card.setText(bean.getText());
        card.setPower(bean.getPower());
        card.setToughness(bean.getToughness());

        card.setRarity(bean.getRarity());
        card.setArtist(bean.getArtist());
        card.setSetName(bean.getSetName());
        card.setNumber(bean.getNumber());
        card.setImageUrl(bean.getImageUrl());

        return card;
    }

    public List<MagicCard> toDomainList(List<MagicCardBean> beans) {
        if (beans == null) {
            return new ArrayList<>();
        }

        List<MagicCard> cards = new ArrayList<>();
        for (MagicCardBean bean : beans) {
            cards.add(toDomain(bean));
        }
        return cards;
    }
}

