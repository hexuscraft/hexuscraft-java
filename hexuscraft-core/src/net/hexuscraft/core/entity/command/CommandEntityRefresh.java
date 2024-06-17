package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.MiniPluginEntity;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandEntityRefresh extends BaseCommand<MiniPluginEntity> {

    CommandEntityRefresh(final MiniPluginEntity miniPluginEntity) {
        super(miniPluginEntity, "refresh", "", "Refresh all NPCs.", Set.of("ref", "r"), MiniPluginEntity.PERM.COMMAND_ENTITY_REFRESH);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        long start = System.currentTimeMillis();

        sender.sendMessage(F.fMain(this) + "Processing NPC refresh...");
        _miniPlugin._hexusPlugin.getServer().getWorlds().forEach(world -> {
            sender.sendMessage(F.fMain(this) + "Refreshing NPCs in world " + F.fItem(world.getName()) + ".");
            _miniPlugin.refreshNPCs(world);
        });

        sender.sendMessage(F.fMain(this) + "Completed NPC refresh in " + F.fItem(F.fTime(System.currentTimeMillis() - start)) + ".");
    }

}
