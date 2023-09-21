package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class AsyncRunnable {

    public AsyncRunnable(MiniPlugin plugin, ParameterizedRunnable request, ParameterizedRunnable callback) {
        final BukkitScheduler scheduler = plugin._javaPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(plugin._javaPlugin, () -> {
            final Object response = request.run();
            scheduler.runTask(plugin._javaPlugin, () -> callback.run(response));
        });
    }

}
