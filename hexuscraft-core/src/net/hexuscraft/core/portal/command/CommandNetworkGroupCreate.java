package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

// TODO: Make this into a "builder" style command: Open a GUI where you can click items to modify properties before
//  creating.

public class CommandNetworkGroupCreate extends BaseCommand<CorePortal> {

    final String[] DISALLOWED_CHARACTERS = new String[]{":", "//", "\\\\", ".."};

    final CoreDatabase _coreDatabase;

    CommandNetworkGroupCreate(CorePortal corePortal, CoreDatabase coreDatabase) {
        super(corePortal,
                "create",
                "",
                "Create a server group.",
                Set.of("c", "add", "a"),
                CorePortal.PERM.COMMAND_NETWORK_GROUP_CREATE);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can open the server group creation menu.")));
            return;
        }

        _miniPlugin.openServerGroupCreateGui(player);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length == 2) {
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        }
        return List.of();
    }

}
