package net.hexuscraft.core.chat;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.database.messages.SupportMessage;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.command.*;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MiniPluginChat extends MiniPlugin<HexusPlugin> {

    public final String CHANNEL_ANNOUNCEMENT = "ChatAnnouncement";
    public final Set<CommandSender> _receivedTipSet;
    private MiniPluginCommand _pluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPermission _miniPluginPermission;
    private MiniPluginPortal _miniPluginPortal;
    private boolean _chatMuted = false;

    public MiniPluginChat(final HexusPlugin plugin) {
        super(plugin, "Chat");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_HELP);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SUPPORT);

        PermissionGroup.VIP._permissions.add(PERM.CHAT_PREFIX);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE_SEE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SUPPORT_STAFF);
        PermissionGroup.TRAINEE._permissions.add(PERM.BYPASS_SILENCE);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_BROADCAST);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ANNOUNCEMENT);

        _receivedTipSet = new HashSet<>();
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandAnnouncement(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandBroadcast(this));
        _pluginCommand.register(new CommandSilence(this));
        _pluginCommand.register(
                new CommandSupport(this, _miniPluginPermission, _miniPluginDatabase, _miniPluginPortal));
        _pluginCommand.register(new CommandSupportResponse(this, _miniPluginPermission));
        _pluginCommand.register(new CommandHelp(this));

        _miniPluginDatabase.registerConsumer(CHANNEL_ANNOUNCEMENT, (_, _, message) -> {
            final String[] args = message.split(",", 3);
            String senderName = args[0];
            String groupName = args[1];
            String announcementMessage = ChatColor.translateAlternateColorCodes('&', args[2]);

            PermissionGroup permissionGroup = PermissionGroup.valueOf(groupName);

            _hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                if (player.hasPermission(PermissionGroup.ADMINISTRATOR.name())) {
                    player.sendMessage(
                            F.fSub("Staff", F.fItem(senderName), " broadcast to ", F.fPermissionGroup(permissionGroup),
                                    "."));
                }

                if (!player.hasPermission(permissionGroup.name())) {
                    return;
                }

                //noinspection deprecation
                player.sendTitle(C.cYellow + "Announcement", announcementMessage);
                player.sendMessage(F.fMain("Announcement", C.cAqua + announcementMessage));
                player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
            });
        });

        _miniPluginDatabase.registerConsumer(SupportMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
            final SupportMessage messageData = SupportMessage.fromString(rawMessage);

            final String senderName;
            final PermissionGroup[] senderPermissionGroups;

            try {
                senderName = PlayerSearch.offlinePlayerSearch(messageData._senderUniqueId()).getName();
            } catch (final IOException ex) {
                logSevere(ex);
                return;
            }

            final PermissionProfile[] permissionProfiles = _miniPluginPermission._permissionProfiles.keySet().stream()
                    .filter(player -> player.getUniqueId().equals(messageData._senderUniqueId()))
                    .map(_miniPluginPermission._permissionProfiles::get).toArray(PermissionProfile[]::new);
            if (permissionProfiles.length == 1) senderPermissionGroups = permissionProfiles[0]._groups();
            else {
                final Set<PermissionGroup> permissionGroups = new HashSet<>();
                try {
                    PermissionQueries.getGroupNames(_miniPluginDatabase.getUnifiedJedis(),
                            messageData._senderUniqueId()).forEach(permissionGroupName -> {
                        try {
                            permissionGroups.add(PermissionGroup.valueOf(permissionGroupName));
                        } catch (final IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (final JedisException ex) {
                    ex.printStackTrace();
                }
                if (permissionGroups.isEmpty()) permissionGroups.add(PermissionGroup.MEMBER);
                senderPermissionGroups = permissionGroups.toArray(PermissionGroup[]::new);
            }

            _hexusPlugin.getServer().getOnlinePlayers().stream()
                    .filter(player -> player.getUniqueId().equals(messageData._senderUniqueId()) ||
                            player.hasPermission(PermissionGroup.TRAINEE.name())).forEach(player -> {
                        player.sendMessage(C.cPurple + messageData._serverName() + " " +
                                F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(senderPermissionGroups)) + " " +
                                senderName + C.cPurple + " " + messageData._message());
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                    });
        });
    }

    @Override
    public void onDisable() {
        _receivedTipSet.clear();
    }

    public boolean getMuted() {
        return _chatMuted;
    }

    public void setMuted(boolean toggle) {
        _chatMuted = toggle;
        _hexusPlugin.getServer().broadcastMessage(F.fMain(this,
                toggle ? F.fError("The chat is now silenced.") : F.fSuccess("The chat is no longer silenced.")));
    }

    @EventHandler
    private void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission(PERM.CHAT_PREFIX.name())) {
            event.setFormat(F.fChat(0, PermissionGroup.getGroupWithHighestWeight(
                    _miniPluginPermission._permissionProfiles.get(player)._groups())));
        } else {
            event.setFormat(F.fChat(0));
        }

        if (_chatMuted && !player.hasPermission(PERM.BYPASS_SILENCE.name())) {
            event.setCancelled(true);
            player.sendMessage(F.fMain(this, F.fError("The chat is currently silenced.")));
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        _receivedTipSet.remove(event.getPlayer());
    }

    public enum PERM implements IPermission {
        COMMAND_ANNOUNCEMENT, COMMAND_BROADCAST, COMMAND_HELP, COMMAND_SILENCE, COMMAND_SILENCE_SEE, COMMAND_SUPPORT, COMMAND_SUPPORT_STAFF,

        CHAT_PREFIX,

        BYPASS_SILENCE
    }

}