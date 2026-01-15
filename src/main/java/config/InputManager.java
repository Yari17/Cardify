package config;

import java.util.Scanner;

public class InputManager {
    private final Scanner scanner;

    public InputManager() {
        this.scanner = new Scanner(System.in);
    }

    public String readString() {
        return scanner.nextLine();
    }


}