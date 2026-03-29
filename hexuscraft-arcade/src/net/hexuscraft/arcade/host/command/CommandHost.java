package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public class CommandHost extends BaseMultiCommand<ArcadeHost>
{
    public CommandHost(ArcadeHost arcadeHost)
    {
        super(arcadeHost,
                "host",
                "View or modify the server host.",
                Set.of("serverhost", "sh"),
                ArcadeHost.PERM.COMMAND_HOST,
                Set.of(new CommandHostSet(arcadeHost), new CommandHostView(arcadeHost)));
    }
}
