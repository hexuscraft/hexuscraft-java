package net.hexuscraft.core.permission;

import java.util.Optional;
import java.util.UUID;

public class BasePermission {

    public final UUID _uuid;

    public BasePermission() {
        _uuid = UUID.randomUUID();
    }

    public BasePermission(UUID uuid) {
        _uuid = uuid;
    }

    @Override
    public final String toString() {
        return _uuid.toString();
    }

    public final Optional<PermissionGroup> getMinimumGroup() {
        for (PermissionGroup group : PermissionGroup.values()) {
            if (!group._permissions.contains(this)) { continue; }
            return Optional.of(group);
        }
        return Optional.empty();
    }

}