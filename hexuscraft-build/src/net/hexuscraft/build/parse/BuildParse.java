package net.hexuscraft.build.parse;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.parse.command.CommandParse;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import org.bukkit.World;

import java.util.Map;

public class BuildParse extends MiniPlugin<Build>
{

    public enum PERM implements IPermission
    {
        COMMAND_PARSE
    }

    CoreCommand _command;

    public BuildParse(Build build)
    {
        super(build, "Parse");

        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_PARSE);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _command = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable()
    {
        _command.register(new CommandParse(this));
    }

    public void parse(World world, int radius)
    {
    }

}
