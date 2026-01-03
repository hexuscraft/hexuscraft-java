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

> - SURVIVAL_GAMES
> - MICRO_BATTLES
> - SKYWARS

### PermissionGroup

> - MEMBER
> - VIP
> - MVP
> - MEDIA
> - BUILD_TEAM
> - BUILD_LEAD
> - DEV_TEAM
> - DEV_LEAD
> - EVENT_TEAM
> - EVENT_LEAD
> - MEDIA_TEAM
> - MEDIA_LEAD
> - QA_TEAM
> - QA_LEAD
> - TRAINEE
> - MODERATOR
> - SENIOR_MODERATOR
> - ADMINISTRATOR

### PunishType

> - WARNING
> - KICK
> - MUTE
> - BAN

## Redis Keys

### Server Groups

> **HASH** `servergroup:(String)`
>
> | Field              | Type                                    |
> |--------------------|-----------------------------------------|
> | requiredPermission | **String** (PermissionGroup Enum)       |
> | minPort            | **Integer**                             |
> | maxPort            | **Integer**                             |
> | totalServers       | **Integer**                             |
> | joinableServers    | **Integer**                             |
> | plugin             | **String**                              |
> | worldZip           | **String**                              |
> | ram                | **Integer**                             |
> | capacity           | **Integer**                             |
> | worldEdit          | **Boolean**                             |
> | timeoutMillis      | **Integer**                             |
> | games              | **String** (Game Enums split by commas) |
> | hostUniqueId           | **String** (Player UUID)                |

### Servers

> **HASH** `server:(String)`
>
> | Field            | Type              |
> |------------------|-------------------|
> | address          | **String** (IPv4) |
> | capacity         | **Integer**       |
> | created          | **Long**          |
> | group            | **String**        |
> | motd             | **String**        |
> | players          | **Integer**       |
> | port             | **Integer**       |
> | tps              | **Double**        |
> | updated          | **Long**          |
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
> - Example: `/rank set USERNAME DEVELOPER`

> **STRING** `user:(UUID):permission:primary`
>
> - PermissionGroup Enum

> **SET** `user:(UUID):permission:groups`
>
> - PermissionGroup Enum

### Punishments

> **HASH** `punishment:(UUID)`
>
> *\* = required*
>
> | Field             | Type                         |
> |-------------------|------------------------------|
> | type*             | **String** (PunishType Enum) |
> | active*           | **Boolean**                  |
> | origin*           | **Long**                     |
> | length*           | **Long**                     |
> | reason*           | **String**                   |
> | server*           | **String**                   |
> | staffId*          | **String** (UUID)            |
> | staffServer*      | **String**                   |
> | removeOrigin      | **Long**                     |
> | removeReason      | **String**                   |
> | removeServer      | **String**                   |
> | removeStaffId     | **String** (UUID)            |
> | removeStaffServer | **String**                   | 

> **SET** `user:(UUID):punishments`
> - `(UUID)` of redis keys `punishment:(UUID)`

### Motd

> **STRING** `motd`
> - Changes the proxy's MOTD. Example:
> ```
> §eMINI-GAMES, PRIVATE SERVERS, TOURNAMENTS
> ```
