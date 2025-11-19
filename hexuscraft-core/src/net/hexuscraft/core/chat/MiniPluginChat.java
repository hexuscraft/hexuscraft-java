package net.hexuscraft.core.chat;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.command.*;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MiniPluginChat extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_ANNOUNCEMENT,
        COMMAND_BROADCAST,
        COMMAND_HELP,
        COMMAND_SILENCE,
        COMMAND_SILENCE_SEE,
        COMMAND_SUPPORT,
        COMMAND_SUPPORT_RESPONSE,

        CHAT_PREFIX
    }

    public final String CHANNEL_ANNOUNCEMENT = "ChatAnnouncement";
    public final String CHANNEL_SUPPORT = "ChatSupport";
    public final String CHANNEL_SUPPORT_RESPONSE = "ChatSupportResponse";

    private MiniPluginCommand _pluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPermission _miniPluginPermission;

    private boolean _chatMuted = false;
    public final Set<CommandSender> _receivedTipSet;

    public MiniPluginChat(final HexusPlugin plugin) {
        super(plugin, "Chat");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_HELP);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SUPPORT);

        PermissionGroup.VIP._permissions.add(PERM.CHAT_PREFIX);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE_SEE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SUPPORT_RESPONSE);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_BROADCAST);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ANNOUNCEMENT);

        _receivedTipSet = new HashSet<>();
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandAnnouncement(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandBroadcast(this));
        _pluginCommand.register(new CommandSilence(this));
        _pluginCommand.register(new CommandSupport(this, _miniPluginPermission, _miniPluginDatabase));
        _pluginCommand.register(new CommandSupportResponse(this, _miniPluginPermission));
        _pluginCommand.register(new CommandHelp(this));

        _miniPluginDatabase.registerCallback(CHANNEL_ANNOUNCEMENT, new MessagedRunnable(this) {

            @Override
            public void run() {
                String message = getMessage();
                String[] args = message.split(",", 3);
                String senderName = args[0];
                String groupName = args[1];
                String announcementMessage = ChatColor.translateAlternateColorCodes('&', args[2]);

                PermissionGroup permissionGroup = PermissionGroup.valueOf(groupName);

                _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.hasPermission(PermissionGroup.ADMINISTRATOR.name())) {
                        player.sendMessage(F.fSub("Staff", F.fItem(senderName), " broadcast to ", F.fPermissionGroup(permissionGroup), "."));
                    }

                    if (!player.hasPermission(permissionGroup.name())) return;

                    //noinspection deprecation
                    player.sendTitle(C.cYellow + "Announcement", announcementMessage);
                    player.sendMessage(F.fMain("Announcement", C.cAqua + announcementMessage));
                });
            }

        });

        _miniPluginDatabase.registerCallback(CHANNEL_SUPPORT, new MessagedRunnable(this) {
            @Override
            public void run() {
                // TODO: this
            }
        });

        _miniPluginDatabase.registerCallback(CHANNEL_SUPPORT_RESPONSE, new MessagedRunnable(this) {
            @Override
            public void run() {
                // TODO: this
            }
        });
    }

    @Override
    public void onDisable() {
        _receivedTipSet.clear();
    }

    public boolean getMuted() {
        return _chatMuted;
    }

    public void setMuted(boolean toggle, boolean... sendDefaultMessage) {
        _chatMuted = toggle;
        if (sendDefaultMessage.length > 0 && sendDefaultMessage[0]) {
            if (_chatMuted) {
                _hexusPlugin.getServer().broadcastMessage(F.fMain(this) + "The global chat is now muted.");
            } else {
                _hexusPlugin.getServer().broadcastMessage(F.fMain(this) + "The global chat is no longer muted.");
            }
        }
    }

    @EventHandler
    private void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission(PERM.CHAT_PREFIX.name())) {
            event.setFormat(F.fChat(0, _miniPluginPermission._primaryGroupMap.get(player)));
        } else {
            event.setFormat(F.fChat(0));
        }

        if (!_chatMuted) return;
        event.setCancelled(true);
        player.sendMessage(F.fMain(this) + "The global chat is currently muted.");
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        _receivedTipSet.remove(event.getPlayer());
    }

}