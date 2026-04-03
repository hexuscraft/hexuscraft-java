package net.hexuscraft.core.buildversion;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.buildversion.command.CommandBuildVersion;
import net.hexuscraft.core.command.CoreCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class CoreBuildVersion extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_BUILDVERSION
    }

    CoreCommand _pluginCommand;

    public CoreBuildVersion(HexusPlugin plugin)
    {
        super(plugin, "Build Version");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable()
    {
        _pluginCommand.register(new CommandBuildVersion(this));
    }

    public long getLastModifiedMillis()
    {
        return _hexusPlugin.getFile().lastModified();
    }

    public long getSizeBytes()
    {
        try
        {
            return Files.size(_hexusPlugin.getFile().toPath());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
