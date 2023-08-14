package net.hexuscraft.core.buildversion;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.buildversion.command.CommandBuildVersion;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class PluginBuildVersion extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_BUILDVERSION
    }

    private PluginCommand _pluginCommand;

    public PluginBuildVersion(JavaPlugin plugin) {
        super(plugin, "Build Version");
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public final void onEnable() {
        _pluginCommand.register(new CommandBuildVersion(this));
    }

    public final long getLastModifiedMillis() {
        return ((HexusPlugin) _javaPlugin).getFile().lastModified();
    }

    public final long getSizeBytes() {
        try {
            return Files.size(((HexusPlugin) _javaPlugin).getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
