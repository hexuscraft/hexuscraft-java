package net.hexuscraft.build.parse.command;

import net.hexuscraft.build.parse.MiniPluginParse;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandParse extends BaseCommand<MiniPluginParse> {

    @SuppressWarnings("FieldCanBeLocal")
    private final Integer MINIMUM_RADIUS = 1;

    public CommandParse(final MiniPluginParse parse) {
        super(parse, "parse", "<Radius>",
                "Parse your current world. The parse centers around chunk (0,0). The radius is the amount of chunks in each direction parsed.",
                Set.of(), MiniPluginParse.PERM.COMMAND_PARSE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        try {
            runLogic(sender, args);
        } catch (final ExInvalidSender ex) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command.")));
        } catch (final ExInvalidRadius ex) {
            sender.sendMessage(F.fMain(this, F.fError("Invalid radius.")));
        } catch (final ExRadiusTooSmall ex) {
            sender.sendMessage(F.fMain(this, F.fError("Radius too small."), "Minimum radius: ",
                    F.fItem(Integer.toString(ex._minimumRadius))));
        } catch (final ExInvalidWorld ex) {
            sender.sendMessage(F.fMain(this, F.fError("You cannot parse the default world.")));
        }
    }

    private void runLogic(final CommandSender sender, final String[] args)
            throws ExInvalidRadius, ExInvalidSender, ExInvalidWorld, ExRadiusTooSmall {
        if (!(sender instanceof final Player player)) {
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

        _miniPlugin.parse(world, radius);
    }

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

}
