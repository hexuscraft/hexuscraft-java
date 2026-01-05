package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandAnnouncement extends BaseCommand<MiniPluginChat> {

    final MiniPluginDatabase _miniPluginDatabase;

    public CommandAnnouncement(final MiniPluginChat miniPluginChat, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginChat, "announce", "<Permission Group> <Message>", "Broadcast a message to the entire network.",
                Set.of("announcement"), MiniPluginChat.PERM.COMMAND_ANNOUNCEMENT);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final PermissionGroup permissionGroup;
        try {
            permissionGroup = PermissionGroup.valueOf(args[0]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this, F.fItem(args[0]), " is not a valid group. Groups: ",
                    F.fItem(PermissionGroup.getColoredNames())));
            return;
        }

        // TODO: Async-ify
        _miniPluginDatabase.getUnifiedJedis().publish((_miniPlugin).CHANNEL_ANNOUNCEMENT,
                sender.getName() + "," + permissionGroup.name() + "," +
                        String.join(" ", Arrays.stream(args).skip(1).toArray(String[]::new)));
        sender.sendMessage(F.fMain(this, "Message has been broadcast."));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        }
        return List.of();
    }
}