package net.hexuscraft.core.permission;

import net.hexuscraft.common.enums.PermissionGroup;

public record PreLoginPermissionProfile(PermissionGroup _primaryGroup,
                                        PermissionGroup[] _secondaryGroups) {

}
