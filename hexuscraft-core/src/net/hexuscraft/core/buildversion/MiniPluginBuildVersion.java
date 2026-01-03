package net.hexuscraft.core.buildversion;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.buildversion.command.CommandBuildVersion;
import net.hexuscraft.core.command.MiniPluginCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public final class MiniPluginBuildVersion extends MiniPlugin<HexusPlugin> {

    private MiniPluginCommand _pluginCommand;

    public MiniPluginBuildVersion(final HexusPlugin plugin) {
        super(plugin, "Build Version");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandBuildVersion(this));
    }

    public long getLastModifiedMillis() {
        return _hexusPlugin.getFile().lastModified();
    }

    public long getSizeBytes() {
        try {
            return Files.size(_hexusPlugin.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum PERM implements IPermission {
        COMMAND_BUILDVERSION
    }

}
