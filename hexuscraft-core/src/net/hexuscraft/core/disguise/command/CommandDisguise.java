package net.hexuscraft.core.disguise.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.disguise.PluginDisguise;
import net.hexuscraft.core.permission.PermissionGroup;
import net.minecraft.server.v1_8_R3.BlockActionData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class CommandDisguise extends BaseCommand {

    public CommandDisguise(PluginDisguise pluginDisguise) {
        super(pluginDisguise, "disguise", "<Name>", "Appear as another player.", Set.of("nick", "impersonate"), PluginDisguise.PERM.COMMAND_DISGUISE);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(F.fMain(this) + "Only players can use this command.");
            return;
        }

        try {
            boolean disguised = ((PluginDisguise) _miniPlugin).disguise(player, EntityType.PLAYER, args[0]);
            if (!disguised) { return; }
            player.sendMessage(F.fMain(this) + "You are now disguised as " + F.fEntity(player.getName()) + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage(F.fMain(this) + "There was an error while applying your disguise: " + ex.getMessage());
        }
    }

}
