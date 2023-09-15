package net.hexuscraft.core;

import org.bukkit.plugin.Plugin;

public interface IHexusPlugin extends Plugin {

    default void load() {}

    default void enable() {}

    default void disable() {}

}
