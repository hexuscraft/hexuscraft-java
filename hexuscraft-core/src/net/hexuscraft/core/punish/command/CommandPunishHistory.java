package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.PluginPunish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class CommandPunishHistory extends BaseCommand {

    public CommandPunishHistory(PluginPunish pluginPunish) {
        super(pluginPunish, "punishmenthistory", "[Player]", "View the history of punishments.", Set.of("punishhistory", "xh"), PluginPunish.PERM.COMMAND_PUNISH_HISTORY);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final UUID targetUUID;
        final String targetName;

        if (args.length == 1) {
            MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
            if (profile == null) {
                return;
            }
            targetUUID = profile.uuid;
            targetName = profile.name;
        } else if (sender instanceof Player player) {
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            sender.sendMessage(F.fMain(this) + "Only players can view their own punishment history.");
            return;
        }

        sender.sendMessage(F.fMain(this) + "Viewing punishment history of " + F.fItem(targetName) + ".");

    }
}
