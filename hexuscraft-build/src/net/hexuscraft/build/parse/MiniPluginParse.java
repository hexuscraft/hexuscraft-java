package net.hexuscraft.build.parse;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.parse.command.CommandParse;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.World;

import java.util.Map;

public final class MiniPluginParse extends MiniPlugin<Build> {

    public enum PERM implements IPermission {
        COMMAND_PARSE
    }

    private MiniPluginCommand _command;

    public MiniPluginParse(final Build build) {
        super(build, "Parse");

        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_PARSE);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _command = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _command.register(new CommandParse(this));
    }

    @SuppressWarnings("unused")
    public void parse(final World world, final int radius) {
    }

}
