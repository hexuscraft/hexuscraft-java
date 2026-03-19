package net.hexuscraft.core.party;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.party.command.CommandParty;

import java.util.Map;

public final class CoreParty extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PARTY
    }

    CoreCommand _pluginCommand;

    public CoreParty(final HexusPlugin plugin) {
        super(plugin,
                "Party");

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_PARTY);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandParty(this));
    }
}
