package net.hexuscraft.arcade.tab;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.player.PlayerTabInfo;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class ArcadeTab extends MiniPlugin<Arcade>
{

    CorePortal _corePortal;

    public ArcadeTab(Arcade arcade)
    {
        super(arcade, "Tab");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        PlayerTabInfo.setHeaderFooter(player, F.fTabHeader(_corePortal._serverName), " ");
    }


}
