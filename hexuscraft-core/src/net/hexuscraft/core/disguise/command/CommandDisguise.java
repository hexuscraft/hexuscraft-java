package net.hexuscraft.core.disguise.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.disguise.PluginDisguise;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
            if (!disguised) {
                return;
            }
            player.sendMessage(F.fMain(this) + "You are now disguised as " + F.fItem(player.getName()) + ".");
        } catch (Exception ex) {
            // Not very important if disguises fail, just print the error to console and let the player know
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
            player.sendMessage(F.fMain(this) + F.fError("There was an error while applying your disguise:\n") + F.fMain() + ex.getMessage());
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            return List.of();
        }

        List<String> names = new ArrayList<>();

        //noinspection ReassignedVariable
        Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream();
        if (sender instanceof Player player) {
            streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
        }

        names.addAll(List.of("*", "**"));
        names.addAll(streamedOnlinePlayers.map(Player::getName).toList());

        return names;
    }

}
