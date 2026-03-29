package net.hexuscraft.arcade.manager;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.GameSurvivalGames;
import net.hexuscraft.arcade.game.GameSurvivalGamesDuo;
import net.hexuscraft.arcade.game.GameTheBridges;
import net.hexuscraft.arcade.manager.command.CommandGame;
import net.hexuscraft.arcade.manager.command.CommandHub;
import net.hexuscraft.arcade.manager.event.GameStateChangedEvent;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public final class ArcadeManager extends MiniPlugin<Arcade>
{

    public enum PERM implements IPermission
    {
        COMMAND_GAME,
        COMMAND_GAME_SET,
        COMMAND_GAME_START,
        COMMAND_GAME_STOP,
        COMMAND_HUB
    }

    public final AtomicReference<Game> _game = new AtomicReference<>();
    public final AtomicReference<GameMap> _gameMap = new AtomicReference<>();
    private final Map<GameType, Class<? extends Game>> GAME_CLASS_MAP = Map.ofEntries(Map.entry(GameType.SURVIVAL_GAMES,
                                                                                                GameSurvivalGames.class),
                                                                                      Map.entry(GameType.SURVIVAL_GAMES_2,
                                                                                                GameSurvivalGamesDuo.class),
                                                                                      Map.entry(GameType.THE_BRIDGES,
                                                                                                GameTheBridges.class));
    private final Random _nextBestGameRandom = new Random();
    private final AtomicReference<BukkitTask> _gameTickTask = new AtomicReference<>();
    private CoreCommand _coreCommand;
    private CoreDatabase _coreDatabase;
    private CorePortal _corePortal;

    public ArcadeManager(final Arcade arcade)
    {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME_SET);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HUB);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandGame(this));
        _coreCommand.register(new CommandHub(this, _corePortal));

        _gameTickTask.set(_hexusPlugin.runSyncTimer(this::tick, 0, 1));
    }

    @Override
    public void onDisable()
    {
        final BukkitTask oldTask = _gameTickTask.getAndSet(null);
        if (oldTask == null)
        {
            return;
        }
        oldTask.cancel();
    }

    private void tick()
    {
        if (_game.get() == null)
        {
            final GameType nextBestGametype = selectNextBestGame();
            if (nextBestGametype == null)
            {
                logWarning("Could not select next best game!");
                return;
            }

            if (!GAME_CLASS_MAP.containsKey(nextBestGametype))
            {
                logWarning("Could not find class for game type '" + nextBestGametype.name() + "'!");
                return;
            }

            try
            {
                final Constructor<? extends Game> constructor = GAME_CLASS_MAP.get(nextBestGametype)
                                                                              .getDeclaredConstructor(ArcadeManager.class);
                constructor.setAccessible(true);
                _game.set(constructor.newInstance(this));
            }
            catch (final InstantiationException |
                         IllegalAccessException |
                         InvocationTargetException |
                         NoSuchMethodException ex)
            {
                logSevere(ex);
                return;
            }
        }

        switch (getGameState())
        {
            case null ->
            {
                setGameState(GameState.LOADING_MAP);
            }
            case LOADING_MAP ->
            {
                if (_gameMap.get() != null)
                {
                    return;
                }
                _gameMap.set(new GameMap());

                setGameState(GameState.WAITING_FOR_PLAYERS);
            }
            case WAITING_FOR_PLAYERS ->
            {
                setGameState(GameState.START_COUNTDOWN);
            }
            case START_COUNTDOWN ->
            {
                setGameState(GameState.STARTING);
            }
            case STARTING ->
            {
                setGameState(GameState.IN_PROGRESS);
            }
            case IN_PROGRESS ->
            {
                setGameState(GameState.ENDING);
            }
            case ENDING ->
            {
                setGameState(GameState.LOADING_MAP);
            }
        }
    }

    public GameState getGameState()
    {
        final Game game = _game.get();
        return game == null ? null : game._state.get();
    }

    private boolean setGameState(final GameState newState)
    {
        final GameStateChangedEvent event = new GameStateChangedEvent(_game.get()._state.get(), newState);
        _hexusPlugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
        {
            return false;
        }
        _game.get()._state.set(newState);
        return true;
    }

    private GameType selectNextBestGame()
    {
        final GameType[] games = _corePortal.getServerGroup(_corePortal._serverGroupName)._games;
        if (games.length == 0)
        {
            return null;
        }
        if (games.length == 1)
        {
            return games[0];
        }
        return games[_nextBestGameRandom.nextInt(0, games.length - 1)];
    }

}
