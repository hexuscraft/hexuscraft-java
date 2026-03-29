package net.hexuscraft.core.database;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;

public class CoreDatabase extends MiniPlugin<HexusPlugin>
{

    public Database _database;

    public CoreDatabase(HexusPlugin plugin)
    {
        super(plugin, "Database");
        _database = new Database();
    }

    @Override
    public void onDisable()
    {
        _database.unregisterConsumers();
    }

}