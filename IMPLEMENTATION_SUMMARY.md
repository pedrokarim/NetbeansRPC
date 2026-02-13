# NetBeans RPC 2.0 - Implementation Summary

## Project Overview
Updated the NetBeans RPC plugin to version 2.0, bringing it up to date with Apache NetBeans 23+, adding a configuration UI panel, and replacing the native Discord RPC library with a pure Java IPC implementation.

## Objectives Achieved

### 1. NetBeans Compatibility
- **From**: NetBeans 11.0 (RELEASE110 API)
- **To**: Apache NetBeans 23+ (RELEASE230 API)
- **Status**: Fully compatible with NetBeans 28

### 2. Modern Java Support
- **From**: Java 8
- **To**: Java 17
- **Build**: JDK 23 compatible with `<proc>full</proc>` for annotation processing

### 3. Configuration UI
Implemented a full-featured configuration panel accessible from `Window` > `Discord Rich Presence`:
- Enable/disable Rich Presence on-the-fly
- Configure Discord Application ID via UI
- Toggle display options (project, file, timestamp)
- Real-time status monitoring
- Connection logs for troubleshooting

### 4. Persistent Settings
- Settings saved using Java Preferences API
- Survives IDE restarts
- Platform-independent storage
- No manual file editing required

### 5. Pure Java Discord IPC
Replaced the `kawaxte/discord-rpc` native library with a custom `DiscordIPCClient`:
- **No native DLL**: Eliminated `UnsatisfiedLinkError` and classloader issues in NetBeans modules
- **Direct IPC**: Communicates with Discord via named pipes (`\\.\pipe\discord-ipc-X`)
- **Protocol**: JSON frames over binary pipe (handshake + SET_ACTIVITY commands)
- **Auto-reconnect**: Automatically reconnects if Discord is restarted
- **Clean shutdown**: Clears presence and closes pipe via JVM shutdown hook

### 6. Enhanced Features
- **Language Support**: Added Kotlin, Groovy, PHP, TypeScript, Markdown, YAML, SQL
- **Asset Mapping**: Dynamic small image keys based on file type
- **Shutdown Hook**: Guarantees presence is cleared when NetBeans exits

## Technical Details

### Build Configuration
```xml
<version>2.0</version>
<packaging>nbm</packaging>
<!-- maven-compiler-plugin -->
<source>17</source>
<target>17</target>
<release>17</release>
<proc>full</proc> <!-- Required for JDK 23+ annotation processing -->
```

### Maven Plugins
- **nbm-maven-plugin**: 4.8 (org.apache.netbeans.utilities)
- **maven-compiler-plugin**: 3.13.0
- **maven-jar-plugin**: 3.4.2

### Components

#### DiscordIPCClient (IPC Communication)
- Pure Java Discord IPC client
- Named pipe connection (`\\.\pipe\discord-ipc-0` through `-9`)
- Binary frame protocol: opcode (4B LE) + length (4B LE) + JSON payload
- Supports: HANDSHAKE, FRAME, CLOSE opcodes
- SET_ACTIVITY command for presence updates

#### DiscordRPCPanel (UI Component)
- TopComponent registered in Window menu
- GridBagLayout for responsive UI
- Real-time status updates
- Integration with Discord RPC lifecycle

#### DiscordRPCSettings (Configuration Manager)
- Centralized settings storage
- Default values management
- Type-safe getters/setters
- Java Preferences API integration

#### Installer (Lifecycle Manager)
- `@OnShowing` startup in `run()` method
- JVM shutdown hook for reliable cleanup
- Start/stop/restart functionality

#### RCPSchedule (Presence Scheduler)
- Timer-based updates every 12 seconds
- TopComponent registry listener for immediate updates
- NetBeans API integration for file/project/language detection
- Auto-reconnect on Discord restart

### Dependencies (Zero External Libraries)
Only NetBeans Platform APIs:
- `org-openide-windows`, `org-openide-util`, `org-openide-modules`
- `org-openide-filesystems`, `org-openide-text`, `org-openide-loaders`
- `org-netbeans-modules-projectapi`, `org-openide-awt`, `org-openide-dialogs`
- `org-netbeans-api-annotations-common`

### Build
- **Status**: BUILD SUCCESS
- **Build Time**: ~4 seconds
- **Output**: NetbeansRPC-2.0.nbm

## Known Limitations

1. **Discord Assets**: Users must configure their own Discord Application assets for custom icons
2. **NetBeans Version**: Requires NetBeans 23+
3. **Java Version**: Requires Java 17+
4. **Platform**: IPC named pipes implementation is Windows-specific (`\\.\pipe\discord-ipc-X`)

## Version History

- **2.0**: Pure Java IPC, configuration UI, NetBeans 23+ support, JDK 23 build compatibility
- **1.2**: Basic Discord Rich Presence via native library

---

**Version**: 2.0
**Build Date**: 2026-02-13
**Compatible With**: Apache NetBeans 23.0+
**Requires**: Java 17+
**License**: GPL-3.0
