# NetBeans RPC 2.0 - What's New

This document describes the new features and improvements in NetBeans RPC version 2.0.

## Major Updates

### 1. NetBeans 23+ Support (Apache NetBeans)

- **Updated to RELEASE230 API**: Full compatibility with Apache NetBeans 23.0 and later versions
- **Java 17 Required**: The plugin now requires Java 17 or higher
- **Modern Maven Plugins**: Updated to the latest stable versions for better compatibility

### 2. Configuration Panel (NEW!)

A new graphical interface has been added to configure and monitor Discord Rich Presence.

#### Accessing the Configuration Panel

- Go to `Window` → `Discord Rich Presence` in the NetBeans menu
- The panel will open in the output area (bottom of the IDE)

#### Features of the Configuration Panel

1. **Enable/Disable Toggle**
   - Quickly enable or disable Discord RPC without restarting NetBeans
   - Changes take effect immediately

2. **Application ID Configuration**
   - Enter your Discord Application ID directly in the UI
   - No need to edit source code or rebuild the plugin
   - Default ID is provided for quick start

3. **Display Customization**
   - ☑ Show project name - Display the name of your current project
   - ☑ Show file name - Display the file you're currently editing
   - ☑ Show elapsed time - Display how long you've been coding

4. **Real-Time Status**
   - View connection status to Discord
   - See current project, file, and language being displayed
   - Monitor activity updates in real-time
   - View connection logs and events

5. **Quick Actions**
   - **Save Settings**: Persist your configuration across NetBeans restarts
   - **Reconnect to Discord**: Restart the connection without restarting NetBeans

### 3. Persistent Settings

Settings are now saved using Java Preferences API, which means:

- Your configuration persists across NetBeans restarts
- Settings are user-specific
- No manual file editing required
- Easy to reset to defaults if needed

### 4. Enhanced Language Support

Added support for more programming languages:

- Kotlin (.kt, .kts)
- Groovy (.groovy)
- PHP (.php)
- TypeScript (.ts, .tsx)
- Markdown (.md)
- YAML (.yml, .yaml)
- SQL (.sql)

And all the previously supported languages:
- Java, XML, HTML, JavaScript, CSS, Python, JSON, Properties

### 5. Improved Code Architecture

**DiscordRPCSettings Class**
- Centralized configuration management
- Type-safe settings access
- Easy to extend with new settings

**Enhanced Installer**
- Support for enabling/disabling at startup
- Restart functionality without IDE restart
- Better lifecycle management

**Updated RCPSchedule**
- Settings-aware presence updates
- UI integration for status display
- More robust error handling
- Better event handlers for Discord connection states

## Migration from 1.x to 2.0

### Compatibility

- **NetBeans Version**: You need Apache NetBeans 23.0 or later (previously 11.0+)
- **Java Version**: You need Java 17 or higher (previously Java 8+)

### Settings Migration

If you were using version 1.x:

1. Your old hardcoded Application ID will be replaced by the default one
2. To keep your custom Application ID:
   - Open the Configuration Panel (`Window` → `Discord Rich Presence`)
   - Enter your Application ID
   - Click "Save Settings"

### Features Removed

- None! All previous features are still available
- Settings are now configurable via UI instead of code editing

## How to Use the New Features

### Initial Setup

1. **Install the Plugin**
   ```bash
   mvn clean package
   ```
   Then install `target/nbm/NetbeansRPC-2.0.nbm` via NetBeans Plugin Manager

2. **Configure Discord Application** (if you want custom assets)
   - Go to [Discord Developer Portal](https://discord.com/developers/applications)
   - Create or select an application
   - Note your Application ID
   - Upload assets in Rich Presence → Art Assets section

3. **Configure the Plugin**
   - Open `Window` → `Discord Rich Presence`
   - Enter your Application ID (or use the default)
   - Customize what information to display
   - Click "Save Settings"

### Daily Use

Once configured, the plugin works automatically:

1. **Start NetBeans** - Discord RPC starts automatically if enabled
2. **Open a Project** - Your Discord status shows the project name
3. **Edit Files** - Your status updates to show the current file
4. **Check Status** - Open the Configuration Panel to see current status

### Troubleshooting

If Discord RPC is not working:

1. **Open the Configuration Panel**
   - Check if it's enabled (checkbox at top)
   - View the status log at the bottom

2. **Check Discord**
   - Ensure Discord desktop app is running
   - Check Discord settings: User Settings → Activity Status → Display current activity

3. **Try Reconnecting**
   - Click "Reconnect to Discord" button in the Configuration Panel
   - Check the status log for connection messages

4. **Verify Application ID**
   - Ensure your Application ID is correct
   - The default ID should work for basic functionality

## Technical Details

### Architecture Changes

```
User Interaction
    ↓
DiscordRPCPanel (UI)
    ↓
DiscordRPCSettings (Config Storage)
    ↓
Installer (Lifecycle Management)
    ↓
RCPSchedule (Discord Communication)
    ↓
Discord API
```

### Configuration Storage

Settings are stored in:
- **Windows**: Registry under `HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fr\pedrokarim\netbeansrpc`
- **Linux**: `~/.java/.userPrefs/fr/pedrokarim/netbeansrpc`
- **macOS**: `~/Library/Preferences/com.apple.java.util.prefs.plist`

### API Usage

The plugin uses:
- **NetBeans Platform APIs** (RELEASE230)
  - `org.openide.*` - Core platform APIs
  - `org.netbeans.api.project.*` - Project detection
  - `TopComponent` - UI panel integration
  
- **Discord RPC Library** (kawaxte/discord-rpc)
  - Discord IPC communication
  - Rich Presence updates

## Future Enhancements

Potential features for future versions:

- [ ] Support for custom presence templates
- [ ] Multiple Discord Application profiles
- [ ] Statistics tracking (time spent per project/language)
- [ ] More detailed file information (lines of code, size)
- [ ] Support for custom icons per language
- [ ] Integration with Git branch information
- [ ] Dark/light theme for the configuration panel

## Credits

- Original plugin by Pedro Karim
- Updated to NetBeans 23+ and UI enhancements in version 2.0
- Discord RPC library by [kawaxte](https://github.com/kawaxte/discord-rpc)
- NetBeans Platform by Apache NetBeans community

## Links

- [GitHub Repository](https://github.com/pedrokarim/NetbeansRPC)
- [Discord Developer Portal](https://discord.com/developers/applications)
- [Apache NetBeans](https://netbeans.apache.org/)
- [VS Code Rich Presence (inspiration)](https://marketplace.visualstudio.com/items?itemName=LeonardSSH.vscord)
