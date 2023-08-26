package net.hexuscraft.core.party;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.party.command.PartyCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginParty extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_PARTY
    }

    PluginCommand _pluginCommand;

    public PluginParty(JavaPlugin javaPlugin) {
        super(javaPlugin, "Party");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PARTY);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new PartyCommand(this));
    }
}
