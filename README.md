# Hexuscraft-Java

Plugins for the Hexuscraft server on Minecraft Java Edition

> **hexuscraft-server-monitor** scripts: https://github.com/hexuscraft/hexuscraft-scripts

## Enums

### PermissionGroup

> - QUALITY_ASSURANCE
> - EVENT_MOD
> - EVENT_LEAD
> - MEMBER
> - VIP
> - MVP
> - MEDIA
> - BUILDER
> - SENIOR_BUILDER
> - TRAINEE
> - MODERATOR
> - SENIOR_MODERATOR
> - ADMINISTRATOR
> - DEVELOPER

### PunishType
> - BAN
> - MUTE
> - WARNING

## Redis Keys

### Server Groups

> **HASH** `servergroup:(String)`
> 
> | Field              | Type                                    |
> |--------------------|-----------------------------------------|
> | requiredPermission | **String** (PermissionGroup Enum)       |
> | maxPort            | **Integer**                             |
> | minPort            | **Integer**                             |
> | totalServers       | **Integer**                             |
> | joinableServers    | **Integer**                             |
> | plugin             | **String**                              |
> | worldZip           | **String**                              |
> | ram                | **Integer**                             |
> | capacity           | **Integer**                             |
> | worldEdit          | **Boolean**                             |
> | games              | **String** (Game Enums split by commas) |

### Servers

> **HASH** `server:(String)`
>
> | Field    | Type              |
> |----------|-------------------|
> | address  | **String** (IPv4) |
> | capacity | **Integer**       |
> | created  | **Long**          |
> | group    | **String**        |
> | motd     | **String**        |
> | players  | **Integer**       |
> | port     | **Integer**       |
> | tps      | **Double**        |
> | updated  | **Long**          |

### Permissions

> **STRING** `user:(UUID):permission:primary`
>
> - PermissionGroup Enum

> **SET** `user:(UUID):permission:groups`
>
> - PermissionGroup Enum 

### Punishments

> **HASH** `punishment:(UUID)`
> 
> | Field             | Type                         |
> |-------------------|------------------------------|
> | type              | **String** (PunishType Enum) |
> | active            | **Boolean**                  |
> | origin            | **Long**                     |
> | length            | **Long**                     |
> | reason            | **String**                   |
> | server            | **String**                   |
> | staffId           | **String** (UUID)            |
> | staffServer       | **String**                   |
> | removeOrigin      | **Long**                     |
> | removeReason      | **String**                   |
> | removeServer      | **String**                   |
> | removeStaffId     | **String** (UUID)            |
> | removeStaffServer | **String**                   | 

>  **SET** `user:(UUID):punishments`
> - `(UUID)` of redis keys `punishment:(UUID)`

### Motd

> **STRING** `motd`
> - Changes the proxy's MOTD. Example:
> ```
> §r         §r§9§m   §r§8§m[ §r   §r§6§lHexuscraft§r  §6§lNetwork§r   §8§m ]§r§9§m   §r
> §r     §eMINI-GAMES, PRIVATE SERVERS, TOURNAMENTS§r
> ```
