package net.hexuscraft.core.permission;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.command.CommandRank;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginPermission extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_RANK,
        COMMAND_RANK_ADD,
        COMMAND_RANK_INFO,
        COMMAND_RANK_LIST,
        COMMAND_RANK_REMOVE,
        COMMAND_RANK_SET,
        COMMAND_RANK_CLEAR,

        OPERATOR
    }

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;

    public final HashMap<Player, PermissionGroup> _primaryGroupMap;
    public final HashMap<Player, Set<PermissionGroup>> _secondaryGroupsMap;

    private final HashMap<Player, PermissionAttachment> _permissionAttachmentMap;

    public PluginPermission(final HexusPlugin plugin) {
        super(plugin, "Permissions");

        _primaryGroupMap = new HashMap<>();
        _secondaryGroupsMap = new HashMap<>();
        _permissionAttachmentMap = new HashMap<>();

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_RANK);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_RANK_LIST);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_RANK_INFO);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.OPERATOR);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_ADD);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_REMOVE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_SET);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
    }

    @Override
    public final void onEnable() {
        for (final Player player : _plugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(player);
        }

        _pluginCommand.register(new CommandRank(this, _pluginDatabase));
    }

    @Override
    public final void onDisable() {
        _primaryGroupMap.clear();
        _secondaryGroupsMap.clear();
    }

    public final void refreshPermissions(Player player) {
        onPlayerQuit(player);
        onPlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(final PlayerJoinEvent event) {
        onPlayerJoin(event.getPlayer());
    }

    private void onPlayerJoin(final Player player) {
        _primaryGroupMap.put(player, PermissionGroup.MEMBER);
        _secondaryGroupsMap.put(player, Set.of());

        final JedisPooled jedis = _pluginDatabase.getJedisPooled();
        final String uuidStr = player.getUniqueId().toString();

        final String primaryGroupStr = jedis.get(PermissionQueries.PRIMARY(uuidStr));
        if (primaryGroupStr == null) {
            jedis.set(PermissionQueries.PRIMARY(uuidStr), _primaryGroupMap.get(player).name());
        } else {
            try {
                _primaryGroupMap.put(player, PermissionGroup.valueOf(primaryGroupStr));
            } catch(final Exception ex) {
                player.sendMessage(F.fMain(this, F.fError("Sorry, there was an error parsing your primary permission group. Please contact an administrator if this issue persists.")));
                warning(ex.getMessage());
            }
        }

        try {
            _secondaryGroupsMap.put(player, jedis.smembers(PermissionQueries.GROUPS(uuidStr)).stream().map(PermissionGroup::valueOf).collect(Collectors.toSet()));
        } catch(final Exception ex) {
            player.sendMessage(F.fMain(this, F.fError("Sorry, there was an error parsing one or more of your secondary permission groups. Please contact an administrator if this issue persists.")));
            warning(ex.getMessage());
        }

        final PermissionAttachment permissionAttachment = player.addAttachment(_plugin);
        _permissionAttachmentMap.put(player, permissionAttachment);

        player.setOp(false);
        setBukkitPermissions(permissionAttachment, false);

        grantPermissions(permissionAttachment, _primaryGroupMap.get(player));
        for (PermissionGroup group : _secondaryGroupsMap.get(player)) {
            grantPermissions(permissionAttachment, group);
        }

        if (!player.hasPermission(PERM.OPERATOR.name())) return;
        player.setOp(true);
        setBukkitPermissions(permissionAttachment, true);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer());
    }

    private void onPlayerQuit(final Player player) {
        player.setOp(false);

        if (_permissionAttachmentMap.containsKey(player))
            player.removeAttachment(_permissionAttachmentMap.get(player));

        _permissionAttachmentMap.remove(player);
        _primaryGroupMap.remove(player);
        _secondaryGroupsMap.remove(player);
    }

    private void setBukkitPermissions(final PermissionAttachment attachment, final boolean toggle) {
        attachment.setPermission("minecraft.command.me", toggle);
        attachment.setPermission("minecraft.command.tell", toggle);
        attachment.setPermission("bukkit.command.help", toggle);
        attachment.setPermission("bukkit.command.plugins", toggle);
        attachment.setPermission("bukkit.command.version", toggle);
    }

    private void grantPermissions(final PermissionAttachment attachment, final PermissionGroup group) {
        attachment.setPermission(group.name(), true);
        group._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));

        for (PermissionGroup parentGroup : group._parents) {
            grantPermissions(attachment, parentGroup);
            parentGroup._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));
        }
    }

}
