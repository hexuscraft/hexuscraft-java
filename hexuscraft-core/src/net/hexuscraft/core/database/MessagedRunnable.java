package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;

public class MessagedRunnable implements Runnable {

    public final MiniPlugin _plugin;

    private String message;

    protected MessagedRunnable(MiniPlugin plugin) {
        _plugin = plugin;
    }

    public final void setMessage(String message) {
        this.message = message;
    }

    public final String getMessage() {
        return message;
    }

    @Override
    public void run() {}

    @Override
    public String toString() {
        return _plugin._name;
    }

}
