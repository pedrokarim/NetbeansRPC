# Structure Détaillée du Projet NetbeansRPC

Ce document explique en détail l'organisation et la structure du projet NetbeansRPC.

## Vue d'Ensemble

NetbeansRPC est un module NetBeans qui intègre Discord Rich Presence pour afficher l'activité de développement en temps réel sur Discord, en utilisant le protocole IPC natif de Discord (named pipes).

## Arborescence Complète

```
NetbeansRPC/
├── pom.xml                                          # Configuration Maven
├── README.md                                        # Documentation principale
├── RELEASE_NOTES.md                                 # Notes de version
├── IMPLEMENTATION_SUMMARY.md                        # Résumé d'implémentation
├── LICENSE                                          # Licence GPL-3.0
├── .gitignore                                       # Fichiers ignorés par Git
│
├── doc/                                             # Documentation
│   ├── netbeans-plugin-development-guide.md         # Guide de développement plugins
│   ├── project-structure.md                         # Ce fichier
│   ├── discord-rpc-integration.md                   # Intégration Discord IPC
│   └── whats-new-2.0.md                             # Changelog v2.0
│
├── src/                                             # Code source
│   └── main/
│       ├── java/                                    # Code Java
│       │   └── fr/pedrokarim/netbeansrpc/
│       │       ├── Installer.java                   # Point d'entrée du module
│       │       ├── RCPSchedule.java                 # Logique de présence Discord
│       │       ├── DiscordIPCClient.java            # Client IPC pur Java
│       │       ├── DiscordRPCPanel.java             # Panneau de configuration UI
│       │       └── DiscordRPCSettings.java          # Gestion des paramètres
│       │
│       └── resources/                               # Ressources
│           └── fr/pedrokarim/netbeansrpc/
│               ├── Bundle.properties                # Métadonnées du module
│               └── layer.xml                        # Configuration NetBeans
│
└── target/                                          # Dossier de build (généré)
    ├── classes/                                     # Classes compilées
    ├── NetbeansRPC-2.0.jar                          # JAR du module
    └── nbm/NetbeansRPC-2.0.nbm                     # Module packagé
```

## Description des Fichiers Principaux

### 1. pom.xml

**Rôle** : Configuration Maven du projet

**Contenu clé** :
- **packaging** : `nbm` (NetBeans Module)
- **version** : 2.0
- **plugins** :
  - `nbm-maven-plugin` (v4.8) : Création du module NetBeans
  - `maven-compiler-plugin` (v3.13.0) : Compilation Java 17 avec `<proc>full</proc>`
  - `maven-jar-plugin` (v3.4.2) : Création du JAR
- **dependencies** : Uniquement les APIs NetBeans (RELEASE230) - aucune bibliothèque externe

### 2. Installer.java

**Rôle** : Point d'entrée et gestionnaire du lifecycle du module

**Fonctionnalités** :
- Démarre le RPC dans `run()` quand l'UI est prête (`@OnShowing`)
- Initialise le Timer pour les mises à jour périodiques
- Enregistre un JVM shutdown hook pour garantir le nettoyage
- Gère start/stop/restart du RPC

**Lifecycle** :
1. `@OnShowing` → `run()` appelé quand l'UI est prête
2. `startRPC()` → Timer + RCPSchedule + shutdown hook
3. `close()` ou shutdown hook → `stopRPC()`

### 3. RCPSchedule.java

**Rôle** : Logique de présence Discord et détection du contexte NetBeans

**Responsabilités** :
- Initialisation du `DiscordIPCClient`
- Détection du fichier, projet et langage actifs via les APIs NetBeans
- Mise à jour de la présence Discord toutes les 12 secondes
- Écoute des changements de fenêtre active pour mise à jour immédiate
- Reconnexion automatique si Discord est redémarré
- Mapping des types de fichiers vers les asset keys Discord

### 4. DiscordIPCClient.java

**Rôle** : Client IPC pur Java pour communiquer avec Discord

**Fonctionnalités** :
- Connexion via named pipes Windows (`\\.\pipe\discord-ipc-0` à `-9`)
- Protocole binaire : opcode (4B) + length (4B) + JSON payload
- Handshake avec Application ID
- Envoi de commandes SET_ACTIVITY
- Clear presence et fermeture propre

### 5. DiscordRPCPanel.java

**Rôle** : Interface utilisateur de configuration

**Fonctionnalités** :
- TopComponent accessible via `Window` > `Discord Rich Presence`
- Enable/disable du RPC
- Configuration de l'Application ID
- Toggles pour projet, fichier, timestamp
- Affichage du statut en temps réel
- Boutons Save et Reconnect

### 6. DiscordRPCSettings.java

**Rôle** : Gestion centralisée des paramètres

**Fonctionnalités** :
- Stockage via Java Preferences API
- Getters/setters typés pour chaque paramètre
- Valeurs par défaut (Application ID: `621768079386345477`)
- Persistance automatique entre redémarrages

### 7. Bundle.properties

**Rôle** : Métadonnées et localisation du module

```properties
OpenIDE-Module-Name=NetbeansRPC
OpenIDE-Module-Short-Description=Discord Rich Presence for NetBeans IDE
OpenIDE-Module-Long-Description=Ce plugin exploite la RichPresence API de Discord...
OpenIDE-Module-Display-Category=Tools
```

### 8. layer.xml

**Rôle** : Configuration de la layer NetBeans (vide car les annotations génèrent le contenu)

## Architecture du Code

### Diagramme de Classes

```
┌─────────────────────────┐
│      Installer          │
│   (ModuleInstall)       │
│   @OnShowing            │
├─────────────────────────┤
│ - timer: Timer          │
│ - rcpSchedule: RCPSch.. │
│ - shutdownHook: Thread  │
├─────────────────────────┤
│ + run()                 │
│ + close()               │
│ + startRPC()            │
│ + stopRPC()             │
│ + restartRPC()          │
└───────────┬─────────────┘
            │ crée et gère
            ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│    RCPSchedule          │────▶│  DiscordIPCClient       │
│    (TimerTask)          │     │  (Closeable)            │
├─────────────────────────┤     ├─────────────────────────┤
│ - ipcClient: DiscordIPC │     │ - pipe: RandomAccessFile│
│ - registryListener      │     │ - applicationId: String │
│ - startTimestamp: long   │     │ - connected: boolean    │
├─────────────────────────┤     ├─────────────────────────┤
│ + run()                 │     │ + connect()             │
│ + initializeDiscordRPC()│     │ + updatePresence()      │
│ + updateRCP()           │     │ + clearPresence()       │
│ + detectCurrentFile()   │     │ + close()               │
│ + detectCurrentProject()│     │ - sendFrame()           │
│ + detectFileType()      │     │ - readResponse()        │
│ + shutdown()            │     └─────────────────────────┘
└─────────────────────────┘
            │
            │ met à jour l'UI
            ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│  DiscordRPCPanel        │────▶│  DiscordRPCSettings     │
│  (TopComponent)         │     │  (Preferences)          │
├─────────────────────────┤     ├─────────────────────────┤
│ - appIdField            │     │ + isEnabled()           │
│ - enabledCheckBox       │     │ + getApplicationId()    │
│ - statusArea            │     │ + isShowProject()       │
├─────────────────────────┤     │ + isShowFile()          │
│ + updateStatus()        │     │ + isShowTimestamp()     │
│ + updatePresenceInfo()  │     │ + saveSettings()        │
│ + getInstance()         │     └─────────────────────────┘
└─────────────────────────┘
```

### Flux d'Exécution

```
1. NetBeans démarre
   ↓
2. @OnShowing → Installer.run()
   ↓
3. startRPC() → Timer + RCPSchedule + Shutdown Hook
   ↓
4. RCPSchedule() → initializeDiscordRPC()
   → DiscordIPCClient.connect() via named pipe
   → Handshake → READY
   → setupListener() sur TopComponent.Registry
   ↓
5. updateRCP(true) → Première présence avec timestamp
   ↓
6. Timer : toutes les 12 secondes
   → run() → updateRCP(false)
   → Si déconnecté : reconnexion automatique
   ↓
7. Listener : changement de fenêtre
   → propertyChange() → updateRCP(false)
   ↓
8. NetBeans se ferme
   ↓
9. Shutdown Hook ou Installer.close()
   → stopRPC() → Timer.cancel()
   → RCPSchedule.shutdown()
   → DiscordIPCClient.close() → clearPresence + CLOSE frame
```

## Dépendances

### APIs NetBeans (RELEASE230)

| API | Usage |
|-----|-------|
| `org-openide-windows` | TopComponent, Window System |
| `org-openide-modules` | ModuleInstall, @OnShowing |
| `org-openide-util` | Exceptions, Lookup |
| `org-openide-util-ui` | UI utilities |
| `org-openide-util-lookup` | Lookup system |
| `org-openide-filesystems` | FileObject |
| `org-openide-text` | EditorCookie |
| `org-openide-loaders` | DataObject |
| `org-openide-nodes` | Node system |
| `org-openide-awt` | ActionID, ActionReference |
| `org-openide-dialogs` | Dialogs |
| `org-netbeans-modules-projectapi` | Project, FileOwnerQuery |
| `org-netbeans-modules-project-ant` | Ant-based projects |
| `org-netbeans-api-annotations-common` | Annotations |

### Aucune Bibliothèque Externe

La version 2.0 n'a aucune dépendance externe. La communication Discord est implémentée en pur Java via `RandomAccessFile` sur les named pipes Windows.

## Build et Packaging

### Commandes Maven

```bash
# Compilation
mvn clean compile

# Création du NBM
mvn clean install
# Génère : target/nbm/NetbeansRPC-2.0.nbm

# Nettoyage
mvn clean
```

### Note JDK 23+

Le `<proc>full</proc>` dans la configuration du maven-compiler-plugin est requis pour que les processeurs d'annotations NetBeans (`@Messages`, `@TopComponent.Registration`, etc.) fonctionnent avec JDK 23+, qui a désactivé la découverte implicite des processeurs d'annotations.
