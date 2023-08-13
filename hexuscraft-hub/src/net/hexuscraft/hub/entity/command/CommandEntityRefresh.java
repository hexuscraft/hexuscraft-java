package net.hexuscraft.hub.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.hub.entity.PluginEntity;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class CommandEntityRefresh extends BaseCommand {

    PluginEntity pluginEntity;

    CommandEntityRefresh(PluginEntity pluginEntity) {
        super(pluginEntity, "refresh", "", "Refresh all NPCs.", Set.of("ref", "r"), PluginEntity.PERM.COMMAND_ENTITY_REFRESH);
        this.pluginEntity = pluginEntity;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        long start = System.currentTimeMillis();

        sender.sendMessage(F.fMain(this) + "Processing NPC refresh...");
        _miniPlugin._javaPlugin.getServer().getWorlds().forEach(world -> {
            sender.sendMessage(F.fMain(this) + "Refreshing NPCs in world " + F.fItem(world.getName()) + ".");
            pluginEntity.refreshNPCs(world);
        });

        sender.sendMessage(F.fMain(this) + "Completed NPC refresh in " + F.fElem(F.fTime(System.currentTimeMillis() - start)) + ".");
    }

}
