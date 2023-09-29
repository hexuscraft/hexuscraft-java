package net.hexuscraft.arcade.game;

public abstract class Game {

    public final String _name;
    public final GameTeam[] _teams;

    protected Game(final String name, final GameTeam[] teams) {
        _name = name;
        _teams = teams;
    }

}
