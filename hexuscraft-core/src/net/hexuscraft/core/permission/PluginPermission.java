package net.hexuscraft.core.permission;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.command.CommandRank;
import net.hexuscraft.core.scoreboard.PluginScoreboard;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginPermission extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_RANK,
        COMMAND_RANK_ADD,
        COMMAND_RANK_INFO,
        COMMAND_RANK_LIST,
        COMMAND_RANK_REMOVE,
        COMMAND_RANK_SET,
        OPERATOR
    }

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;
    private PluginScoreboard _pluginScoreboard;

    public final HashMap<Player, PermissionGroup> _primaryGroupMap;
    public final HashMap<Player, Set<PermissionGroup>> _secondaryGroupsMap;

    private final HashMap<Player, PermissionAttachment> _permissionAttachmentMap;

    public PluginPermission(JavaPlugin javaPlugin) {
        super(javaPlugin, "Permissions");

        _primaryGroupMap = new HashMap<>();
        _secondaryGroupsMap = new HashMap<>();
        _permissionAttachmentMap = new HashMap<>();

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_RANK);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_ADD);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_RANK_INFO);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_RANK_LIST);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_REMOVE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_SET);
        PermissionGroup.DEVELOPER._permissions.add(PERM.OPERATOR);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
        _pluginScoreboard = (PluginScoreboard) dependencies.get(PluginScoreboard.class);
    }

    @Override
    public final void onEnable() {
        for (Player player : _javaPlugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(player);
        }

        _pluginCommand.register(new CommandRank(this, _pluginDatabase, _pluginScoreboard));
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
    private void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerJoin(event.getPlayer());
    }

    private void onPlayerJoin(Player player) {
        _primaryGroupMap.put(player, PermissionGroup.DEFAULT);
        _secondaryGroupsMap.put(player, Set.of());

        JedisPooled jedis = _pluginDatabase.getJedisPooled();
        String uuidStr = player.getUniqueId().toString();

        String primaryGroupStr = jedis.get(PermissionQueries.PRIMARY(uuidStr));
        if (primaryGroupStr == null) {
            jedis.set(PermissionQueries.PRIMARY(uuidStr), _primaryGroupMap.get(player).name());
        } else {
            _primaryGroupMap.put(player, PermissionGroup.valueOf(primaryGroupStr));
        }

        _secondaryGroupsMap.put(player, jedis.smembers(PermissionQueries.GROUPS(uuidStr)).stream().map(PermissionGroup::valueOf).collect(Collectors.toSet()));

        PermissionAttachment permissionAttachment = player.addAttachment(_javaPlugin);
        _permissionAttachmentMap.put(player, permissionAttachment);

        player.setOp(false);
        setBukkitPermissions(permissionAttachment, false);

        grantPermissions(permissionAttachment, _primaryGroupMap.get(player));
        for (PermissionGroup group : _secondaryGroupsMap.get(player)) {
            grantPermissions(permissionAttachment, group);
        }

        if (!player.hasPermission(PERM.OPERATOR.name())) {
            return;
        }
        player.setOp(true);
        setBukkitPermissions(permissionAttachment, true);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer());
    }

    private void onPlayerQuit(Player player) {
        player.setOp(false);
        player.removeAttachment(_permissionAttachmentMap.get(player));

        _primaryGroupMap.remove(player);
        _secondaryGroupsMap.remove(player);
        _permissionAttachmentMap.remove(player);
    }

    private void setBukkitPermissions(PermissionAttachment attachment, boolean toggle) {
        attachment.setPermission(_pluginCommand._commandMap.getCommand("minecraft:me").getPermission(), toggle);
        attachment.setPermission(_pluginCommand._commandMap.getCommand("minecraft:tell").getPermission(), toggle);
        attachment.setPermission(_pluginCommand._commandMap.getCommand("bukkit:help").getPermission(), toggle);
        attachment.setPermission(_pluginCommand._commandMap.getCommand("bukkit:plugins").getPermission(), toggle);
        attachment.setPermission(_pluginCommand._commandMap.getCommand("bukkit:version").getPermission(), toggle);
    }

    private void grantPermissions(PermissionAttachment attachment, PermissionGroup group) {
        attachment.setPermission(group.name(), true);
        group._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));

        for (PermissionGroup parentGroup : group._parents) {
            grantPermissions(attachment, parentGroup);
            parentGroup._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));
        }
    }

}
