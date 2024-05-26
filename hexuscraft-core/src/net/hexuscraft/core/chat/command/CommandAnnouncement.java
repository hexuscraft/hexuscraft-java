package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandAnnouncement extends BaseCommand<HexusPlugin> {

    final PluginDatabase _pluginDatabase;

    public CommandAnnouncement(PluginChat pluginChat, PluginDatabase pluginDatabase) {
        super(pluginChat, "announce", "<Permission Group> <Message>", "Broadcast a server-wide message.", Set.of("announcement"), PluginChat.PERM.COMMAND_ANNOUNCEMENT);
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        PermissionGroup permissionGroup;
        try {
            permissionGroup = PermissionGroup.valueOf(args[0]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this) + F.fItem(args[0]) + " is not a valid group. Groups: " + F.fList(PermissionGroup.getColoredNames()));
            return;
        }

        List<String> messageList = new ArrayList<>(Arrays.stream(args).toList());
        messageList.remove(0);
        _pluginDatabase.getJedisPooled().publish(((PluginChat) _miniPlugin).CHANNEL_ANNOUNCEMENT, sender.getName() + "," + permissionGroup.name() + "," + String.join(" ", messageList));
        sender.sendMessage(F.fMain(this) + "Message has been broadcast.");
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        }
        return List.of();
    }
}