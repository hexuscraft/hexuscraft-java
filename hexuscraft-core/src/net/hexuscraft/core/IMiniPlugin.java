package net.hexuscraft.core;

import java.util.Map;

public interface IMiniPlugin {

    default void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
    }

    default void onEnable() {
    }

    default void onDisable() {
    }

}
