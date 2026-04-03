# Hexuscraft-Java

Plugins for the Hexuscraft server on Minecraft Java Edition

> **hexuscraft-server-monitor** scripts: https://github.com/hexuscraft/hexuscraft-scripts

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
> - DEV_TEAM
> - DEV_LEAD
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

### PunishType

> - WARNING
> - KICK
> - MUTE
> - BAN

### ServerType

> - VELOCITY
> - BUKKIT

## Redis Data

*`*` required*

### Server Groups

> **HASH** `servergroup:(String)`
>
> |              Field | Type                                    |
> |-------------------:|:----------------------------------------|
> |           capacity | **Integer**                             |
> |              games | **String** (Game Enums split by commas) |
> |       hostUniqueId | **String** (Player UUID)                |
> |    joinableServers | **Integer**                             |
> |            maxPort | **Integer**                             |
> |            minPort | **Integer**                             |
> |             plugin | **String**                              |
> |                ram | **Integer**                             |
> | requiredPermission | **String** (PermissionGroup Enum)       |
> |       totalServers | **Integer**                             |
> |      timeoutMillis | **Integer**                             |
> |          worldEdit | **Boolean**                             |
> |           worldZip | **String**                              |

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

### Permissions

> If you have successfully set up the network and are looking to change your own rank, you can also execute the `/rank`
> command from the console to make things a bit easier.
>
> Note that you must run this command from a Notchian server running a HexusPlugin, such as Hub, Arcade, WebTranslator,
> etc. You cannot run this command on Proxy or ServerMonitor.
>
> Refer to the PermissionGroup enum for a list of ranks.
>
> - Example: `/rank add Notch ADMINISTRATOR`

> **SET** `user:(UUID):permission:groups`
>
> - PermissionGroup Enum

### Punishments

> **HASH** `punishment:(UUID)`
>
> *`^` required if `active` is `FALSE`*
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

### Motd

> **STRING** `motd`
> - Changes the MOTD on proxy servers. Example:
> ```
> §eMINI-GAMES, PRIVATE SERVERS, TOURNAMENTS
> ```

### News

> **HASH** `news:(UUID)`
>
> |   Field | Type        |
> |--------:|:------------|
> |  active | **Boolean** |
> |  weight | **Integer** |
> | message | **String**  |
