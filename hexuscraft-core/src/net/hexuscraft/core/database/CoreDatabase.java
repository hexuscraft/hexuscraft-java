package net.hexuscraft.core.database;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;

public final class CoreDatabase extends MiniPlugin<HexusPlugin> {

    public final Database _database;

    public CoreDatabase(final HexusPlugin plugin) {
        super(plugin,
                "Database");
        _database = new Database();
    }

    @Override
    public void onDisable() {
        _database.unregisterConsumers();
    }

}