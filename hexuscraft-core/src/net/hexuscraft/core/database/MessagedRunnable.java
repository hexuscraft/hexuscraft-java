package net.hexuscraft.core.database;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;

public class MessagedRunnable implements Runnable {

    public final MiniPlugin<? extends HexusPlugin> _miniPlugin;

    private String _message;

    protected MessagedRunnable(MiniPlugin<? extends HexusPlugin> plugin) {
        _miniPlugin = plugin;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    @Override
    public void run() {
    }

    @Override
    public String toString() {
        return _miniPlugin._name;
    }

}
