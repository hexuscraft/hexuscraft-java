package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.MiniPluginHost;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandHostView extends BaseCommand<MiniPluginHost> {
    public CommandHostView(final MiniPluginHost miniPluginHost) {
        super(miniPluginHost, "view", "", "View the current server host.", Set.of(),
                MiniPluginHost.PERM.COMMAND_HOST_SET);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (_miniPlugin._hostOfflinePlayer.get() == null) {
            sender.sendMessage(F.fMain(this, "There is currently no server host."));
            return;
        }

        final OfflinePlayer oldHost = _miniPlugin._hostOfflinePlayer.get();
        if (oldHost == null) {
            sender.sendMessage(F.fMain(this, "There is currently no server host."));
            return;
        }

        sender.sendMessage(F.fMain(this, "The current server host is ", F.fItem(oldHost.getName())));
    }
}
