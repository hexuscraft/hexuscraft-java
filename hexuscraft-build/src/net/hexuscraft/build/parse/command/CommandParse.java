package net.hexuscraft.build.parse.command;

import net.hexuscraft.build.parse.BuildParse;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandParse extends BaseCommand<BuildParse>
{

    @SuppressWarnings("FieldCanBeLocal")
    final Integer MINIMUM_RADIUS = 1;

    public CommandParse(BuildParse parse)
    {
        super(parse,
                "parse",
                "<Radius>",
                "Parse your current world. The parse centers around chunk (0,0). The radius is the amount of chunks " +
                        "in each direction parsed.",
                Set.of(),
                BuildParse.PERM.COMMAND_PARSE);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        try
        {
            runLogic(sender, args);
        }
        catch (ExInvalidSender ex)
        {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command.")));
        }
        catch (ExInvalidRadius ex)
        {
            sender.sendMessage(F.fMain(this, F.fError("Invalid radius.")));
        }
        catch (ExRadiusTooSmall ex)
        {
            sender.sendMessage(F.fMain(this,
                    F.fError("Radius too small."),
                    "Minimum radius: ",
                    F.fItem(Integer.toString(ex._minimumRadius))));
        }
        catch (ExInvalidWorld ex)
        {
            sender.sendMessage(F.fMain(this, F.fError("You cannot parse the default world.")));
        }
    }

    void runLogic(CommandSender sender, String[] args)
            throws ExInvalidRadius, ExInvalidSender, ExInvalidWorld, ExRadiusTooSmall
    {
        if (!(sender instanceof Player player))
        {
            throw new ExInvalidSender();
        }

        int radius;
        try
        {
            radius = Integer.parseInt(args[0]);
        }
        catch (Exception ex)
        {
            throw new ExInvalidRadius();
        }

        if (radius < MINIMUM_RADIUS)
        {
            throw new ExRadiusTooSmall(MINIMUM_RADIUS);
        }

        World world = player.getWorld();
        if (world.getName().equals("world"))
        {
            throw new ExInvalidWorld();
        }

        _miniPlugin.parse(world, radius);
    }

    static class ExInvalidSender extends Exception
    {
    }

    static class ExInvalidRadius extends Exception
    {
    }

    static class ExInvalidWorld extends Exception
    {
    }

    static class ExRadiusTooSmall extends Exception
    {
        public Integer _minimumRadius;

        public ExRadiusTooSmall(Integer minimumRadius)
        {
            _minimumRadius = minimumRadius;
        }
    }

}
