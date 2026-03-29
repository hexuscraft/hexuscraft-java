package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandServer extends BaseCommand<CorePortal>
{

    private final CoreDatabase _coreDatabase;

    public CommandServer(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal,
                "server",
                "[Name]",
                "View your current server or teleport to a server.",
                Set.of("sv", "portal"),
                CorePortal.PERM.COMMAND_SERVER);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 1)
        {
            if (!(sender instanceof Player player))
            {
                sender.sendMessage(F.fMain(this, "Only players can teleport to a server."));
                return;
            }
            String serverName = args[0];

            ServerData serverData = _miniPlugin.getServer(serverName);
            if (serverData == null)
            {
                sender.sendMessage(F.fMain(this,
                        F.fError("Could not locate server with name ", F.fItem(serverName), ".")));
                return;
            }

            ServerGroupData serverGroupData = _miniPlugin.getServerGroup(serverData._group);
            if (serverGroupData == null)
            {
                sender.sendMessage(F.fMain(this,
                        F.fError("Could not locate server group with name ", F.fItem(serverData._group), ".")));
                return;
            }
            if (!sender.hasPermission(serverGroupData._requiredPermission.name()))
            {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }


            _miniPlugin.teleport(player, serverData._name);
            return;
        }
        if (args.length == 0)
        {
            sender.sendMessage(F.fMain(this, "You are connected to ", F.fItem(_miniPlugin._serverName), "."));
            return;
        }
        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return Arrays.stream(_miniPlugin.getServers()).map(serverData -> serverData._name).toList();
        }
        return List.of();
    }
}
