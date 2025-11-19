package net.hexuscraft.core.permission;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.command.CommandRank;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

public final class MiniPluginPermission extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_RANK, COMMAND_RANK_ADD, COMMAND_RANK_INFO, COMMAND_RANK_LIST, COMMAND_RANK_REMOVE, COMMAND_RANK_SET, COMMAND_RANK_CLEAR,

        OPERATOR
    }

    private MiniPluginPortal _miniPluginPortal;
    private MiniPluginCommand _miniPluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;

    public final HashMap<Player, PermissionGroup> _primaryGroupMap;
    public final HashMap<Player, Set<PermissionGroup>> _secondaryGroupsMap;

    private final HashMap<Player, PermissionAttachment> _permissionAttachmentMap;

    public MiniPluginPermission(final HexusPlugin plugin) {
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
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_CLEAR);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandRank(this, _miniPluginDatabase));
        _hexusPlugin.getServer().getOnlinePlayers().forEach(this::loadPermissionsAsync);
    }

    @Override
    public void onDisable() {
        _hexusPlugin.getServer().getOnlinePlayers().forEach(this::clearPermissions);
        _primaryGroupMap.clear();
        _secondaryGroupsMap.clear();
        _permissionAttachmentMap.forEach((_, permissionAttachment) -> permissionAttachment.remove());
        _permissionAttachmentMap.clear();
    }

    public void refreshPermissions(Player player) {
        clearPermissions(player);
        loadPermissionsAsync(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(final PlayerJoinEvent event) {
        loadPermissionsAsync(event.getPlayer());
    }

    @SuppressWarnings("UnusedReturnValue")
    private BukkitTask loadPermissionsAsync(final Player player) {
        _primaryGroupMap.put(player, PermissionGroup.MEMBER);
        _secondaryGroupsMap.put(player, Set.of());

        final JedisPooled redis = _miniPluginDatabase.getJedisPooled();
        final UUID uuid = player.getUniqueId();

        return _hexusPlugin.runAsync(() -> {
            try {
                final String primaryGroupName = redis.get(PermissionQueries.PRIMARY(uuid));
                if (!player.isOnline()) return; // Player might have left before the redis query finishes

                try {
                    _primaryGroupMap.put(player, PermissionGroup.valueOf(primaryGroupName));
                } catch (final IllegalArgumentException ex) {
                    _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this, F.fError("Sorry, you currently possess a primary permission group (", F.fItem(primaryGroupName), ") that does not exist on this server. (", F.fItem(_miniPluginPortal._serverName), "). You will temporarily possess the ", F.fPermissionGroup(_primaryGroupMap.get(player)), " primary group for the duration of this session. Please contact an administrator if this issue persists. Apologies for any inconvenience caused."))));
                    logWarning("Player '" + player.getName() + "' (" + player.getUniqueId() + ") has primary group '" + primaryGroupName + "' that does not exist on this server. They have been assigned primary group '" + _primaryGroupMap.get(player).name() + "' for this session.");
                }
            } catch (final JedisException ex) {
                _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this, F.fError("Sorry, we were unable to fetch your primary permission group. You will temporarily possess the ", F.fPermissionGroup(_primaryGroupMap.get(player)), " primary group for the duration of this session. Please contact an administrator if this issue persists. Apologies for any inconvenience caused."))));
                logWarning("Unable to fetch primary permission group of player '" + player.getName() + "' (" + player.getUniqueId() + "). They have been assigned primary group '" + _primaryGroupMap.get(player).name() + "' for this session.");
            }

            try {
                final Set<String> secondaryGroupNames = redis.smembers(PermissionQueries.GROUPS(uuid));
                if (!player.isOnline()) return; // Player might have left before the redis query finishes

                secondaryGroupNames.forEach(secondaryGroupName -> {
                    try {
                        _secondaryGroupsMap.get(player).add(PermissionGroup.valueOf(secondaryGroupName));
                    } catch (final IllegalArgumentException ex) {
                        _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this, F.fError("Sorry, you currently possess a secondary permission group (", F.fItem(secondaryGroupName), ") that does not exist on this server. (", F.fItem(_miniPluginPortal._serverName), "). You will not be granted this secondary permission group for this session. Please contact an administrator if this issue persists. Apologies for any inconvenience caused."))));
                        logWarning("Player '" + player.getName() + "' (" + player.getUniqueId() + ") has secondary group '" + secondaryGroupName + "' that does not exist on this server. They will not be granted this secondary group for this session.");
                    }
                });
            } catch (final JedisException ex) {
                _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this, F.fError("Sorry, we were unable to fetch your secondary permission groups. You will not be assigned any secondary permission groups for the duration of this session. Please contact an administrator if this issue persists. Apologies for any inconvenience caused."))));
                logWarning("Unable to fetch secondary permission groups of player '" + player.getName() + "' (" + player.getUniqueId() + "). They will not be assigned any secondary groups for this session.");
            }

            _hexusPlugin.runSync(() -> {
                final PermissionAttachment permissionAttachment = player.addAttachment(_hexusPlugin);
                _permissionAttachmentMap.put(player, permissionAttachment);

                player.setOp(false);
                setBukkitPermissions(permissionAttachment, false);

                grantPermissions(permissionAttachment, _primaryGroupMap.get(player));
                _secondaryGroupsMap.get(player).forEach(permissionGroup -> grantPermissions(permissionAttachment, permissionGroup));

                if (!player.hasPermission(PERM.OPERATOR.name())) return;
                player.setOp(true);
                setBukkitPermissions(permissionAttachment, true);
            });
        });

    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        clearPermissions(event.getPlayer());
    }

    private void clearPermissions(final Player player) {
        player.setOp(false);

        if (_permissionAttachmentMap.containsKey(player)) {
            final PermissionAttachment permissionAttachment = _permissionAttachmentMap.get(player);
            player.removeAttachment(permissionAttachment);
            permissionAttachment.remove();
        }

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

        Arrays.stream(group._parents).forEach(parentGroup -> {
            grantPermissions(attachment, parentGroup);
            parentGroup._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));
        });
    }

}
