package net.hexuscraft.core.party;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.party.command.CommandParty;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public final class MiniPluginParty extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PARTY
    }

    MiniPluginCommand _pluginCommand;

    public MiniPluginParty(final HexusPlugin plugin) {
        super(plugin, "Party");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PARTY);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandParty(this));
    }
}
