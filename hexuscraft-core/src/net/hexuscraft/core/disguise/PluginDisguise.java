package net.hexuscraft.core.disguise;

import com.mojang.authlib.GameProfile;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.disguise.command.CommandDisguise;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class PluginDisguise extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_DISGUISE
    }

    PluginCommand _pluginCommand;

    public PluginDisguise(final HexusPlugin plugin) {
        super(plugin, "Disguise");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);

        PermissionGroup.MEDIA._permissions.add(PERM.COMMAND_DISGUISE);
    }

    @Override
    public final void onEnable() {
        _pluginCommand.register(new CommandDisguise(this));
    }

    public final boolean disguise(Player player, EntityType entityType, String disguiseName) throws NoSuchFieldException, IllegalAccessException, DisguiseUnsupportedEntityException {
        Server server = _plugin.getServer();

        if (entityType == EntityType.PLAYER) {
            CraftPlayer craftPlayer = (CraftPlayer) player;

            final MojangProfile profile = PlayerSearch.fetchMojangProfile(disguiseName, player);
            if (profile == null) {
                return false;
            }

            GameProfile playerProfile = craftPlayer.getProfile();
            GameProfile targetProfile = new GameProfile(profile.uuid, profile.name);

            String newName = targetProfile.getName();

            Field gameProfileNameField = playerProfile.getClass().getDeclaredField("name");
            gameProfileNameField.setAccessible(true);
            gameProfileNameField.set(playerProfile, newName);
            gameProfileNameField.setAccessible(false);

            playerProfile.getProperties().putAll(targetProfile.getProperties());

            player.setDisplayName(newName);

            server.getOnlinePlayers().forEach(player1 -> {
                player1.hidePlayer(player);
                player1.showPlayer(player);
            });

            _plugin.getServer().getPluginManager().callEvent(new DisguiseEvent(player, playerProfile, targetProfile));

            return true;
        }

        throw new DisguiseUnsupportedEntityException("Unsupported entity type");
    }

}
