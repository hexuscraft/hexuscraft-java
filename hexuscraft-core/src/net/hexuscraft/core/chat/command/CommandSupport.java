package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.database.messages.SupportMessage;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public final class CommandSupport extends BaseCommand<MiniPluginChat> {

    private final MiniPluginPortal _miniPluginPortal;
    private final MiniPluginPermission _miniPluginPermission;
    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandSupport(final MiniPluginChat miniPluginChat, final MiniPluginPermission miniPluginPermission,
                          final MiniPluginDatabase miniPluginDatabase, final MiniPluginPortal miniPluginPortal) {
        super(miniPluginChat, "support", "<Message>", "Request help from a staff member.",
                Set.of("a", "admin", "helpop", "sc", "staffchat"), MiniPluginChat.PERM.COMMAND_SUPPORT);
        _miniPluginPermission = miniPluginPermission;
        _miniPluginDatabase = miniPluginDatabase;
        _miniPluginPortal = miniPluginPortal;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command.")));
            return;
        }

        if (!_miniPlugin._receivedTipSet.contains(player)) {
            _miniPlugin._receivedTipSet.add(player);
            player.sendMessage(F.fMain(this,
                    "You should receive a reply shortly if a staff member is available. You can also report rule-breakers with ",
                    F.fItem("/report"), "."));
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            try {
                _miniPluginDatabase.getUnifiedJedis().publish(SupportMessage.CHANNEL_NAME,
                        new SupportMessage(player.getUniqueId(), String.join(" ", args),
                                _miniPluginPortal._serverName).toString());
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("An error occurred while sending your support message. Maybe try again later?")));

                _miniPlugin.logWarning(
                        "JedisException while player '" + sender.getName() + "' sending support message: " +
                                ex.getMessage());
            }
        });
    }

}
