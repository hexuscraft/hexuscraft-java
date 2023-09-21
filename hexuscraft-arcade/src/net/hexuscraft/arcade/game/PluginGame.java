package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginGame extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_GAME
    }

    protected PluginGame(Arcade arcade) {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
    }

}
