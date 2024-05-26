package net.hexuscraft.build.parse.command;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.parse.PluginParse;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandParse extends BaseCommand<Build> {

    private final Integer MINIMUM_RADIUS = 1;

    private static final class ExInvalidSender extends Exception {
    }

    private static final class ExInvalidRadius extends Exception {
    }

    private static final class ExInvalidWorld extends Exception {
    }

    private static final class ExRadiusTooSmall extends Exception {
        public final Integer _minimumRadius;

        public ExRadiusTooSmall(final Integer minimumRadius) {
            _minimumRadius = minimumRadius;
        }
    }

    private final PluginParse _parse;

    public CommandParse(final PluginParse parse) {
        super(parse, "parse", "<Radius>",
                "Parse your current world. The parse centers around chunk (0,0). The radius is the amount of chunks in each direction parsed.",
                Set.of(), PluginParse.PERM.COMMAND_PARSE);

        _parse = parse;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        try {
            runLogic(sender, alias, args);
        } catch (final ExInvalidSender ex) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command.")));
        } catch (final ExInvalidRadius ex) {
            sender.sendMessage(F.fMain(this, F.fError("Invalid radius.")));
        } catch (final ExInvalidWorld ex) {
            sender.sendMessage(F.fMain(this, F.fError("You cannot parse the default world.")));
        } catch (final ExRadiusTooSmall ex) {
            sender.sendMessage(F.fMain(this, F.fError("Radius too small."), "Minimum radius: ",
                    F.fItem(Integer.toString(ex._minimumRadius))));
        }
    }

    private void runLogic(final CommandSender sender, final String alias, final String[] args)
            throws ExInvalidRadius, ExInvalidSender, ExInvalidWorld, ExRadiusTooSmall {
        if (!(sender instanceof Player player)) {
            throw new ExInvalidSender();
        }

        final int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            throw new ExInvalidRadius();
        }

        if (radius < MINIMUM_RADIUS) {
            throw new ExRadiusTooSmall(MINIMUM_RADIUS);
        }

        final World world = player.getWorld();
        if (world.getName().equals("world")) {
            throw new ExInvalidWorld();
        }

        _parse.parse(world, radius);
    }

}
