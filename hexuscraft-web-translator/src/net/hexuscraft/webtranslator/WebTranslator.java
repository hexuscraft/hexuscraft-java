package net.hexuscraft.webtranslator;

import net.hexuscraft.core.HexusPlugin;

public final class WebTranslator extends HexusPlugin {

    public WebTranslator() {
    }

    @Override
    public void enable() {
        runSyncLater(() -> getServer().unloadWorld("world", false), 20 * 20);
    }
}