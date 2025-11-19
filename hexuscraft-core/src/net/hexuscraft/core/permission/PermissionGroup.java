package net.hexuscraft.core.permission;

import net.hexuscraft.core.chat.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionGroup {

//    Base Ranks

    MEMBER("Member", C.cWhite), // Member is the first rank for all new players
    VIP("VIP", C.cGreen, MEMBER), // VIP is the first purchasable rank available on our store
    MVP("MVP", C.cAqua, VIP), // MVP is the second purchasable rank available on our store
    MEDIA("Media", C.cDPurple, MVP), // Media is formed of influencers, content creators and other famous personnel

//    Teams

    BUILD_TEAM("BuildTeam", C.cWhite), // Build Team is responsible for building and updating maps used on our network
    BUILD_LEAD("BuildLead", C.cWhite, BUILD_TEAM), // Build Lead

    DEV_TEAM("DevTeam", C.cWhite), // Dev Team is responsible for the creation of source code used to run and maintain the network
    DEV_LEAD("DevLead", C.cWhite, DEV_TEAM), // Dev Lead

    EVENT_TEAM("EventTeam", C.cWhite), // Event Team is responsible for hosting community events for engagement, fun and prizes
    EVENT_LEAD("EventLead", C.cWhite, EVENT_TEAM), // Event Lead

    MEDIA_TEAM("MediaTeam", C.cWhite), // Media Team is responsible for assisting Media rank recipients
    MEDIA_LEAD("MediaLead", C.cWhite, MEDIA_TEAM), // Media Lead

    QA_TEAM("QualityTeam", C.cWhite), // Quality Assurance Team is responsible for game testing, balancing, suggestions and bug reports
    QA_LEAD("QualityLead", C.cWhite, QA_TEAM), // Quality Assurance Lead

    STAFF_TEAM("StaffTeam", C.cWhite), // Staffing Team is responsible for the selection, induction and assisting of new and existing staff members
    STAFF_LEAD("StaffLead", C.cWhite), // Staffing Lead

//    Staff Ranks

    TRAINEE("Trainee", C.cDAqua, MEDIA), // Trainees are newly accepted moderators-in-training who must undergo several processes to become a fully-fledged moderator
    MODERATOR("Mod", C.cGold, TRAINEE), // Moderators are responsible for handling reports, punishing rule-breakers, and helping players who need assistance
    SENIOR_MODERATOR("Sr.Mod", C.cGold, MODERATOR), // Senior Moderators are moderators who have joined a staff-only team
    ADMINISTRATOR("Admin", C.cGold, SENIOR_MODERATOR, BUILD_LEAD, DEV_LEAD, EVENT_LEAD, MEDIA_LEAD, QA_LEAD, STAFF_LEAD) // Administrators are responsible for the leadership and daily operations of a specific staff-only team

    ;

    public final String _prefix;
    public final String _color;
    public final PermissionGroup[] _parents;
    public final List<IPermission> _permissions;

    PermissionGroup(final String prefix, final String color, final PermissionGroup... parents) {
        _prefix = prefix;
        _color = color;
        _parents = parents;
        _permissions = new ArrayList<>();
    }

    public static String[] getColoredNames() {
        return Arrays.stream(PermissionGroup.values()).map(group -> group._color + group.name()).toArray(String[]::new);
    }

}