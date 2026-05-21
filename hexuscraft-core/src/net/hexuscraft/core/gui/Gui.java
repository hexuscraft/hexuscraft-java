package net.hexuscraft.core.gui;

import org.bukkit.entity.Player;

public class Gui extends IGui {
    public Gui(GuiType guiType, Player player) {
        _guiType = guiType;
        _player = player;
    }
}
