package net.hexuscraft.arcade.host;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Map;
import java.util.UUID;

public class MiniPluginHost extends MiniPlugin<Arcade> {

    public UUID _hostUniqueId;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPortal _miniPluginPortal;

    public MiniPluginHost(final Arcade arcade) {
        super(arcade, "Host");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
    }

    @Override
    public void onEnable() {
        try {
            final ServerGroupData serverGroupData =
                    ServerQueries.getServerGroup(_miniPluginDatabase.getUnifiedJedis(), _miniPluginPortal._serverGroup);
            _hostUniqueId = serverGroupData._hostUUID;
        } catch (final JedisException ex) {
            ex.printStackTrace();
        }

        _miniPluginPortal.teleportAsync(_hostUniqueId, _miniPluginPortal._serverName);
    }
}
