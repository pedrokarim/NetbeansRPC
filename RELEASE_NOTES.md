# NetBeans RPC 2.0 - Release Notes

## 🎉 What's New

NetBeans RPC 2.0 is a major update that brings the plugin up to date with the latest Apache NetBeans (version 23+) and introduces a comprehensive configuration interface.

## ✨ Key Features

### Configuration Panel (NEW!)
- **Access**: Window → Discord Rich Presence
- **Features**:
  - Enable/disable Rich Presence without restarting
  - Configure Discord Application ID via UI
  - Customize what information to display
  - View real-time status and connection logs
  - Quick reconnect functionality

### Modern Platform Support
- **Apache NetBeans 23+**: Full compatibility with the latest NetBeans versions (including v28)
- **Java 17**: Updated to modern Java for better performance and features
- **Updated APIs**: Using RELEASE230 NetBeans APIs

### Enhanced Language Support
Now detects 15+ programming languages including:
- Java, XML, HTML, JavaScript, CSS, Python, JSON, Properties (existing)
- Kotlin, Groovy, PHP, TypeScript, Markdown, YAML, SQL (NEW!)

### Persistent Settings
- Settings automatically saved and restored
- No manual configuration file editing required
- User-specific preferences

## 📋 System Requirements

- **NetBeans IDE**: Apache NetBeans 23.0 or higher
- **Java**: JDK 17 or higher
- **Maven**: 3.x (for building from source)
- **Discord**: Desktop application must be running

## 🚀 Installation

### Quick Install
1. Download `NetbeansRPC-2.0.nbm` from the releases page
2. In NetBeans: `Tools` → `Plugins` → `Downloaded`
3. Click `Add Plugins...` and select the NBM file
4. Click `Install` and follow the wizard
5. Restart NetBeans

### First-Time Configuration
1. After restart, go to `Window` → `Discord Rich Presence`
2. Verify the default Application ID or enter your own
3. Choose what information to display
4. Click `Save Settings`
5. Your Discord status should now show your activity!

## 🔧 Configuration

### Discord Application Setup
1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Copy your Application ID
4. (Optional) Upload assets in Rich Presence → Art Assets:
   - `netbeans` - NetBeans logo (512x512px)
   - `java` - Java/language icon (512x512px)

### Plugin Configuration
Open the configuration panel: `Window` → `Discord Rich Presence`

**Options:**
- ☑ Enable Discord Rich Presence - Turn the plugin on/off
- 📝 Discord Application ID - Enter your custom ID or use default
- ☑ Show project name - Display current project
- ☑ Show file name - Display current file
- ☑ Show elapsed time - Display coding duration

Click "Save Settings" to apply changes.

## 📊 What Discord Shows

When active, your Discord profile displays:
```
🖥️ NetBeans IDE
📁 MyProject
📝 Editing Main.java
💻 Programming in Java
⏱️ 01:23:45 elapsed
```

## 🔄 Upgrading from 1.x

### Breaking Changes
- **NetBeans Version**: Now requires NetBeans 23+ (was 11+)
- **Java Version**: Now requires Java 17+ (was Java 8+)

### Migration Steps
1. Uninstall NetbeansRPC 1.x via Tools → Plugins
2. Upgrade to NetBeans 23+ if needed
3. Upgrade to Java 17+ if needed
4. Install NetbeansRPC 2.0
5. Configure via the new UI panel

Your old custom Application ID will need to be re-entered in the configuration panel.

## 🐛 Troubleshooting

### Plugin Not Working?
1. **Check Discord**: Ensure Discord desktop app is running
2. **Check Settings**: Open configuration panel and verify it's enabled
3. **Check Application ID**: Verify your Application ID is correct
4. **Try Reconnecting**: Click "Reconnect to Discord" button
5. **Check Discord Settings**: User Settings → Activity Status → Display current activity

### View Status Logs
Open `Window` → `Discord Rich Presence` to see connection status and logs.

### Common Issues

**"Failed to connect to Discord"**
- Make sure Discord is running
- Wait a few seconds and try "Reconnect"
- Restart both Discord and NetBeans

**"No presence shown"**
- Check Discord privacy settings
- Verify Activity Status is enabled in Discord
- Open a project and file in NetBeans

**"Unknown asset"**
- The Discord Application doesn't have required assets
- Upload `netbeans` and `java` assets in Discord Developer Portal
- Or use the default Application ID

## 📚 Documentation

- [What's New in 2.0](doc/whats-new-2.0.md)
- [Discord RPC Integration](doc/discord-rpc-integration.md)
- [NetBeans Plugin Development](doc/netbeans-plugin-development-guide.md)
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md)

## 🤝 Contributing

Contributions are welcome! Please feel free to:
- Report bugs via GitHub Issues
- Suggest features via GitHub Issues
- Submit pull requests with improvements

## 📄 License

GNU General Public License v3.0 - See [LICENSE](LICENSE) file

## 👤 Author

**Pedro Karim**
- GitHub: [@pedrokarim](https://github.com/pedrokarim)

## 🙏 Acknowledgments

- Discord RPC Library by [kawaxte](https://github.com/kawaxte/discord-rpc)
- Apache NetBeans community
- All contributors and users

## 📝 Changelog

### Version 2.0 (2026-02-13)
- ✨ Added configuration panel UI
- ✨ Added persistent settings
- ⬆️ Updated to NetBeans 23+ API (RELEASE230)
- ⬆️ Updated to Java 17
- ⬆️ Updated all Maven plugins
- ✨ Added support for Kotlin, Groovy, PHP, TypeScript, Markdown, YAML, SQL
- 🐛 Improved error handling and status reporting
- 📚 Comprehensive documentation updates
- ✅ Security scan passed (0 alerts)
- ✅ Code review completed

### Version 1.2 (Previous)
- Basic Discord Rich Presence functionality
- NetBeans 11+ support
- Java 8+ support

---

**Download**: [NetbeansRPC-2.0.nbm](target/nbm/NetbeansRPC-2.0.nbm)  
**Size**: 2.4 MB  
**Release Date**: February 13, 2026  
**Minimum NetBeans**: 23.0  
**Minimum Java**: 17
