package net.hexuscraft.core.actionbar;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.player.UtilTitleTab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class CoreActionBar extends MiniPlugin<HexusPlugin>
{

    Map<Player, Set<ActionBar>> _actionBarMap;

    public CoreActionBar(HexusPlugin plugin)
    {
        super(plugin, "Action Bar");
        _actionBarMap = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        _hexusPlugin.runAsyncTimer(this::updateActionBars, 0, 20);
    }

    public void updateActionBars()
    {
        _actionBarMap.forEach((player, actionBars) ->
        {
            if (actionBars.isEmpty())
            {
                return;
            }
            UtilTitleTab.sendActionText(player,
                    actionBars.stream().max(Comparator.comparing(ActionBar::getWeight)).get().getMessage());
        });
    }

    public ActionBar registerActionBar(ActionBar actionBar)
    {
        Set<ActionBar> actionBars;
        if (_actionBarMap.containsKey(actionBar._player))
        {
            actionBars = _actionBarMap.get(actionBar._player);
        }
        else
        {
            actionBars = new HashSet<>();
            _actionBarMap.put(actionBar._player, actionBars);
        }
        actionBars.add(actionBar);

        return actionBar;
    }

    public void unregisterActionBar(ActionBar actionBar)
    {
        Player player = actionBar._player;

        if (_actionBarMap.containsKey(player))
        {
            Set<ActionBar> actionBars = _actionBarMap.get(player);
            actionBars.remove(actionBar);

            if (!actionBars.isEmpty())
            {
                return;
            }
            _actionBarMap.remove(player);
        }
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (!_actionBarMap.containsKey(player))
        {
            return;
        }
        _actionBarMap.get(player).forEach(this::unregisterActionBar);
    }

}
