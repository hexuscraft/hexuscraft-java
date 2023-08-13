package net.hexuscraft.core.chat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.command.*;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginChat extends MiniPlugin {

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

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;
    private PluginPermission _pluginPermission;

    private boolean _chatMuted = false;

    public PluginChat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Chat");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_HELP);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SUPPORT);

        PermissionGroup.VIP._permissions.add(PERM.CHAT_PREFIX);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SILENCE_SEE);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_SUPPORT_RESPONSE);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_BROADCAST);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ANNOUNCEMENT);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginPermission = (PluginPermission) dependencies.get(PluginPermission.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
    }

    @Override
    public final void onEnable() {
        _pluginCommand.register(new CommandAnnouncement(this, _pluginDatabase));
        _pluginCommand.register(new CommandBroadcast(this));
        _pluginCommand.register(new CommandSilence(this));
        _pluginCommand.register(new CommandSupport(this, _pluginPermission));
        _pluginCommand.register(new CommandSupportResponse(this, _pluginPermission));
        _pluginCommand.register(new CommandHelp(this));

        _pluginDatabase.registerCallback(CHANNEL_ANNOUNCEMENT, new MessagedRunnable(this) {

            @Override
            public void run() {
                String message = getMessage();
                String[] args = message.split(",", 3);
                String senderName = args[0];
                String groupName = args[1];
                String announcementMessage = args[2];

                PermissionGroup permissionGroup = PermissionGroup.valueOf(groupName);

                _plugin._javaPlugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.hasPermission(PermissionGroup.ADMINISTRATOR.name())) {
                        player.sendMessage(F.fStaff() + F.fMain(this) + F.fEntity(senderName) + " broadcast to " + F.fPermissionGroup(permissionGroup) + ".");
                    }

                    if (!player.hasPermission(permissionGroup.name())) {
                        return;
                    }

                    player.sendMessage(F.fAnnouncement(announcementMessage));

                    PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                    playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(announcementMessage)));
                    playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(C.cYellow + "Announcement")));

                    /*
                    //noinspection deprecation
                    player.sendTitle(C.cYellow + "Announcement", announcementMessage);
                     */
                });
            }

        });
    }

    public final boolean getMuted() {
        return _chatMuted;
    }

    public final void setMuted(boolean toggle, boolean... sendDefaultMessage) {
        _chatMuted = toggle;
        if (sendDefaultMessage.length > 0 && sendDefaultMessage[0]) {
            if (_chatMuted) {
                _javaPlugin.getServer().broadcastMessage(F.fMain(this) + "The global chat is now muted.");
            } else {
                _javaPlugin.getServer().broadcastMessage(F.fMain(this) + "The global chat is no longer muted.");
            }
        }
    }

    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(F.fChat(0, _pluginPermission._primaryGroupMap.get(player)));

        if (!_chatMuted) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(F.fMain(this) + "The global chat is currently muted.");
    }

}