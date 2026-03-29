package net.hexuscraft.core.party.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.party.CoreParty;

import java.util.Set;

public class CommandParty extends BaseMultiCommand<CoreParty>
{

    public CommandParty(CoreParty pluginParty)
    {
        //        super(pluginParty, "party", "Manage or join a party.", Set.of("p", "z"), MiniPluginParty.PERM.COMMAND_PARTY, Set.of());
        super(pluginParty,
              "party",
              "Parties are currently work in progress - Check back soon!",
              Set.of("p", "z"),
              CoreParty.PERM.COMMAND_PARTY,
              Set.of());
    }

}
