package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.MiniPluginPermission;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Set;

public final class CommandRankList extends BaseCommand<MiniPluginPermission> {

    CommandRankList(MiniPluginPermission miniPluginPermission) {
        super(miniPluginPermission, "list", "", "List all permission groups.", Set.of("l"),
                MiniPluginPermission.PERM.COMMAND_RANK_LIST);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Listing Groups:\n"
                + F.fMain("") +
                F.fItem(Arrays.stream(PermissionGroup.values()).map(group -> group._color + group.name())
                        .toArray(String[]::new)));
    }


}
