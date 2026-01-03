package net.hexuscraft.common.enums;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.chat.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum PermissionGroup {

//    Base Ranks

    MEMBER("Member", C.cWhite, 1), // Default rank for all new players
    VIP("VIP", C.cGreen, 2, MEMBER), // First purchasable rank available at store.hexuscraft.net
    MVP("MVP", C.cAqua, 3, VIP), // Second purchasable rank available at store.hexuscraft.net
    MEDIA("Media", C.cDPurple, 4, MVP), // Influencers, content creators and other famous personnel

//    Teams

    BUILD_TEAM("BuildTeam", C.cWhite, 0), // Build Team
    BUILD_LEAD("BuildLead", C.cWhite, 0, BUILD_TEAM), // Build Team Lead

    DEV_TEAM("DevTeam", C.cWhite, 0), // Dev Team
    DEV_LEAD("DevLead", C.cWhite, 0, DEV_TEAM), // Dev Team Lead

    EVENT_TEAM("EventTeam", C.cWhite, 0), // Events Team
    EVENT_LEAD("EventLead", C.cWhite, 0, EVENT_TEAM), // Events Team Lead

    MEDIA_TEAM("MediaTeam", C.cWhite, 0, MEDIA), // Media Team
    MEDIA_LEAD("MediaLead", C.cWhite, 0, MEDIA_TEAM), // Media Team Lead

    QA_TEAM("QaTeam", C.cWhite, 0), // QA Team
    QA_LEAD("QaLead", C.cWhite, 0, QA_TEAM), // QA Team Lead

//    Staff Ranks

    TRAINEE("Trainee", C.cDAqua, 5, MVP), // Newly accepted moderators-in-training
    MODERATOR("Mod", C.cYellow, 6, TRAINEE), // Moderators who help moderate the server
    SENIOR_MODERATOR("Sr.Mod", C.cGold, 7, MODERATOR), // Moderators who have joined a staff-only team
    ADMINISTRATOR("Admin", C.cRed, 8,
            SENIOR_MODERATOR), // Leaders of a staff-only team with access to all network permissions

    ;

    public final String _prefix;
    public final String _color;
    public final int _weight;
    public final PermissionGroup[] _parents;
    public final List<IPermission> _permissions;

    PermissionGroup(final String prefix, final String color, final int weight, final PermissionGroup... parents) {
        _prefix = prefix;
        _color = color;
        _weight = weight;
        _parents = parents;
        _permissions = new ArrayList<>();
    }

    public static String[] getColoredNames(final boolean skipServerGroups) {
        final List<String> names = new ArrayList<>();
        for (final PermissionGroup group : PermissionGroup.values()) {
            final String groupName = group.name();
            if (skipServerGroups && groupName.startsWith("_")) continue;
            names.add(group._color + groupName);
        }
        return names.toArray(new String[0]);
    }

    public static String[] getColoredNames() {
        return getColoredNames(true);
    }

    public static PermissionGroup getGroupWithLowestWeight(final PermissionGroup[] potentialGroups) {
        return Arrays.stream(potentialGroups).min(Comparator.comparingInt(group -> group._weight)).orElse(null);
    }

    public static PermissionGroup getGroupWithHighestWeight(final PermissionGroup[] potentialGroups) {
        return Arrays.stream(potentialGroups).max(Comparator.comparingInt(group -> group._weight)).orElse(null);
    }

}