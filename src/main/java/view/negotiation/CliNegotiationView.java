package view.negotiation;

import controller.NegotiationController;
import model.bean.CardBean;
import model.bean.ProposalBean;
import config.InputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("java:S106")
public class CliNegotiationView implements INegotiationView {
    private final InputManager inputManager;
    private NegotiationController controller;
    private List<CardBean> inventory = new ArrayList<>();
    private List<CardBean> requested = new ArrayList<>();
    private List<CardBean> proposed = new ArrayList<>();
    private List<String> availableStores = new ArrayList<>();
    private String meetingDateHint;

    private Consumer<CardBean> onPropose;
    private Consumer<CardBean> onUnpropose;
    private Consumer<ProposalBean> onConfirm;

    public CliNegotiationView() {
        this.inputManager = new config.InputManager();
    }

    public CliNegotiationView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void showInventory(List<CardBean> inventory) {
        this.inventory = inventory != null ? inventory : new ArrayList<>();
    }

    @Override
    public void showRequested(List<CardBean> requested) {
        this.requested = requested != null ? requested : new ArrayList<>();
    }

    @Override
    public void showProposed(List<CardBean> proposed) {
        this.proposed = proposed != null ? proposed : new ArrayList<>();
    }

    @Override
    public void setOnCardProposed(Consumer<CardBean> onPropose) {
        this.onPropose = onPropose;
    }

    @Override
    public void setOnCardUnproposed(Consumer<CardBean> onUnpropose) {
        this.onUnpropose = onUnpropose;
    }

    @Override
    public void setOnConfirmRequested(Consumer<ProposalBean> onConfirm) {
        this.onConfirm = onConfirm;
    }

    @Override
    public void showConfirmationResult(boolean success, String message) {
        System.out.println(success ? "✓ " + message : "✗ " + message);
    }

    @Override
    public void setController(NegotiationController controller) {
        this.controller = controller;
    }

    @Override
    public void setAvailableStores(List<String> storeUsernames) {
        this.availableStores = storeUsernames != null ? storeUsernames : new ArrayList<>();
    }

    @Override
    public void setMeetingDateHint(String dateHint) {
        this.meetingDateHint = dateHint;
    }

    @Override
    public void display() {
        // simple interactive loop
        while (true) {
            System.out.println("=== TRADE NEGOTIATION ===");
            System.out.println("Requested items:");
            for (int i = 0; i < requested.size(); i++) {
                System.out.printf(" %d) %s x%d\n", i + 1, requested.get(i).getName(), requested.get(i).getQuantity());
            }
            System.out.println("Your inventory:");
            for (int i = 0; i < inventory.size(); i++) {
                System.out.printf(" %d) %s x%d\n", i + 1, inventory.get(i).getName(), inventory.get(i).getQuantity());
            }
            System.out.println("Proposed items:");
            for (int i = 0; i < proposed.size(); i++) {
                System.out.printf(" %d) %s x%d\n", i + 1, proposed.get(i).getName(), proposed.get(i).getQuantity());
            }

            System.out.println("Options: 1=propose 2=unpropose 3=confirm 0=back");
            String choice = inputManager.readString().trim();
            if ("0".equals(choice)) return;
            switch (choice) {
                case "1" -> handlePropose();
                case "2" -> handleUnpropose();
                case "3" -> handleConfirm();
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void handlePropose() {
        System.out.print("Select inventory index to propose: ");
        String s = inputManager.readString().trim();
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < inventory.size()) {
                CardBean card = inventory.get(idx);
                proposed.add(card);
                if (onPropose != null) onPropose.accept(card);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void handleUnpropose() {
        System.out.print("Select proposed index to remove: ");
        String s = inputManager.readString().trim();
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < proposed.size()) {
                CardBean card = proposed.remove(idx);
                if (onUnpropose != null) onUnpropose.accept(card);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void handleConfirm() {
        // Ask for meeting place and date in CLI
        System.out.println("Available stores:");
        for (int i = 0; i < availableStores.size(); i++) {
            System.out.printf(" %d) %s\n", i + 1, availableStores.get(i));
        }
        System.out.print("Select store index: ");
        String s = inputManager.readString().trim();
        String chosenStore = null;
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < availableStores.size()) chosenStore = availableStores.get(idx);
        } catch (NumberFormatException ignored) {}
        if (chosenStore == null) {
            System.out.println("Invalid store selection");
            return;
        }
        System.out.print("Enter meeting date (YYYY-MM-DD) [hint: " + (meetingDateHint != null ? meetingDateHint : "tomorrow") + "]: ");
        String dateIn = inputManager.readString().trim();
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(dateIn);
            if (!d.isAfter(java.time.LocalDate.now())) {
                System.out.println("Date must be after today");
                return;
            }
        } catch (Exception ex) {
            System.out.println("Invalid date format");
            return;
        }

        ProposalBean bean = new ProposalBean();
        bean.setOffered(proposed);
        bean.setRequested(requested);
        bean.setFromUser(controller != null ? controller.getProposerUsername() : null);
        bean.setToUser(controller != null ? controller.getTargetOwnerUsername() : null);
        bean.setMeetingPlace(chosenStore);
        bean.setMeetingDate(dateIn);
        if (onConfirm != null) onConfirm.accept(bean);
    }

    @Override
    public void close() {
    }
}
