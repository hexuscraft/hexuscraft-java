package net.hexuscraft.core.cooldown;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class PluginCooldown extends MiniPlugin<HexusPlugin> {

    private Map<Object, Cooldown> _cooldownMap;

    public PluginCooldown(final HexusPlugin plugin) {
        super(plugin, "Cooldown");
        _cooldownMap = new HashMap<>();
    }

    public boolean use(final Object parent, final String name, final Long delayMs) {
        final long now = System.currentTimeMillis();

        final long remaining = now - _cooldownMap.getOrDefault(name, 0L);
        if (remaining < delayMs) {
            return false;
        }

        _cooldownMap.put(name, new Cooldown(name, now, delayMs));
        _plugin.getServer().getScheduler().runTaskLater(_plugin, () -> {
            if (!_cooldownMap.get(name).equals(now)) return;
            _cooldownMap.remove(name);
        }, delayMs);
        return true;
    }

    public void use(final Object parent, final String name, final Long delayMs, final CommandSender sender) {
        if (!use(parent, name, delayMs)) {
            sender.sendMessage(F.fMain(this, "You cannot use ", object.toString(), " for another ", F.fTime()));
        }
        return;
    }

}
