package net.hexuscraft.core.cooldown;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MiniPluginCooldown extends MiniPlugin<HexusPlugin> {

    private final Map<Object, List<Cooldown>> _cooldownMap;

    public MiniPluginCooldown(final HexusPlugin plugin) {
        super(plugin, "Cooldown");
        _cooldownMap = new HashMap<>();
    }

    private Cooldown getCooldown(final Object parent, final String name) {
        if (!_cooldownMap.containsKey(parent)) return null;
        for (final Cooldown cooldown : _cooldownMap.get(parent)) {
            if (!cooldown._name().equals(name)) continue;
            return cooldown;
        }
        return null;
    }

    private void addCooldown(final Object parent, final Cooldown cooldown) {
        if (!_cooldownMap.containsKey(parent)) _cooldownMap.put(parent, new ArrayList<>());
        _cooldownMap.get(parent).add(cooldown);
    }

    private Long calculateRemaining(final Long now, final Long start, final Long delay) {
        return delay - (now - start);
    }

    public boolean use(final Object parent, final String name, final Long delayMs) {
        final Cooldown cooldown = getCooldown(parent, name);
        if (cooldown != null)
            return calculateRemaining(System.currentTimeMillis(), cooldown._started(), cooldown._delayMs()) <= 0;

        addCooldown(parent, new Cooldown(name, System.currentTimeMillis(), delayMs));
        return true;
    }

    public void use(final Object parent, final String name, final Long delayMs, final CommandSender sender) {
        if (use(parent, name, delayMs)) return;

        final Cooldown cooldown = getCooldown(parent, name);
        if (cooldown == null) {
            sender.sendMessage(F.fMain(this, "Please wait before trying to use ", F.fItem(name), " again."));
            return;
        }

        sender.sendMessage(F.fMain(this, "You cannot use ", F.fItem(name), " for another ", F.fTime(calculateRemaining(System.currentTimeMillis(), cooldown._started(), cooldown._delayMs()))));
    }

}
