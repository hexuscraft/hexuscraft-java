package net.hexuscraft.core.permission;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.command.CommandRank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

public class CorePermission extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_RANK,
        COMMAND_RANK_ADD,
        COMMAND_RANK_INFO,
        COMMAND_RANK_LIST,
        COMMAND_RANK_REMOVE,
        COMMAND_RANK_CLEAR,

        OPERATOR
    }

    public HashMap<Player, PermissionProfile> _permissionProfiles;
    CoreCommand _coreCommand;
    CoreDatabase _coreDatabase;

    public CorePermission(HexusPlugin plugin)
    {
        super(plugin, "Permissions");

        _permissionProfiles = new HashMap<>();

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_RANK);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_RANK_LIST);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_RANK_INFO);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.OPERATOR);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_ADD);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_REMOVE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RANK_CLEAR);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandRank(this, _coreDatabase));
    }

    @Override
    public void onDisable()
    {
        _hexusPlugin.getServer().getOnlinePlayers().forEach(this::clearPermissions);
        _permissionProfiles.forEach((_, permissionProfile) -> permissionProfile._attachment().remove());
        _permissionProfiles.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();

        Set<String> permissionGroupNames;
        try
        {
            permissionGroupNames =
                    _coreDatabase._database._jedis.smembers(PermissionQueries.GROUPS(player.getUniqueId()));
        }
        catch (JedisException ex)
        {
            logSevere(ex);
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(
                    "There was an error while fetching your permission groups. Please try again later or contact an " +
                            "administrator if this issue persists.");
            return;
        }

        Set<PermissionGroup> permissionGroups =
                new HashSet<>(permissionGroupNames.stream().map(PermissionGroup::valueOf).toList());

        permissionGroups.add(PermissionGroup._PLAYER);

        _permissionProfiles.put(player,
                new PermissionProfile(permissionGroups.toArray(PermissionGroup[]::new),
                        player.addAttachment(_hexusPlugin)));
        refreshPermissions(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
        // We want to remove the permission profiles after other MiniPlugins have finished their things
    void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        clearPermissions(player);
        _permissionProfiles.remove(player);
    }

    public void refreshPermissions(Player player)
    {
        clearPermissions(player);

        PermissionProfile profile = _permissionProfiles.get(player);
        if (profile == null)
        {
            logWarning("Unable to grant permissions for player '" +
                    player.getName() +
                    "' as they have no permission profile.");
            return;
        }

        PermissionAttachment attachment = profile._attachment();
        Arrays.stream(profile._groups()).forEach(group -> grantPermissions(attachment, group));

        if (player.hasPermission(PERM.OPERATOR.name()))
        {
            player.setOp(true);
        }
        else
        {
            denyBukkitPermissions(attachment);
        }
    }

    void clearPermissions(Player player)
    {
        player.setOp(false);

        PermissionProfile profile = _permissionProfiles.get(player);
        if (profile == null)
        {
            logWarning("Unable to clear permissions for player '" +
                    player.getName() +
                    "' as they have no permission profile.");
            return;
        }

        PermissionAttachment attachment = profile._attachment();
        attachment.getPermissions().forEach((s, _) -> attachment.unsetPermission(s));
    }

    void denyBukkitPermissions(PermissionAttachment attachment)
    {
        attachment.setPermission("minecraft.command.me", false);
        attachment.setPermission("minecraft.command.tell", false);
        attachment.setPermission("bukkit.command.help", false);
        attachment.setPermission("bukkit.command.plugins", false);
        attachment.setPermission("bukkit.command.version", false);
    }

    void grantPermissions(PermissionAttachment attachment, PermissionGroup group)
    {
        attachment.setPermission(group.name(), true);
        group._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(), true));

        Arrays.stream(group._inherits).forEach(parentGroup ->
        {
            grantPermissions(attachment, parentGroup);
            parentGroup._permissions.forEach(basePermission -> attachment.setPermission(basePermission.toString(),
                    true));
        });
    }

}
