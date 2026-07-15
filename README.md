# Hexuscraft-Java

Plugins for the Hexuscraft server on Minecraft Java Edition

> **hexuscraft-server-monitor** scripts: https://github.com/hexuscraft/hexuscraft-scripts

## Building

### Dependencies

You must provide your own CraftBukkit jar. The easiest way to do this is compiling CraftBukkit using BuildTools
available at https://www.spigotmc.org/wiki/buildtools/

Make sure to specify compilation of CraftBukkit 1.8.8

Remember to add the file as a library to your local project. You can usually do this in your IDE but I prefer using a
maven goal command:

```
mvn install:install-file -Dfile=lib/craftbukkit-1.8.8.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.8.8-R0.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
```

## Enums

### CheatSeverity

> - LOW
> - MEDIUM
> - HIGH

### CurrencyType

> - COIN
> - XP

### GameType

> - THE_BRIDGES
> - SURVIVAL_GAMES
> - SURVIVAL_GAMES_2
> - MICRO_BATTLES
> - SKYWARS
> - SKYWARS_2
> - CAKE_WARS
> - CAKE_WARS_2

### PermissionGroup

- Ranks beginning with an underscore `_` are unassignable

> - _PLAYER *(default)*
> - VIP
> - MVP
> - MEDIA
>
>
> - BUILD_TEAM
> - BUILD_LEAD
>
>
> - EVENT_TEAM
> - EVENT_LEAD
>
>
> - MEDIA_TEAM
> - MEDIA_LEAD
>
>
> - QUALITY_ASSURANCE_TEAM
> - QUALITY_ASSURANCE_LEAD
>
>
> - STAFF_MANAGEMENT_TEAM
> - STAFF_MANAGEMENT_LEAD
>
>
> - TRAINEE
> - MODERATOR
> - SENIOR_MODERATOR
> - ADMINISTRATOR
> - _CONSOLE

### PunishType

> - WARNING
> - KICK
> - MUTE
> - BAN

### ReportCloseReason

> - PUNISHED
> - INSUFFICIENT_EVIDENCE

### ReportSubmitReason

> - CHAT
>- GAMEPLAY
>- CLIENT
>- MISC

### ServerType

> - VELOCITY
> - BUKKIT

## Redis Data

*`*` required*

### News

> **HASH** `news:(UUID)`
>
> |   Field | Type        |
> |--------:|:------------|
> |  active | **Boolean** |
> |  weight | **Integer** |
> | message | **String**  |

### Punishments

> **HASH** `punishment:(UUID)`
>
> *`*` = required*
>
> *`^` = required if `active` is `false`*
>
> |               Field | Type           |
> |--------------------:|:---------------|
> |               uuid* | **UUID**       |
> |               type* | **PunishType** |
> |             active* | **Boolean**    |
> |             origin* | **Long**       |
> |             length* | **Long**       |
> |             reason* | **String**     |
> |         targetUUID* | **UUID**       |
> |       targetServer* | **String**     |
> |          staffUUID* | **UUID**       |
> |        staffServer* | **String**     |
> |       removeOrigin^ | **Long**       |
> |       removeReason^ | **String**     |
> | removeTargetServer^ | **String**     |
> |    removeStaffUUID^ | **UUID**       |
> |  removeStaffServer^ | **String**     | 

> **SET** `user:(UUID):punishments:received`
> - `(UUID)` of redis keys `punishment:(UUID)`

> **SET** `user:(UUID):punishments:issued`
> - `(UUID)` of redis keys `punishment:(UUID)`

> **SET** `user:(UUID):punishments:revoked`
> - `(UUID)` of redis keys `punishment:(UUID)`

### Reports

> **HASH** `report:(UUID)`
>
> *`*` = required*
>
> *`^` = required if `active` is `false`*
>
> |            Field | Type                          |
> |-----------------:|:------------------------------|
> |            uuid* | **UUID**                      |
> |      senderUUID* | **UUID**                      |
> |      targetUUID* | **UUID**                      |
> |         message* | **String**                    |
> |          reason* | **ReportSubmitReason** *enum* |
> |          active* | **Boolean**                   |
> |          origin* | **Long**                      |
> |          server* | **String**                    |
> |    removeOrigin^ | **Long**                      |
> |    removeReason^ | **String**                    |
> |    removeServer^ | **String**                    |
> | removeStaffUUID^ | **UUID**                      |

> **SET** `user:(UUID):reports:submitted`
> - `(UUID)` of redis keys `report:(UUID)`

> **SET** `user:(UUID):reports:received`
> - `(UUID)` of redis keys `report:(UUID)`

### Servers

> **HASH** `server:(String)`
>
> |            Field | Type              |
> |-----------------:|:------------------|
> |          address | **String** (IPv4) |
> |         capacity | **Integer**       |
> |    createdMillis | **Long**          |
> |            group | **String**        |
> |             motd | **String**        |
> |          players | **Integer**       |
> |             port | **Integer**       |
> |              tps | **Double**        |
> |          updated | **Long**          |
> | updatedByMonitor | **Boolean**       |

### Server Groups

> **HASH** `servergroup:(String)`
>
> \* = required
>
> |            Field | Type                                     |
> |-----------------:|------------------------------------------|
> |         capacity | **Integer**                              |
> |         fallback | **Boolean**                              |
> |            games | **GameType[]** _split with comma_        |
> |             host | **UUID**                                 |
> |  joinableServers | **Integer**                              |
> |        maxPort\* | **Integer**                              |
> |        minPort\* | **Integer**                              |
> |         plugin\* | **String**                               |
> |            ramMB | **Integer**                              |
> | permissionGroups | **PermissionGroup[]** _split with comma_ |
> |     totalServers | **Integer**                              |
> |    timeoutMillis | **Integer**                              |
> |        worldEdit | **Boolean**                              |
> |         worldZip | **String**                               |

### Permissions

> If you have successfully set up the network and are looking to change your own rank, you can also execute the `/rank`
> command from the console to make things a bit easier.
>
> Note that you must run this command from a Notchian server running a HexusPlugin, such as Hub, Arcade, Web,
> etc. You cannot _(currently)_ run this command on Proxy or ServerMonitor.
>
> Refer to the PermissionGroup enum for a list of ranks.
>
> - Example: `/rank add joeshmoe ADMINISTRATOR`

> **SET** `user:(UUID):permission:groups`
>
> - PermissionGroup Enum

### Motd

> **STRING** `motd`
> - Changes the MOTD on proxy servers. Example:
> ```
> §eMINI-GAMES, PRIVATE SERVERS, TOURNAMENTS
> ```
