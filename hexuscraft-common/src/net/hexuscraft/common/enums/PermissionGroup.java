package net.hexuscraft.common.enums;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.utils.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum PermissionGroup {

//    Base Ranks

    PLAYER("Player",
            C.cWhite,
            1,
            "A normal Hexian player. Thanks for playing our server!"),
    VIP("VIP",
            C.cGreen,
            2,
            "The first purchasable rank, available at store.hexuscraft.net.",
            PLAYER),
    MVP("MVP",
            C.cAqua,
            3,
            "The second purchasable rank, available at store.hexuscraft.net.",
            VIP),
    MEDIA("Media",
            C.cDPurple,
            4,
            "Content creators and influencers who create Minecraft gameplay videos.",
            MVP),

//    Public Teams

    BUILD_TEAM("Builder",
            C.cBlue,
            10,
            "Builders are responsible for creating and updating our game and lobby maps.",
            MEDIA), // Build Team
    BUILD_LEAD("BuildLead",
            C.cWhite,
            0,
            "Build Lead",
            BUILD_TEAM), // Build Team Lead

    EVENT_TEAM("E.Team",
            C.cWhite,
            0,
            "Event Team"), // Events Team
    EVENT_LEAD("E.Lead",
            C.cWhite,
            0,
            "Event Lead",
            EVENT_TEAM), // Events Team Lead

    MEDIA_TEAM("M.Team",
            C.cWhite,
            0,
            "Media Team",
            MEDIA), // Media Team
    MEDIA_LEAD("M.Lead",
            C.cWhite,
            0,
            "Media Lead",
            MEDIA_TEAM), // Media Team Lead

    QUALITY_ASSURANCE_TEAM("QaTeam",
            C.cWhite,
            0,
            "QA Team"),
    QUALITY_ASSURANCE_LEAD("QaLead",
            C.cWhite,
            0,
            "QA Lead",
            QUALITY_ASSURANCE_TEAM),

// Staff-Only Teams

    STAFF_MANAGEMENT_TEAM("SmTeam",
            C.cWhite,
            0,
            "Staff Management Team"),
    STAFF_MANAGEMENT_LEAD("SmLead",
            C.cWhite,
            0,
            "Staff Management Lead",
            STAFF_MANAGEMENT_TEAM),

//    Staff Ranks

    TRAINEE("Trainee",
            C.cDAqua,
            200,
            "Trainees are moderators-in-training, undergoing a trial phase before becoming a fully-fleged moderator. You can contact them for assistance with /a.",
            MVP),
    MODERATOR("Mod",
            C.cYellow,
            201,
            "Moderators are responsible for assisting players, handling player reports and punishing rule-breakers. You can contact them for assistance with /a.",
            TRAINEE),
    SENIOR_MODERATOR("Sr.Mod",
            C.cGold,
            202,
            "Senior Moderators are moderators who have joined a staff-only team. They are responsible for performing their team's duties in addition to assisting players, handling player reports and punishing rule-breakers. You can contact them for assistance with /a.",
            MODERATOR),
    ADMINISTRATOR("Admin",
            C.cRed,
            203,
            "Administrators lead a team. They are responsible for managing their team ",
            SENIOR_MODERATOR),

    ;

    public final String _prefix;
    public final String _color;
    public final int _weight;
    public final String _description;
    public final PermissionGroup[] _inherits;

    public final List<IPermission> _permissions;

    PermissionGroup(final String prefix, final String color, final int weight, final String description, final PermissionGroup... inherits) {
        _prefix = prefix;
        _color = color;
        _weight = weight;
        _description = description;
        _inherits = inherits;
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
        return Arrays.stream(potentialGroups)
                .min(Comparator.comparingInt(group -> group._weight))
                .orElse(null);
    }

    public static PermissionGroup getGroupWithHighestWeight(final PermissionGroup[] potentialGroups) {
        return Arrays.stream(potentialGroups)
                .max(Comparator.comparingInt(group -> group._weight))
                .orElse(null);
    }

}