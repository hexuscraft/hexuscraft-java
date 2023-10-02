package net.hexuscraft.build.parse;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.parse.command.CommandParse;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.World;

import java.util.Map;

public class PluginParse extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_PARSE
    }

    private PluginCommand _command;

    public PluginParse(final Build build) {
        super(build, "Parse");

        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_PARSE);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _command = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    public final void parse(final World world, final int radius) {
        _command.register(new CommandParse(this));
    }

}
