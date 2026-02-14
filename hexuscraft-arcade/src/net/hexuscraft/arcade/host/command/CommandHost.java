package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.MiniPluginHost;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public class CommandHost extends BaseMultiCommand<MiniPluginHost> {
    public CommandHost(final MiniPluginHost miniPluginHost) {
        super(miniPluginHost,
                "host",
                "View or modify the server host.",
                Set.of("serverhost",
                        "sh"),
                MiniPluginHost.PERM.COMMAND_HOST,
                Set.of(
                        new CommandHostSet(miniPluginHost),
                        new CommandHostView(miniPluginHost)
                ));
    }
}
