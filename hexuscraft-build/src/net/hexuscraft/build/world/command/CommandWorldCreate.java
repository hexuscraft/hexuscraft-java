package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public final class CommandWorldCreate extends BaseCommand<MiniPluginWorld> {

    public CommandWorldCreate(final MiniPluginWorld miniPluginWorld) {
        super(miniPluginWorld, "create", "<Name>", "Create a new void world.", Set.of("c"),
                MiniPluginWorld.PERM.COMMAND_WORLD_CREATE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final WorldCreator worldCreator = new WorldCreator(args[0]);
        worldCreator.type(WorldType.FLAT);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generateStructures(false);
        worldCreator.generatorSettings("3;0");
        worldCreator.generator("3;0");

        final World world = _miniPlugin._hexusPlugin.getServer().createWorld(worldCreator);
        world.setSpawnLocation(0, 100, 0);
        world.setDifficulty(Difficulty.NORMAL);
        world.setAutoSave(false);
        world.setAmbientSpawnLimit(0);
        world.setAnimalSpawnLimit(0);
        world.setMonsterSpawnLimit(0);
        world.setPVP(false);
        world.setWaterAnimalSpawnLimit(0);
        Map.of(
                "doDaylightCycle", "false",
                "doEntityDrops", "false",
                "doFireTick", "false",
                "mobGriefing", "false"
        ).forEach(world::setGameRuleValue);
        world.save();

        sender.sendMessage(F.fMain(this, "Created world ", F.fItem(world.getName()), "."));

        if (!(sender instanceof Player player)) return;
        player.teleport(world.getSpawnLocation());
    }

}
