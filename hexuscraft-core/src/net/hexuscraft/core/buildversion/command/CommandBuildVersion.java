package net.hexuscraft.core.buildversion.command;

import net.hexuscraft.core.buildversion.PluginBuildVersion;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandBuildVersion extends BaseCommand {

    public CommandBuildVersion(PluginBuildVersion plugin) {
        super(plugin, "buildversion", "", "", Set.of(), PluginBuildVersion.PERM.COMMAND_BUILDVERSION);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        PluginBuildVersion plugin = (PluginBuildVersion) _miniPlugin;
        long lastModified = plugin.getLastModifiedMillis();
        long fileSize = plugin.getSizeBytes();
        sender.sendMessage(F.fMain(this) + "Build Version:\n"
                + F.fMain() + "Name: " + F.fItem(plugin._javaPlugin.getDescription().getName())
                + F.fMain() + "Main: " + F.fItem(plugin._javaPlugin.getDescription().getMain())
                + F.fMain() + "Version: " + F.fItem(plugin._javaPlugin.getDescription().getVersion())
                + F.fMain() + "Load: " + F.fItem(plugin._javaPlugin.getDescription().getLoad().name())
                + F.fMain() + "Last Modified: " + F.fTime(System.currentTimeMillis() - lastModified) + " (" + lastModified + ")"
                + F.fMain() + "File Size: " + F.fItem(fileSize + "B")
        );
    }

}
