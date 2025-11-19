package net.hexuscraft.core.database;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;

import java.util.concurrent.atomic.AtomicReference;

public class MessagedRunnable implements Runnable {

    public final MiniPlugin<? extends HexusPlugin> _miniPlugin;

    private final AtomicReference<String> _pattern = new AtomicReference<>(null);
    private final AtomicReference<String> _channelName = new AtomicReference<>(null);
    private final AtomicReference<String> _message = new AtomicReference<>(null);

    protected MessagedRunnable(final MiniPlugin<? extends HexusPlugin> plugin) {
        _miniPlugin = plugin;
    }

    public String getPattern() {
        return _pattern.get();
    }

    public String getChannelName() {
        return _channelName.get();
    }

    public String getMessage() {
        return _message.get();
    }

    public void setPattern(final String pattern) {
        _pattern.set(pattern);
    }

    public void setChannelName(final String channelName) {
        _channelName.set(channelName);
    }

    public void setMessage(final String message) {
        _message.set(message);
    }

    @Override
    public void run() {
    }

    @Override
    public String toString() {
        System.out.println(this.getClass());
        return _miniPlugin._prefix;
    }

}
