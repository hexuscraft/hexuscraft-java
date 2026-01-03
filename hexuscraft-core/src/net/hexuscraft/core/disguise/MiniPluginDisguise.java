package net.hexuscraft.core.disguise;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.disguise.command.CommandDisguise;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public final class MiniPluginDisguise extends MiniPlugin<HexusPlugin> {

    private MiniPluginCommand _miniPluginCommand;

    public MiniPluginDisguise(final HexusPlugin plugin) {
        super(plugin, "Disguise");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandDisguise(this));
    }

    public void disguise(final Player targetPlayer, final OfflinePlayer disguiseOfflinePlayer) {
        targetPlayer.setDisplayName(disguiseOfflinePlayer.getName());
        targetPlayer.setCustomName(disguiseOfflinePlayer.getName());
        _hexusPlugin.getServer().getOnlinePlayers().forEach(player -> player.hidePlayer(targetPlayer));
        _hexusPlugin.getServer().getOnlinePlayers().forEach(player -> player.showPlayer(targetPlayer));
    }

    public enum PERM implements IPermission {
        COMMAND_DISGUISE
    }
}
