package net.hexuscraft.servermanager;

import java.io.Console;

public class ServerManager implements Runnable {
    static String LOGGER_FILE_NAME = "manager.log";

    static Console _console;

    public static void main(String[] args) {
        _console = System.console();
        _console.printf("sup" + "\n");

        ServerManager threaddedServerManager = new ServerManager();
        Thread thread = new Thread(threaddedServerManager);
        thread.start();

        String input = _console.readLine("> ");

        _console.printf("INPUT: " + input + "\n");
        char[] pass = _console.readPassword("> ");
        _console.printf("PASS: " + String.valueOf(pass) + "\n");
        _console.readLine();
    }

    @Override
    public void run() {
//        Console console = System.console();
        while (true) {
            _console.printf("TEST\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}