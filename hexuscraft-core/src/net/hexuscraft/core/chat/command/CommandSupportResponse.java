package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.database.PubSubConsumer;
import net.hexuscraft.common.database.messages.ChatSupportResponseReceivedMessage;
import net.hexuscraft.common.database.messages.ChatSupportResponseSentMessage;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.actionbar.ActionBar;
import net.hexuscraft.core.actionbar.CoreActionBar;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandSupportResponse extends BaseCommand<CoreChat>
{

    CorePermission _corePermission;
    CoreDatabase _coreDatabase;
    CorePortal _corePortal;
    CoreActionBar _coreActionBar;

    public CommandSupportResponse(CoreChat coreChat,
            CorePermission corePermission,
            CoreDatabase coreDatabase,
            CorePortal corePortal,
            CoreActionBar coreActionBar)
    {
        super(coreChat,
                "supportresponse",
                "<Player> <Message>",
                "Respond to a help request.",
                Set.of("ma", "sr"),
                CoreChat.PERM.COMMAND_SUPPORT_STAFF);
        _corePermission = corePermission;
        _coreDatabase = coreDatabase;
        _corePortal = corePortal;
        _coreActionBar = coreActionBar;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length < 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        AtomicReference<ActionBar> atomicActionBar = new AtomicReference<>();
        if (sender instanceof Player player)
        {
            atomicActionBar.set(_coreActionBar.registerActionBar(new ActionBar(player,
                    0,
                    F.fMain(this, "Fetching offline player for ", F.fItem(args[0]), "..."))));
        }

        OfflinePlayer target = PlayerSearch.offlinePlayerSearch(args[0], sender);
        if (target == null)
        {
            if (atomicActionBar.get() != null)
            {
                _coreActionBar.unregisterActionBar(atomicActionBar.get());
            }
            return;
        }

        if (atomicActionBar.get() instanceof ActionBar actionBar)
        {
            actionBar.message().set(F.fMain(this, "Sending support response to ", F.fItem(target.getName()), "..."));
        }

        UUID uuid = UUID.randomUUID();

        AtomicReference<PubSubConsumer> atomicConsumer = new AtomicReference<>();

        BukkitTask task = _miniPlugin._hexusPlugin.runSyncLater(() ->
        {
            if (atomicConsumer.get() instanceof PubSubConsumer consumer)
            {
                _coreDatabase._database.unregisterConsumer(consumer);
            }
            if (atomicActionBar.get() instanceof ActionBar actionBar)
            {
                _coreActionBar.unregisterActionBar(actionBar);
            }
            sender.sendMessage(F.fMain(this,
                    F.fError("Timed out while sending your support message. Please try again later or contact an " +
                            "administrator if this issue persists.")));
        }, 100);

        PubSubConsumer consumer = (pattern, channelName, rawMessage) ->
        {
            ChatSupportResponseReceivedMessage parsedMessage =
                    ChatSupportResponseReceivedMessage.fromString(rawMessage);

            if (!parsedMessage._uuid().equals(uuid))
            {
                return;
            }

            if (atomicConsumer.get() instanceof PubSubConsumer consumer1)
            {
                _coreDatabase._database.unregisterConsumer(consumer1);
            }
            if (atomicActionBar.get() instanceof ActionBar actionBar)
            {
                _coreActionBar.unregisterActionBar(actionBar);
            }
            task.cancel();
        };

        atomicConsumer.set(consumer);
        _coreDatabase._database.registerConsumer(ChatSupportResponseReceivedMessage.CHANNEL_NAME, consumer);

        _miniPlugin._hexusPlugin.runAsync(() ->
        {
            try
            {
                _coreDatabase._database._jedis.publish(ChatSupportResponseSentMessage.CHANNEL_NAME,
                        new ChatSupportResponseSentMessage(uuid,
                                sender instanceof Player player ? player.getUniqueId() : UtilUniqueId.EMPTY_UUID,
                                sender.getName(),
                                _corePortal._serverName,
                                sender instanceof Player player ?
                                        _corePermission._permissionProfiles.get(player)._groups() :
                                        new PermissionGroup[]{PermissionGroup._CONSOLE},
                                target.getUniqueId(),
                                target.getName(),
                                String.join(" ", Arrays.stream(args).skip(1).toArray(String[]::new))).toString());
            }
            catch (JedisException ex)
            {
                sender.sendMessage(F.fMain(this,
                        F.fError("An error occurred while sending your support message. Please try again later or " +
                                "contact an administrator if this issue persists.")));
                _miniPlugin.logSevere(ex);
            }
        });
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 1)
        {
            return List.of();
        }

        return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                sender,
                false);
    }

}
