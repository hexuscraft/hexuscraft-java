package net.hexuscraft.servermonitor;

import net.hexuscraft.servermonitor.database.PluginDatabase;

import java.io.Console;

public class ServerMonitor implements Runnable {

    public static void main(String[] args) {
        new ServerMonitor();
    }

    private final Console _console;
    private final PluginDatabase _database;

    private ServerMonitor() {
        _console = System.console();
        _database = new PluginDatabase();
        new Thread(this).start();
    }

    private void log(String message, Object... args) {
        _console.printf(message + "\n", args);
    }

    private void tick() {
    }

    @Override
    public final void run() {
        while (true) {
            try {
                tick();
                //noinspection BusyWait
                Thread.sleep(1);
            } catch (Exception ex) {
                log(ex.getMessage(), ex);
                break;
            }
        }
    }

}