package net.hexuscraft.core.npc.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.npc.MiniPluginNpc;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNpcPurge extends BaseCommand<MiniPluginNpc> {

    CommandNpcPurge(final MiniPluginNpc miniPluginNpc) {
        super(miniPluginNpc, "purge", "", "Temporarily purge all NPCs.", Set.of("kill", "p"),
                MiniPluginNpc.PERM.COMMAND_ENTITY_PURGE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        int amount = _miniPlugin.purge();

        sender.sendMessage(F.fMain(this) + "Temporarily purged " + F.fItem(amount + " NPCs") + ".");
    }

}
