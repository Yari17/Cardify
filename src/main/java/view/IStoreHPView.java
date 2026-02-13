package view;

import model.bean.TradeSessionBean;
import java.util.List;

public interface IStoreHPView extends IView {
    void setStoreName(String name);

    void showOngoingTrades(List<TradeSessionBean> trades);

    void showScheduledTrades(List<TradeSessionBean> trades);

    void showHistoryTrades(List<TradeSessionBean> trades);
}
