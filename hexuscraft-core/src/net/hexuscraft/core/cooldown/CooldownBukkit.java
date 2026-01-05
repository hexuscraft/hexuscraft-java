package net.hexuscraft.core.cooldown;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilCooldown;
import org.bukkit.command.CommandSender;

public class CooldownBukkit {

    private Long calculateRemaining(final Long now, final Long start, final Long delay) {
        return delay - (now - start);
    }

    public boolean use(final Object parent, final String name, final Long delayMs, final CommandSender sender) {
        if (UtilCooldown.use(parent, name, delayMs)) return true;

        final UtilCooldown.Cooldown cooldown = UtilCooldown.getCooldown(parent, name);
        if (cooldown == null) {
            sender.sendMessage(F.fMain(this, "Please wait before trying to use ", F.fItem(name), " again."));
            return false;
        }

        sender.sendMessage(F.fMain(this, "You cannot use ", F.fItem(name), " for another ",
                F.fTime(calculateRemaining(System.currentTimeMillis(), cooldown._started(),
                        cooldown._delayMs()))));
        return false;
    }

}
