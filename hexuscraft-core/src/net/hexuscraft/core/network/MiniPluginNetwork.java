package net.hexuscraft.core.network;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.command.CommandNetwork;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.ByteArrayDataInputRunnable;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MiniPluginNetwork extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_NETWORK,
        COMMAND_NETWORK_GROUP,
        COMMAND_NETWORK_GROUP_CREATE,
        COMMAND_NETWORK_GROUP_DELETE,
        COMMAND_NETWORK_GROUP_LIST,
        COMMAND_NETWORK_SERVER,
        COMMAND_NETWORK_SPY
    }

    private MiniPluginCommand _miniPluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;

    public Set<CommandSender> _spyingPlayers;

    public MiniPluginNetwork(final HexusPlugin plugin) {
        super(plugin, "Network");
        _spyingPlayers = new HashSet<>();

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_GROUP);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_GROUP_CREATE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_GROUP_DELETE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_GROUP_LIST);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_SERVER);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETWORK_SPY);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandNetwork(this, _miniPluginDatabase));
        _miniPluginDatabase.registerCallback("NetworkSpy", new MessagedRunnable(this) {
            @Override
            public void run() {
                _spyingPlayers.forEach(commandSender -> commandSender.sendMessage(F.fSub(this, getMessage())));
            }
        });
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (!_spyingPlayers.contains(player)) return;
        _spyingPlayers.remove(player);
    }

}
