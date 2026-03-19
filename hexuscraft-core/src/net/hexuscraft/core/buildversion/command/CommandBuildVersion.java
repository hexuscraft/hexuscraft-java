package net.hexuscraft.core.buildversion.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.buildversion.CoreBuildVersion;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Map;
import java.util.Set;

public final class CommandBuildVersion extends BaseCommand<CoreBuildVersion> {

    public CommandBuildVersion(final CoreBuildVersion plugin) {
        super(plugin,
                "buildversion",
                "",
                "View information about the current plugin.",
                Set.of("bv"),
                CoreBuildVersion.PERM.COMMAND_BUILDVERSION);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final CoreBuildVersion plugin = _miniPlugin;
        final PluginDescriptionFile description = plugin._hexusPlugin.getDescription();
        final long lastModified = plugin.getLastModifiedMillis();

        final StringBuilder builder = new StringBuilder(F.fMain(this,
                "Plugin Information:\n"));
        Map.of(
                        "Name",
                        description.getName(),
                        "Main",
                        description.getMain(),
                        "Version",
                        description.getVersion(),
                        "Load",
                        description.getLoad()
                                .name(),
                        "Last Modified",
                        F.fTime(System.currentTimeMillis() - lastModified) + " (" + lastModified + ")",
                        "File Size",
                        plugin.getSizeBytes() + " (B)"
                )
                .forEach((key, value) -> builder.append(F.fMain("",
                        key,
                        ": ",
                        F.fItem(value),
                        "\n")));
        sender.sendMessage(builder.toString());
    }

}
