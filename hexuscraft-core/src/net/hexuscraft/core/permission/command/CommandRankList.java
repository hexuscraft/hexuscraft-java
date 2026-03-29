package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.CorePermission;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Set;

public class CommandRankList extends BaseCommand<CorePermission>
{

    CommandRankList(CorePermission corePermission)
    {
        super(corePermission,
                "list",
                "",
                "List all permission groups.",
                Set.of("l"),
                CorePermission.PERM.COMMAND_RANK_LIST);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length != 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) +
                "Listing Groups:\n" +
                F.fMain("") +
                F.fItem(Arrays.stream(PermissionGroup.values())
                        .map(group -> group._color + group.name())
                        .toArray(String[]::new)));
    }


}
