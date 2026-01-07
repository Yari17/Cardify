package view;

import java.util.Scanner;

public class InputManager {
    private final Scanner scanner;

    public InputManager() {
        // Unico punto dove si collega System.in
        this.scanner = new Scanner(System.in);
    }

    public String readString() {
        return scanner.nextLine();
    }

    public int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.println("Inserisci un numero valido!");
            scanner.next(); // Pulisce input errato
        }
        int numero = scanner.nextInt();
        scanner.nextLine(); // Pulisce il buffer dal \n finale (Fix problema 2)
        return numero;
    }

    // NIENTE metodo close() pubblico!
    // System.in non va mai chiuso finché l'app non muore.
}