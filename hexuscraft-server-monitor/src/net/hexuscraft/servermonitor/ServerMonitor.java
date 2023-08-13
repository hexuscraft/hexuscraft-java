package net.hexuscraft.servermonitor;

import java.io.Console;

public class ServerMonitor implements Runnable {
    static String LOGGER_FILE_NAME = "manager.log";

    static Console _console;

    static boolean Running = true;

    public static void main(String[] args) {
        _console = System.console();
        _console.printf("sup" + "\n");

        ServerMonitor threaddedServerMonitor = new ServerMonitor();
        Thread thread = new Thread(threaddedServerMonitor);
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
        while (Running) {
            _console.printf("TEST\n");
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}