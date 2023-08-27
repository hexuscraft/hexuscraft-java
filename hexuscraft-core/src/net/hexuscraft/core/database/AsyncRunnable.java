package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncRunnable {

    public AsyncRunnable(MiniPlugin miniPlugin, ParameterizedRunnable request, ParameterizedRunnable callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Object response = request.run();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.run(response);
                    }
                }.runTask(miniPlugin._javaPlugin);
            }
        }.runTaskAsynchronously(miniPlugin._javaPlugin);
    }

}
