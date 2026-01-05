package net.hexuscraft.core.npc.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.npc.MiniPluginNpc;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNpcRefresh extends BaseCommand<MiniPluginNpc> {

    CommandNpcRefresh(final MiniPluginNpc miniPluginNpc) {
        super(miniPluginNpc, "refresh", "", "Refresh all NPCs.", Set.of("ref", "r"),
                MiniPluginNpc.PERM.COMMAND_ENTITY_REFRESH);
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

        sender.sendMessage(
                F.fMain(this) + "Completed NPC refresh in " + F.fItem(F.fTime(System.currentTimeMillis() - start)) +
                        ".");
    }

}
