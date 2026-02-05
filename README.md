# Hexuscraft-Java

Plugins for the Hexuscraft server on Minecraft Java Edition

> **hexuscraft-server-monitor** scripts: https://github.com/hexuscraft/hexuscraft-scripts

## Building

### Dependencies

You must provide your own CraftBukkit jar. The easiest way to do this is compiling CraftBukkit using BuildTools
available at https://www.spigotmc.org/wiki/buildtools/

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

> - SURVIVAL_GAMES
> - MICRO_BATTLES
> - SKYWARS

### PermissionGroup

> - PLAYER *(default)*
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

## Redis Keys

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
> *`^` required if `active` is `false`*
>
> |              Field | Type                         |
> |-------------------:|:-----------------------------|
> |              type* | **String** (PunishType Enum) |
> |            active* | **Boolean**                  |
> |            origin* | **Long**                     |
> |            length* | **Long**                     |
> |            reason* | **String**                   |
> |            server* | **String**                   |
> |           staffId* | **String** (UUID)            |
> |       staffServer* | **String**                   |
> |      removeOrigin^ | **Long**                     |
> |      removeReason^ | **String**                   |
> |      removeServer^ | **String**                   |
> |     removeStaffId^ | **String** (UUID)            |
> | removeStaffServer^ | **String**                   | 

> **SET** `user:(UUID):punishments`
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
