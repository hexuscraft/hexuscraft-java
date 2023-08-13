package net.hexuscraft.core;

import org.bukkit.entity.Player;

public class MiniPluginClient extends MiniPlugin {

    public final MiniPlugin _miniPlugin;
    public final Player _player;

    protected MiniPluginClient(MiniPlugin miniPlugin, Player player) {
        super(miniPlugin._javaPlugin, miniPlugin._name + " (" + player.getName() + ")");
        _miniPlugin = miniPlugin;
        _player = player;
    }

}
