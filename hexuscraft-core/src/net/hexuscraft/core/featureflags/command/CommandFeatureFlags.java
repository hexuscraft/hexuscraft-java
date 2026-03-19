package net.hexuscraft.core.featureflags.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.featureflags.CoreFeatureFlags;

import java.util.Set;

public final class CommandFeatureFlags extends BaseMultiCommand<CoreFeatureFlags> {

    public CommandFeatureFlags(final CoreFeatureFlags miniPlugin) {
        super(miniPlugin, "featureflags", "View and change flags to modify server or network behaviour.",
                Set.of("featflags", "flags"), CoreFeatureFlags.PERM.COMMAND_FEATURE_FLAGS, Set.of());
    }

}
