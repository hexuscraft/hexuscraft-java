package net.hexuscraft.core.buildversion.command;

import net.hexuscraft.core.buildversion.PluginBuildVersion;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Map;
import java.util.Set;

public class CommandBuildVersion extends BaseCommand {

    public CommandBuildVersion(final PluginBuildVersion plugin) {
        super(plugin, "buildversion", "", "View information about the current plugin.", Set.of("bv"), PluginBuildVersion.PERM.COMMAND_BUILDVERSION);
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final PluginBuildVersion plugin = (PluginBuildVersion) _miniPlugin;
        final PluginDescriptionFile description = plugin._plugin.getDescription();
        final long lastModified = plugin.getLastModifiedMillis();

        final StringBuilder builder = new StringBuilder(F.fMain(this, "Plugin Information:\n"));
        Map.of(
                "Name", description.getName(),
                "Main", description.getMain(),
                "Version", description.getVersion(),
                "Load", description.getLoad().name(),
                "Last Modified", F.fTime(System.currentTimeMillis() - lastModified) + " (" + lastModified + ")",
                "File Size", plugin.getSizeBytes() + " (B)"
        ).forEach((key, value) -> builder.append(F.fMain("", key, ": ", F.fItem(value), "\n")));
        sender.sendMessage(builder.toString());
    }

}
