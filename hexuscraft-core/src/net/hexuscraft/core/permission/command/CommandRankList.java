package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

public class CommandRankList extends BaseCommand {

    CommandRankList(PluginPermission pluginPermission) {
        super(pluginPermission, "list", "", "List all permission groups.", Set.of("l"), PluginPermission.PERM.COMMAND_RANK_LIST);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(help(alias));
            return;
        }

        Set<String> metaGroups = new HashSet<>();
        Set<String> playerGroups = new HashSet<>();

        for (PermissionGroup group : PermissionGroup.values()) {
            if (group.name().startsWith("_")) {
                metaGroups.add(group._color + group.name());
                continue;
            }
            playerGroups.add(group._color + group.name());
        }

        sender.sendMessage(F.fMain(this) + "Listing Meta Groups:");
        sender.sendMessage(F.fMain() + F.fList(metaGroups.toArray(new String[0])));
        sender.sendMessage(F.fMain(this) + "Listing Player Groups:");
        sender.sendMessage(F.fMain() + F.fList(playerGroups.toArray(new String[0])));
    }


}
