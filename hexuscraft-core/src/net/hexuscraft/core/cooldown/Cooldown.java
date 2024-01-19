package net.hexuscraft.core.cooldown;

public class Cooldown {

    public String _name;
    public long _started;
    public long _delayMs;

    public Cooldown(final String name, final long started, final long delayMs) {
        _name = name;
        _started = started;
        _delayMs = delayMs;
    }

}
