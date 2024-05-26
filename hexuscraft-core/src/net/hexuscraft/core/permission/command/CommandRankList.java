package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Set;

public class CommandRankList extends BaseCommand<HexusPlugin> {

    CommandRankList(PluginPermission pluginPermission) {
        super(pluginPermission, "list", "", "List all permission groups.", Set.of("l"), PluginPermission.PERM.COMMAND_RANK_LIST);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Listing Groups:\n"
                + F.fMain("") + F.fList(Arrays.stream(PermissionGroup.values()).map(group -> group._color + group.name()).toArray(String[]::new)));
    }


}
