package view.managetrade;

import view.IView;
import model.bean.ProposalBean;
import java.util.List;
import controller.ManageTradeController;

public interface IManageTradeView extends IView {
    void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled);

    void setUsername(String username);

    // Allow wiring of the ManageTradeController so the view can invoke accept/decline actions
    void setManageController(ManageTradeController controller);
    void onAcceptTradeProposal(String id);
    void onCancelTradeProposal(String id);
    void onDeclineTradeProposal(String id);
    void onTradeClick(String id);
    void onTradeNowClick(String id);
}
