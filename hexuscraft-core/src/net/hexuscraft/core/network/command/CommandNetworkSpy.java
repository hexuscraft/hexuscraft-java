package net.hexuscraft.core.network.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.network.MiniPluginNetwork;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandNetworkSpy extends BaseCommand<MiniPluginNetwork> {

    CommandNetworkSpy(final MiniPluginNetwork miniPluginNetwork) {
        super(miniPluginNetwork, "spy", "", "Receive event logs from ServerMonitor.", Set.of(), MiniPluginNetwork.PERM.COMMAND_NETWORK_SPY);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (_miniPlugin._spyingPlayers.contains(sender)) {
            _miniPlugin._spyingPlayers.remove(sender);
            sender.sendMessage(F.fMain(this, F.fError("You are no longer spying the network.")));
            return;
        }

        _miniPlugin._spyingPlayers.add(sender);
        sender.sendMessage(F.fMain(this, F.fSuccess("You are now spying the network.")));
    }

}
