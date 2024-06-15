package net.hexuscraft.core.cooldown;

public class Cooldown {

    public final String _name;
    public final long _started;
    public final long _delayMs;

    public Cooldown(final String name, final long started, final long delayMs) {
        _name = name;
        _started = started;
        _delayMs = delayMs;
    }

}
