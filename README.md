# NetbeansRPC

A NetBeans IDE plugin that integrates Discord Rich Presence to share your coding activity with your Discord friends in real-time.

![Version](https://img.shields.io/badge/version-2.0-blue.svg)
![License](https://img.shields.io/badge/license-GPL--3.0-green.svg)
![NetBeans](https://img.shields.io/badge/NetBeans-23.0+-orange.svg)
![Java](https://img.shields.io/badge/Java-17+-red.svg)

## 📋 Description

NetbeansRPC is a NetBeans Platform module that uses the Discord Rich Presence API to display your current coding activity directly on your Discord profile. When you're working in NetBeans IDE, your Discord status will automatically update to show:

- 📁 Current project name
- 📝 File you're editing
- 💻 Programming language/file type
- ⏱️ Time spent coding
- ⚙️ **NEW:** Configuration panel for customizing your presence

## ✨ Features

- **Automatic Activity Updates**: Your Discord presence updates automatically every 12 seconds
- **Project Detection**: Displays the name of the project you're working on
- **File Tracking**: Shows which file you're currently editing
- **Language Detection**: Identifies the programming language based on file type (Java, XML, HTML, JavaScript, Python, Kotlin, Groovy, TypeScript, and more)
- **Smart Idle Detection**: Shows "Idle" status when you're not actively editing
- **Configuration Panel**: NEW! Open from Window menu to configure settings
  - Enable/disable Rich Presence
  - Configure Discord Application ID
  - Customize what information to display (project, file, timestamp)
  - View real-time status updates
- **Lightweight**: Minimal performance impact on your IDE
- **Modern**: Built for NetBeans 23+ (Apache NetBeans IDE 23 and later)

## 🔧 Prerequisites

- **NetBeans IDE**: Version 23.0 or higher (Apache NetBeans)
- **Java**: JDK 17 or higher
- **Maven**: Version 3.x
- **Discord**: Desktop application running on your computer
- **Discord Application**: You need to create a Discord application (see Configuration section)

## 📦 Installation

### Option 1: From NBM File (Recommended)

1. Build the project:
   ```bash
   mvn clean package
   ```

2. In NetBeans, go to `Tools` → `Plugins` → `Downloaded`
3. Click `Add Plugins...`
4. Navigate to `target/` and select `NetbeansRPC-2.0.nbm`
5. Click `Install` and follow the wizard
6. Restart NetBeans IDE

### Option 2: Development Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/pedrokarim/NetbeansRPC.git
   cd NetbeansRPC
   ```

2. Build and install:
   ```bash
   mvn clean install nbm:run-platform
   ```

## 🏗️ Building from Source

```bash
# Compile the project
mvn clean compile

# Package as NBM (NetBeans Module)
mvn clean package

# Run in a test NetBeans platform
mvn nbm:run-platform

# Clean build artifacts
mvn clean
```

The compiled NBM file will be available in the `target/nbm/` directory.

## ⚙️ Configuration

### Using the Configuration Panel (Recommended)

1. After installation, go to `Window` → `Discord Rich Presence` in NetBeans
2. The configuration panel allows you to:
   - Enable or disable Discord RPC
   - Change the Discord Application ID
   - Toggle project name display
   - Toggle file name display  
   - Toggle elapsed time display
   - View real-time status and activity
3. Click "Save Settings" to apply changes
4. Click "Reconnect to Discord" to restart the connection

### Discord Application Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application or use an existing one
3. Note your **Application ID** (Client ID)
4. In the "Rich Presence" section, upload assets:
   - `netbeans` - NetBeans IDE logo (for large image)
   - `java` - Java/language icon (for small image)

### Customizing Application ID (Manual)

To use your own Discord Application ID:

1. Open the Configuration Panel (Window → Discord Rich Presence)
2. Enter your Application ID in the text field
3. Click "Save Settings"
4. Click "Reconnect to Discord" to apply changes

Alternatively, edit the preferences file or use the settings stored in Java Preferences.

## 🚀 Usage

Once installed, the plugin starts automatically when NetBeans IDE launches. You'll see:

- A console message: `[PluginRPC] NetbeansRPC has loaded.`
- Your Discord status will update automatically as you work
- Open a project and start editing files to see the presence in action

The plugin will display:
- **Large Image**: NetBeans IDE logo
- **Details**: Project name (e.g., "📁 MyProject")
- **State**: Current file (e.g., "📝 Editing Main.java")
- **Small Image**: Language icon with tooltip (e.g., "Programming in Java")
- **Timestamp**: Shows how long you've been working

## 📂 Project Structure

```
NetbeansRPC/
├── pom.xml                           # Maven configuration
├── src/
│   └── main/
│       ├── java/fr/pedrokarim/netbeansrpc/
│       │   ├── Installer.java        # Module installer and lifecycle
│       │   └── RCPSchedule.java      # Discord RPC logic and updates
│       └── resources/fr/pedrokarim/netbeansrpc/
│           ├── Bundle.properties     # Module metadata
│           ├── layer.xml            # NetBeans layer registration
│           └── icon/                # Module icons
└── target/                          # Build output (generated)
```

## 🛠️ How It Works

1. **Module Installation**: `Installer.java` uses the `@OnShowing` annotation to start when NetBeans launches
2. **Timer Setup**: A timer schedules updates every 12 seconds
3. **Activity Detection**: `RCPSchedule.java` monitors the active editor window
4. **NetBeans API Integration**: Uses NetBeans Platform APIs to detect:
   - Current file being edited (`EditorCookie`, `DataObject`)
   - Current project (`FileOwnerQuery`, `ProjectUtils`)
   - File type and MIME type (`FileObject.getMIMEType()`)
5. **Discord Communication**: Updates are sent to Discord via the `discord-rpc` library
6. **Cleanup**: Properly shuts down Discord RPC when NetBeans closes

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Pedro Karim**
- GitHub: [@pedrokarim](https://github.com/pedrokarim)

## 🙏 Acknowledgments

- [Discord RPC Library](https://github.com/kawaxte/discord-rpc) by kawaxte
- NetBeans Platform documentation and community
- All contributors to this project

## 📚 Additional Documentation

For more detailed information about NetBeans plugin development, see the [documentation folder](doc/):
- [NetBeans Plugin Development Guide](doc/netbeans-plugin-development-guide.md)
- [Project Structure Details](doc/project-structure.md)
- [Discord RPC Integration](doc/discord-rpc-integration.md)
