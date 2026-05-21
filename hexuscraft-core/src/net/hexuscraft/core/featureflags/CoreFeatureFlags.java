package net.hexuscraft.core.featureflags;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;

public class CoreFeatureFlags extends MiniPlugin<HexusPlugin> {

    public CoreFeatureFlags(HexusPlugin plugin) {
        super(plugin, "Feature Flags");
    }

    public enum PERM implements IPermission {
        COMMAND_FEATURE_FLAGS
    }

    public enum FLAG {

    }

}
