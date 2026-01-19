package view;

import model.bean.ProposalBean;
import java.util.List;
import java.util.function.Consumer;


public interface IManageTradeView extends IView {

    
    void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled);


    void setUsername(String username);

    void registerOnAccept(Consumer<String> onAccept);
    void registerOnDecline(Consumer<String> onDecline);
    void registerOnCancel(Consumer<String> onCancel);
    void registerOnTradeClick(Consumer<String> onTradeClick);
    void registerOnTradeNowClick(Consumer<String> onTradeNowClick);

}
