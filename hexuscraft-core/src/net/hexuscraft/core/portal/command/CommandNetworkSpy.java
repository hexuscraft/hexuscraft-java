package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkSpy extends BaseCommand<CorePortal> {

    CommandNetworkSpy(final CorePortal corePortal) {
        super(corePortal,
                "spy",
                "",
                "Receive event logs from ServerMonitor.",
                Set.of(),
                CorePortal.PERM.COMMAND_NETWORK_SPY);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (_miniPlugin._networkChannelSpies.contains(sender)) {
            _miniPlugin._networkChannelSpies.remove(sender);
            sender.sendMessage(F.fMain(this,
                    F.fError("You are no longer spying network channels.")));
            return;
        }

        _miniPlugin._networkChannelSpies.add(sender);
        sender.sendMessage(F.fMain(this,
                F.fSuccess("You are now spying network channels.")));
    }

}
