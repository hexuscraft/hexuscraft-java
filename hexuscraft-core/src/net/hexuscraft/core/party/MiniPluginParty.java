package net.hexuscraft.core.party;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.party.command.CommandParty;

import java.util.Map;

public final class MiniPluginParty extends MiniPlugin<HexusPlugin> {

    MiniPluginCommand _pluginCommand;

    public MiniPluginParty(final HexusPlugin plugin) {
        super(plugin, "Party");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PARTY);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandParty(this));
    }

    public enum PERM implements IPermission {
        COMMAND_PARTY
    }
}
