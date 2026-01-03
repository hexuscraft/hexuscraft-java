package net.hexuscraft.core.permission;

import net.hexuscraft.common.enums.PermissionGroup;
import org.bukkit.permissions.PermissionAttachment;

public record PermissionProfile(PermissionGroup[] _groups, PermissionAttachment _attachment) {

}
