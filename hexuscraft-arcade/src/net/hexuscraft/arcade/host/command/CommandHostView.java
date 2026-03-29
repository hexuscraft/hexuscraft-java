package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandHostView extends BaseCommand<ArcadeHost>
{
    public CommandHostView(ArcadeHost arcadeHost)
    {
        super(arcadeHost, "view", "", "View the current server host.", Set.of("v"), ArcadeHost.PERM.COMMAND_HOST_VIEW);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        OfflinePlayer host = _miniPlugin._host.get();
        if (host == null)
        {
            sender.sendMessage(F.fMain(this, "There is currently no server host."));
            return;
        }

        sender.sendMessage(F.fMain(this,
                "The current server host is ",
                F.fItem(host.isOnline() ? host.getPlayer().getDisplayName() : host.getName())));
    }
}
