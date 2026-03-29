package net.hexuscraft.core.chat;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.messages.ChatAnnouncementMessage;
import net.hexuscraft.common.database.messages.ChatSupportMessage;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.command.*;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CoreChat extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_ANNOUNCEMENT,
        COMMAND_BROADCAST,
        COMMAND_HELP,
        COMMAND_SILENCE,
        COMMAND_SILENCE_SEE,
        COMMAND_SUPPORT,
        COMMAND_SUPPORT_STAFF,

        CHAT_PREFIX,

        BYPASS_SILENCE
    }

    public String CHANNEL_ANNOUNCEMENT = "ChatAnnouncement";
    public Set<CommandSender> _receivedTipSet;
    private CoreCommand _coreCommand;
    private CoreDatabase _coreDatabase;
    private CorePermission _corePermission;
    private CorePortal _corePortal;
    private boolean _chatMuted = false;

    public CoreChat(HexusPlugin plugin)
    {
        super(plugin, "Chat");

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HELP);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_SUPPORT);

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
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _corePermission = (CorePermission) dependencies.get(CorePermission.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandAnnouncement(this, _coreDatabase));
        _coreCommand.register(new CommandBroadcast(this));
        _coreCommand.register(new CommandSilence(this));
        _coreCommand.register(new CommandSupport(this, _corePermission, _coreDatabase, _corePortal));
        _coreCommand.register(new CommandSupportResponse(this, _corePermission));
        _coreCommand.register(new CommandHelp(this, _coreCommand));

        _coreDatabase._database.registerConsumer(ChatAnnouncementMessage.CHANNEL_NAME, (_, _, rawMessage) ->
        {
            ChatAnnouncementMessage parsedMessage = ChatAnnouncementMessage.fromString(rawMessage);

            _hexusPlugin.runAsync(() ->
                                  {
                                      String senderName;
                                      try
                                      {
                                          senderName = Objects.requireNonNull(PlayerSearch.offlinePlayerSearch(
                                                  parsedMessage._senderUniqueId())).getName();
                                      }
                                      catch (IOException | NullPointerException ex)
                                      {
                                          logSevere(ex);
                                          return;
                                      }

                                      _hexusPlugin.getServer()
                                                  .getOnlinePlayers()
                                                  .stream()
                                                  .filter(player -> player.hasPermission(PermissionGroup.ADMINISTRATOR.name()))
                                                  .forEach(player ->
                                                           {

                                                               player.sendMessage(F.fStaff(this,
                                                                                           F.fItem(senderName),
                                                                                           " sent an announcement to ",
                                                                                           F.fPermissionGroup(
                                                                                                   parsedMessage._permissionGroup()),
                                                                                           "."));
                                                           });
                                  });

            _hexusPlugin.getServer()
                        .getOnlinePlayers()
                        .stream()
                        .filter(player -> player.hasPermission(parsedMessage._permissionGroup().name()))
                        .forEach(player ->
                                 {
                                     //noinspection deprecation
                                     player.sendTitle(C.cYellow + "Announcement", parsedMessage._message());
                                     player.sendMessage(F.fMain("Announcement", C.cAqua + parsedMessage._message()));
                                     player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
                                 });
        });

        _coreDatabase._database.registerConsumer(ChatSupportMessage.CHANNEL_NAME, (_, _, rawMessage) ->
        {
            ChatSupportMessage messageData = ChatSupportMessage.fromString(rawMessage);

            _hexusPlugin.getServer()
                        .getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getUniqueId().equals(messageData._senderUniqueId()) ||
                                          player.hasPermission(PermissionGroup.TRAINEE.name()))
                        .forEach(player ->
                                 {
                                     player.sendMessage(C.cPurple +
                                                        messageData._serverName() +
                                                        " " +
                                                        F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(
                                                                messageData._permissionGroups())) +
                                                        " " +
                                                        messageData._username() +
                                                        C.cPurple +
                                                        " " +
                                                        messageData._message());
                                     player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                                 });
        });
    }

    @Override
    public void onDisable()
    {
        _receivedTipSet.clear();
    }

    public boolean getMuted()
    {
        return _chatMuted;
    }

    public void setMuted(boolean toggle)
    {
        _chatMuted = toggle;
        _hexusPlugin.getServer()
                    .broadcastMessage(F.fMain(this,
                                              toggle ?
                                              F.fError("The chat is now silenced.") :
                                              F.fSuccess("The chat is no longer silenced.")));
    }

    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (player.hasPermission(PERM.CHAT_PREFIX.name()))
        {
            event.setFormat(F.fChat(0,
                                    PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(
                                            player)._groups())));
        }
        else
        {
            event.setFormat(F.fChat(0));
        }

        if (_chatMuted && !player.hasPermission(PERM.BYPASS_SILENCE.name()))
        {
            event.setCancelled(true);
            player.sendMessage(F.fMain(this, F.fError("The chat is currently silenced.")));
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        _receivedTipSet.remove(event.getPlayer());
    }

}