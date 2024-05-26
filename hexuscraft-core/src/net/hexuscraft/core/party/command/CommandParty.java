package net.hexuscraft.core.party.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.party.PluginParty;

import java.util.Set;

public class CommandParty extends BaseMultiCommand<HexusPlugin> {

    public CommandParty(PluginParty pluginParty) {
//        super(pluginParty, "party", "Manage or join a party.", Set.of("p", "z"), PluginParty.PERM.COMMAND_PARTY, Set.of());
        super(pluginParty, "party", "Parties are currently work in progress - Check back soon!", Set.of("p", "z"), PluginParty.PERM.COMMAND_PARTY, Set.of());
    }

}
