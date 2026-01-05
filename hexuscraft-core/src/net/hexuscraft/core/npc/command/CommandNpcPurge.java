package net.hexuscraft.core.npc.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.npc.MiniPluginNpc;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandNpcPurge extends BaseCommand<MiniPluginNpc> {

    CommandNpcPurge(final MiniPluginNpc miniPluginNpc) {
        super(miniPluginNpc, "purge", "", "Temporarily purge all NPCs.", Set.of("p", "kill", "k", "destroy", "d"),
                MiniPluginNpc.PERM.COMMAND_ENTITY_PURGE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can purge NPCs in their current world.")));
            return;
        }

        _miniPlugin.removeNPCs(player.getWorld());
        sender.sendMessage(F.fMain(this, "Purged all NPCs in your world."));
    }

}
