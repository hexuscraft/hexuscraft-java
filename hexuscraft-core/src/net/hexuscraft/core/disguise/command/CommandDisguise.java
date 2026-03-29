package net.hexuscraft.core.disguise.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.disguise.CoreDisguise;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandDisguise extends BaseCommand<CoreDisguise>
{

    public CommandDisguise(CoreDisguise coreDisguise)
    {
        super(coreDisguise,
                "disguise",
                "[Target] [Username]",
                "Disguise yourself, or force another player to disguise, as another player.",
                Set.of("nick"),
                CoreDisguise.PERM.COMMAND_DISGUISE);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 0 || args.length > 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        Player targetPlayer;
        String disguiseUsername;

        if (args.length == 1)
        {
            if (!(sender instanceof Player player))
            {
                sender.sendMessage(F.fMain(this, F.fError("Only players can disguise themself.")));
                return;
            }

            targetPlayer = player;
            disguiseUsername = args[0];
        }
        else
        {
            Player[] potentialMatches = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer()
                    .getOnlinePlayers(), args[0]);
            if (potentialMatches.length != 1)
            {
                sender.sendMessage(F.fMain(this,
                        F.fMatches(Arrays.stream(potentialMatches).map(Player::getName).toArray(String[]::new),
                                args[0])));
                return;
            }

            targetPlayer = potentialMatches[0];
            disguiseUsername = args[1];
        }

        OfflinePlayer potentialDisguise = PlayerSearch.offlinePlayerSearch(disguiseUsername, sender);
        if (potentialDisguise == null)
        {
            sender.sendMessage(F.fMain(this, F.fError("The specified disguise username does not exist.")));
            return;
        }

        super._miniPlugin.disguise(targetPlayer, potentialDisguise);

        targetPlayer.sendMessage(F.fMain(this, "You are now disguised as ", F.fItem(potentialDisguise.getName()), "."));
        if (targetPlayer.equals(sender))
        {
            return;
        }
        sender.sendMessage(F.fMain(this,
                "Disguised ",
                F.fItem(targetPlayer.getName()),
                " as ",
                F.fItem(potentialDisguise.getName()),
                "."));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        return List.of(sender.getName());
    }

}
