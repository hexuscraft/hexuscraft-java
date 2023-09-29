package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;

public class MessagedRunnable implements Runnable {

    public final MiniPlugin _plugin;

    private String _message;

    protected MessagedRunnable(MiniPlugin plugin) {
        _plugin = plugin;
    }

    public final void setMessage(String message) {
        _message = message;
    }

    public final String getMessage() {
        return _message;
    }

    @Override
    public void run() {
    }

    @Override
    public String toString() {
        return _plugin._name;
    }

}
