package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.MiniPluginClient;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheatClient extends MiniPluginClient {

    private Map<String, Integer> violations;
    private List<Guardian> guardians;

    public CheatClient(PluginCheat pluginCheat, Player player) {
        super(pluginCheat, player);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> miniPluginClassMap) {
        violations = new HashMap<>();
        guardians = new ArrayList<>();
    }

    @Override
    public final void onDisable() {
        violations.clear();
        violations = null;

        for (Guardian guardian : guardians) {
            guardian.remove();
        }
        guardians.clear();
        guardians = null;
    }

    private Guardian spawnGuardian(Location location) {
        Player player = _player;
        Guardian guardian = (Guardian) player.getWorld().spawnEntity(player.getLocation(), EntityType.GUARDIAN);
        guardian.setTarget(player);
        guardian.setCustomName(C.cRed + C.fBold + "HAC");
        guardian.setCustomNameVisible(true);
        guardian.setMetadata("Invulnerable", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("PersistenceRequired", new FixedMetadataValue(_javaPlugin, 1));
//        guardian.setMetadata("Silent", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("NoAI", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("CustomName", new FixedMetadataValue(_javaPlugin, guardian.getCustomName()));
        guardian.teleport(location);
        return guardian;
    }

    @SuppressWarnings("unused")
    private void ban(Player player, String reason, CheatSeverity severity, int count) {
        Guardian guardian0 = spawnGuardian(player.getLocation().add(new Vector(3, 3, 0)));
        Guardian guardian1 = spawnGuardian(player.getLocation().add(new Vector(-3, 3, 0)));
        Guardian guardian2 = spawnGuardian(player.getLocation().add(new Vector(0, 3, 3)));
        Guardian guardian3 = spawnGuardian(player.getLocation().add(new Vector(0, 3, -3)));
        guardians.add(guardian0);
        guardians.add(guardian1);
        guardians.add(guardian2);
        guardians.add(guardian3);
    }

    @SuppressWarnings("unused")
    private void kick(Player player, String reason, CheatSeverity severity, int count) {
        _javaPlugin.getServer().broadcastMessage(F.fMain(this) + F.fItem(player) + " kicked for " + severity.getColor() + reason);
        player.kickPlayer(C.cRed + C.fBold + "You were kicked by Hexuscraft Anti-Cheat" + C.fReset + C.cWhite + "\n" + reason);
    }

    private void alert(Player player, String reason, CheatSeverity severity, int count) {
//        _javaPlugin.getServer().broadcastMessage(F.fMain(this, F.fPlayer(player) + " suspected of " + severity.getColor() + reason));
//        ComponentBuilder builder = new ComponentBuilder("abc");
        _javaPlugin.getServer().broadcastMessage(F.fCheat("HAC", player, severity, reason, count, "Lobby-1"));
    }

    public final void flag(Player player, String reason, CheatSeverity severity) {
        String keyName = reason + ":" + severity.name();
        if (!violations.containsKey(keyName)) {
            violations.put(keyName, 0);
        }
        int newCount = violations.get(keyName) + 1;
        violations.put(keyName, newCount);

        alert(player, reason, severity, newCount);
        if (newCount % 10 == 0) {
            ban(player, reason, severity, newCount);
        }
    }

}
