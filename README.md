# Hexuscraft-Java

Plugins for the Hexuscraft server on Minecraft Java Edition

## Redis Data

### HASH punishment.(UUID)

| Field         | Type        |
|---------------|-------------|
| reason        | **String**  |
| origin        | **Long**    |
| active        | **Boolean** |
| staffServer   | **String**  |
| staffUuid     | **UUID**    |
| server        | **String**  |
| removeOrigin  | **Long**    |
| removeStaff   | **UUID**    | 
| removeReason  | **String**  | 

### HASH servergroup.(String)

| Field           | Type                          |
|-----------------|-------------------------------|
| joinableServers | **Integer**                   |
| maxPort         | **Integer**                   |
| minPort         | **Integer**                   |
| totalServers    | **Integer**                   |
| type            | **String** (DEDICATED, PROXY) |

### HASH server.(String)

| Field    | Type                         |
|----------|------------------------------|
| address  | **String**                   |
| capacity | **Integer**                  |
| group    | **String** (servergroup.###) |
| players  | **Integer**                  |
| port     | **Integer**                  |
| updated  | **Long**                     |

### SET user.(UUID).punishments

- UUID

### STRING user.(UUID).permission.primary

- Rank Enum

### SET user.(UUID).permission.additional

- Rank Enum

### STRING motd
- *Anything*
