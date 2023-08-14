package net.hexuscraft.core.permission;

import net.hexuscraft.core.chat.C;

import java.util.ArrayList;
import java.util.List;

public enum PermissionGroup {

//    Special

//    _COMMAND_BROADCAST("", C.cWhite),
//    _COMMAND_SILENCE("", C.cWhite),
//
//    _SERVER_CO_HOST("Co-Host", C.cDGreen, _COMMAND_BROADCAST, _COMMAND_SILENCE),
//    _SERVER_HOST("Host", C.cDGreen, _SERVER_CO_HOST),
//    _OPERATOR("", C.cWhite),
//    _AUTHENTICATE("", C.cWhite),
//    _CHAT_PREFIX("", C.cWhite),

//    Teams

    QUALITY_ASSURANCE("QA", C.cWhite),

    EVENT_MOD("E.Mod", C.cDGreen),
    EVENT_LEAD("E.Lead", C.cDGreen, EVENT_MOD),

//    Ranks

    MEMBER("Member", C.cWhite),

    VIP("VIP", C.cGreen, MEMBER),
    MVP("MVP", C.cAqua, VIP),

    MEDIA("Media", C.cDPurple, MVP),

    BUILDER("Builder", C.cBlue, MVP),
    SENIOR_BUILDER("Sr.Build", C.cBlue, BUILDER),

    TRAINEE("Trainee", C.cDAqua, MVP),
    MODERATOR("Mod", C.cGold, TRAINEE),
    SENIOR_MODERATOR("Sr.Mod", C.cGold, MODERATOR),

    ADMINISTRATOR("Admin", C.cRed, SENIOR_MODERATOR, SENIOR_BUILDER, MEDIA, EVENT_LEAD, QUALITY_ASSURANCE),
    DEVELOPER("Dev", C.cRed, ADMINISTRATOR),
    LEADER("Leader", C.cRed, DEVELOPER),
    OWNER("Owner", C.cRed, LEADER);

    public static final PermissionGroup DEFAULT = MEMBER;

    public final String _prefix;
    public final String _color;
    public final List<IPermission> _permissions;
    public final PermissionGroup[] _parents;

    PermissionGroup(String prefix, String color, PermissionGroup... parents) {
        _prefix = prefix;
        _color = color;
        _permissions = new ArrayList<>();
        _parents = parents;
    }

    public static String[] getColoredNames(boolean skipServerGroups) {
        List<String> names = new ArrayList<>();
        for (PermissionGroup group : PermissionGroup.values()) {
            String groupName = group.name();
            if (skipServerGroups && groupName.startsWith("_")) {
                continue;
            }
            names.add(group._color + groupName);
        }
        return names.toArray(new String[0]);
    }

    public static String[] getColoredNames() {
        return getColoredNames(true);
    }

}