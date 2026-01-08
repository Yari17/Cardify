package model.domain.card.adapter;

import model.bean.CardBean;

import java.util.List;

public interface ICardApiAdapter<T extends CardBean> {
    List<CardBean> search(String query);
    List<CardBean> searchSet(String setID);
    CardBean getCardById(String id);
    T getCardDetails(String id);
}
