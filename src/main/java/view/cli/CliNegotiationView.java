package view.cli;

import controller.NegotiationController;
import model.bean.CardBean;
import model.bean.ProposalBean;
import config.InputManager;
import org.jetbrains.annotations.NotNull;
import view.INegotiationView;

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
    // store last CLI inputs so controller can read them via getters
    private String lastSelectedStore;
    private String lastMeetingDateInput;
    private String lastMeetingTimeInput;

    private Consumer<CardBean> onPropose;
    private Consumer<CardBean> onUnpropose;
    private Consumer<ProposalBean> onConfirm;

    // Reused format and message constants to avoid duplication
    private static final String ITEM_LINE_FMT = " %d) %s x%d%n";
    private static final String INVALID_INDEX_MSG = "Invalid index";

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
        this.requested = new ArrayList<>();
        if (requested != null) {
            for (CardBean cb : requested) {
                CardBean copy = new CardBean(cb);
                copy.setQuantity(1); // requested card must be one unit
                this.requested.add(copy);
            }
        }
    }

    @Override
    public void showProposed(List<CardBean> proposed) {
        this.proposed = proposed != null ? proposed : new ArrayList<>();
    }

    @Override
    public void registerOnCardProposed(Consumer<CardBean> onPropose) {
        this.onPropose = onPropose;
    }

    @Override
    public void registerOnCardUnproposed(Consumer<CardBean> onUnpropose) {
        this.onUnpropose = onUnpropose;
    }

    @Override
    public void registerOnConfirmRequested(Consumer<ProposalBean> onConfirm) {
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
                System.out.printf(ITEM_LINE_FMT, i + 1, requested.get(i).getName(), requested.get(i).getQuantity());
            }
            System.out.println("Your inventory:");
            for (int i = 0; i < inventory.size(); i++) {
                System.out.printf(ITEM_LINE_FMT, i + 1, inventory.get(i).getName(), inventory.get(i).getQuantity());
            }
            System.out.println("Proposed items:");
            for (int i = 0; i < proposed.size(); i++) {
                System.out.printf(ITEM_LINE_FMT, i + 1, proposed.get(i).getName(), proposed.get(i).getQuantity());
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
        } catch (NumberFormatException _) {System.out.println(INVALID_INDEX_MSG);}    }

    private void handleUnpropose() {
        System.out.print("Select proposed index to remove: ");
        String s = inputManager.readString().trim();
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < proposed.size()) {
                CardBean card = proposed.remove(idx);
                if (onUnpropose != null) onUnpropose.accept(card);
            }
        } catch (NumberFormatException _) {System.out.println(INVALID_INDEX_MSG);}    }

    private void handleConfirm() {
        // Ask for meeting place and date in CLI
        System.out.println("Available stores:");
        for (int i = 0; i < availableStores.size(); i++) {
            System.out.printf(ITEM_LINE_FMT, i + 1, availableStores.get(i), 1);
        }
        System.out.print("Select store index: ");
        String s = inputManager.readString().trim();
        String chosenStore = null;
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < availableStores.size()) chosenStore = availableStores.get(idx);
        } catch (NumberFormatException _) {System.out.println(INVALID_INDEX_MSG);}
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
        } catch (Exception _) {
            System.out.println("Invalid date format");
            return;
        }

        // Prompt optional time HH:mm
        System.out.print("Enter meeting time (HH:mm) [optional, press ENTER to skip]: ");
        String timeIn = inputManager.readString().trim();
        if (timeIn.isEmpty()) timeIn = null;

        // store last inputs for getters
        this.lastSelectedStore = chosenStore;
        this.lastMeetingDateInput = dateIn;
        this.lastMeetingTimeInput = timeIn;

        ProposalBean bean = getProposalBean(chosenStore, dateIn, timeIn);
        if (onConfirm != null) onConfirm.accept(bean);
    }

    @NotNull
    private ProposalBean getProposalBean(String chosenStore, String dateIn, String timeIn) {
        ProposalBean bean = new ProposalBean();
        bean.setOffered(new ArrayList<>(proposed));
        // ensure requested copies are quantity 1
        List<CardBean> requestedCopies = new ArrayList<>();
        for (CardBean cb : requested) {
            CardBean copy = new CardBean(cb);
            copy.setQuantity(1);
            requestedCopies.add(copy);
        }
        bean.setRequested(requestedCopies);
        bean.setFromUser(controller != null ? controller.getProposerUsername() : null);
        bean.setToUser(controller != null ? controller.getTargetOwnerUsername() : null);
        bean.setMeetingPlace(chosenStore);
        bean.setMeetingDate(dateIn);
        if (timeIn != null && !timeIn.isEmpty()) {
            try {
                java.time.LocalTime.parse(timeIn);
                bean.setMeetingTime(timeIn);
            } catch (Exception _) {
                System.out.println("Invalid time format, ignoring time");
                bean.setMeetingTime(null);
            }
        }
        return bean;
    }

    @Override
    public void close() {
        // nothing to close for CLI
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    @Override
    public void refresh() {
        // CLI: refresh does not automatically start the interactive loop; caller may call display().
    }

    @Override
    public void setStage(javafx.stage.Stage stage) {
        // CLI does not use JavaFX stages; present for interface compatibility.
    }

    // GETTERS for controller to read current inputs
    @Override
    public List<CardBean> getProposedCards() {
        return new ArrayList<>(proposed);
    }

    @Override
    public List<CardBean> getRequestedCards() {
        List<CardBean> copies = new ArrayList<>();
        for (CardBean cb : requested) {
            CardBean copy = new CardBean(cb);
            copy.setQuantity(1);
            copies.add(copy);
        }
        return copies;
    }

    @Override
    public String getSelectedStore() { return lastSelectedStore; }

    @Override
    public String getMeetingDateInput() { return lastMeetingDateInput; }

    @Override
    public String getMeetingTimeInput() { return lastMeetingTimeInput; }
}
