package net.hexuscraft.core.disguise.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.disguise.MiniPluginDisguise;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandDisguise extends BaseCommand<MiniPluginDisguise> {

    public CommandDisguise(final MiniPluginDisguise miniPluginDisguise) {
        super(miniPluginDisguise, "disguise", "[Target] [Username]",
                "Disguise yourself, or force another player to disguise, as another player.", Set.of("nick"),
                MiniPluginDisguise.PERM.COMMAND_DISGUISE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player targetPlayer;
        final String disguiseUsername;

        if (args.length == 1) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can disguise themself.")));
                return;
            }

            targetPlayer = player;
            disguiseUsername = args[0];
        } else {
            final Player[] potentialMatches =
                    PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0]);
            if (potentialMatches.length != 1) {
                sender.sendMessage(F.fMain(this,
                        F.fMatches(Arrays.stream(potentialMatches).map(Player::getName).toArray(String[]::new),
                                args[0])));
                return;
            }

            targetPlayer = potentialMatches[0];
            disguiseUsername = args[1];
        }

        final OfflinePlayer potentialDisguise = PlayerSearch.offlinePlayerSearch(disguiseUsername, sender);
        if (potentialDisguise == null) {
            sender.sendMessage(F.fMain(this, F.fError("The specified disguise username does not exist.")));
            return;
        }

        super._miniPlugin.disguise(targetPlayer, potentialDisguise);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return List.of(sender.getName());
    }

}
