package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandAnnouncement extends BaseCommand<MiniPluginChat> {

    final MiniPluginDatabase _miniPluginDatabase;

    public CommandAnnouncement(final MiniPluginChat miniPluginChat, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginChat, "announce", "<Permission Group> <Message>", "Broadcast a server-wide message.", Set.of("announcement"), MiniPluginChat.PERM.COMMAND_ANNOUNCEMENT);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
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
        messageList.removeFirst();
        _miniPluginDatabase.getJedisPooled().publish((_miniPlugin).CHANNEL_ANNOUNCEMENT, sender.getName() + "," + permissionGroup.name() + "," + String.join(" ", messageList));
        sender.sendMessage(F.fMain(this) + "Message has been broadcast.");
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        }
        return List.of();
    }
}