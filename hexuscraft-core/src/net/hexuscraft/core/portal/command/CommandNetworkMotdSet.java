package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandNetworkMotdSet extends BaseCommand<CorePortal>
{

    final CoreDatabase _coreDatabase;

    CommandNetworkMotdSet(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal, "set", "<Message>", "Set the current MOTD.", Set.of("s"), CorePortal.PERM.COMMAND_MOTD_SET);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        sender.sendMessage(F.fMain(this, "Please wait... Updating the MOTD to:\n", C.fReset + message));

        _miniPlugin._hexusPlugin.runAsync(() ->
        {
            ServerQueries.setMotd(_coreDatabase._database._jedis, message);
            _miniPlugin._hexusPlugin.runSync(() -> sender.sendMessage(F.fMain(this,
                    F.fSuccess("Successfully updated the MOTD:\n"),
                    C.fReset + message) + "\n" + F.fMain("", "It may take a few seconds for all proxies to update.")));
        });

    }

}
