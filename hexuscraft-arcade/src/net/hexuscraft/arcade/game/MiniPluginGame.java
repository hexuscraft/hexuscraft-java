package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.command.CommandGame;
import net.hexuscraft.arcade.game.command.CommandHub;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Map;

public final class MiniPluginGame extends MiniPlugin<Arcade> {

    public enum PERM implements IPermission {
        COMMAND_GAME, COMMAND_GAME_SET, COMMAND_GAME_START, COMMAND_GAME_STOP, COMMAND_HUB
    }

    public GameState _gameState = GameState.LOADING_MAP;

    private MiniPluginCommand _miniPluginCommand;
    private MiniPluginPortal _miniPluginPortal;

    public MiniPluginGame(final Arcade arcade) {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME_SET);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_HUB);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandGame(this));
        _miniPluginCommand.register(new CommandHub(this, _miniPluginPortal));
    }

}
