package net.hexuscraft.core.party;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.party.command.PartyCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public class PluginParty extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PARTY
    }

    PluginCommand _pluginCommand;

    public PluginParty(final HexusPlugin plugin) {
        super(plugin, "Party");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PARTY);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new PartyCommand(this));
    }
}
