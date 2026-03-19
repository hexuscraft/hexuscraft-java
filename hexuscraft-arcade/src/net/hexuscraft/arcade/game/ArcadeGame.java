package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.command.CommandGame;
import net.hexuscraft.arcade.game.command.CommandHub;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Map;

public final class ArcadeGame extends MiniPlugin<Arcade> {

    public enum PERM implements IPermission {
        COMMAND_GAME,
        COMMAND_GAME_SET,
        COMMAND_GAME_START,
        COMMAND_GAME_STOP,
        COMMAND_HUB
    }

    public GameState _gameState = GameState.LOADING_MAP;
    private CoreCommand _coreCommand;
    private CorePortal _corePortal;

    public ArcadeGame(final Arcade arcade) {
        super(arcade,
                "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME_SET);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HUB);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable() {
        _coreCommand.register(new CommandGame(this));
        _coreCommand.register(new CommandHub(this,
                _corePortal));
    }

}
