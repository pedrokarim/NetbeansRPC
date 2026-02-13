# Intégration Discord Rich Presence dans NetbeansRPC

Ce document explique en détail comment Discord Rich Presence est intégré dans le plugin NetbeansRPC via le protocole IPC natif de Discord.

## Table des Matières

1. [Qu'est-ce que Discord Rich Presence ?](#quest-ce-que-discord-rich-presence)
2. [Architecture de l'Intégration](#architecture-de-lintégration)
3. [Protocole Discord IPC](#protocole-discord-ipc)
4. [Implémentation : DiscordIPCClient](#implémentation--discordipcclient)
5. [Mise à Jour de la Présence](#mise-à-jour-de-la-présence)
6. [Gestion du Lifecycle](#gestion-du-lifecycle)
7. [Configuration Discord](#configuration-discord)
8. [Personnalisation](#personnalisation)
9. [Troubleshooting](#troubleshooting)

## Qu'est-ce que Discord Rich Presence ?

### Définition

Discord Rich Presence (RPC) est une fonctionnalité qui permet aux applications de partager des informations détaillées sur l'activité d'un utilisateur directement dans son profil Discord.

### Ce que Discord Affiche

Quand NetbeansRPC est actif, votre profil Discord montre :

```
┌─────────────────────────────────────┐
│  NetBeans IDE                       │
│                                     │
│  MyAwesomeProject                   │  ← Details
│  Editing Main.java                  │  ← State
│  Programming in Java                │  ← Small image text
│  01:23:45 elapsed                   │  ← Timestamp
└─────────────────────────────────────┘
```

### Éléments de la Présence

- **Large Image** : Logo de l'application (asset key: `first`)
- **Small Image** : Icône contextuelle (asset key: `java`, `maven`, etc.)
- **Details** : Première ligne de texte (nom du projet)
- **State** : Deuxième ligne de texte (fichier en cours)
- **Timestamps** : Temps écoulé

## Architecture de l'Intégration

### Vue d'Ensemble

```
┌──────────────────┐
│  NetBeans IDE    │
│                  │
│  ┌────────────┐  │
│  │ RCPSchedule│  │
│  └─────┬──────┘  │
│        │         │
└────────┼─────────┘
         │ Pure Java
         ▼
┌──────────────────┐
│ DiscordIPCClient │  ← Client IPC pur Java
│ (Named Pipe)     │
└────────┬─────────┘
         │ \\.\pipe\discord-ipc-X
         ▼
┌──────────────────┐
│  Discord Client  │  ← Application Discord
│  (Running)       │
└──────────────────┘
```

### Composants

1. **RCPSchedule** : Classe gérant la logique de présence
2. **DiscordIPCClient** : Client IPC pur Java
3. **Named Pipe** : Communication inter-processus Windows
4. **Discord Client** : Application Discord locale

### Pourquoi un Client IPC Pur Java ?

La version précédente utilisait la bibliothèque `kawaxte/discord-rpc` qui dépendait de JNA et d'une DLL native. Cela causait des `UnsatisfiedLinkError` dans l'environnement modulaire de NetBeans car la bibliothèque utilisait `ClassLoader.getSystemResource()` pour extraire la DLL, qui ne fonctionne pas avec les classloaders isolés des modules NetBeans.

La solution : implémenter le protocole Discord IPC directement en Java via les named pipes Windows, éliminant toute dépendance native.

## Protocole Discord IPC

### Connexion

Discord écoute sur des named pipes nommés `\\.\pipe\discord-ipc-0` à `\\.\pipe\discord-ipc-9`. Le client essaie chaque pipe jusqu'à trouver celui qui répond.

```java
pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
```

### Format des Frames

Chaque message est encapsulé dans une frame binaire :

```
┌──────────┬──────────┬─────────────────────┐
│ Opcode   │ Length   │ JSON Payload        │
│ (4B LE)  │ (4B LE)  │ (variable)          │
└──────────┴──────────┴─────────────────────┘
```

- **Opcode** : uint32 little-endian
  - `0` = HANDSHAKE
  - `1` = FRAME
  - `2` = CLOSE
- **Length** : uint32 little-endian (taille du payload JSON)
- **Payload** : Chaîne JSON encodée en UTF-8

### Handshake

Première étape après connexion au pipe :

```json
{"v": 1, "client_id": "621768079386345477"}
```

Discord répond avec un FRAME contenant `"evt": "READY"` et les informations utilisateur.

### SET_ACTIVITY (Mise à jour de la présence)

```json
{
  "cmd": "SET_ACTIVITY",
  "args": {
    "pid": 12345,
    "activity": {
      "details": "MyProject",
      "state": "Editing Main.java",
      "timestamps": {"start": 1234567890},
      "assets": {
        "large_image": "first",
        "large_text": "NetBeans IDE",
        "small_image": "java",
        "small_text": "Programming in Java"
      }
    }
  },
  "nonce": "unique-uuid"
}
```

### Clear Activity

Pour effacer la présence, envoyer SET_ACTIVITY sans `activity` :

```json
{
  "cmd": "SET_ACTIVITY",
  "args": {"pid": 12345},
  "nonce": "unique-uuid"
}
```

## Implémentation : DiscordIPCClient

### Connexion

```java
public boolean connect() {
    for (int i = 0; i < 10; i++) {
        try {
            pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
            sendHandshake();
            String response = readResponse();
            if (response != null && response.contains("READY")) {
                connected = true;
                return true;
            }
        } catch (IOException e) {
            closePipe(); // Try next pipe
        }
    }
    return false;
}
```

### Envoi de Frames

```java
private void sendFrame(int opcode, String payload) throws IOException {
    byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
    ByteBuffer buffer = ByteBuffer.allocate(8 + payloadBytes.length);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(opcode);
    buffer.putInt(payloadBytes.length);
    buffer.put(payloadBytes);
    pipe.write(buffer.array());
}
```

### Lecture de Réponses

```java
private String readResponse() {
    byte[] header = new byte[8];
    pipe.readFully(header);
    ByteBuffer headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
    int op = headerBuf.getInt();
    int length = headerBuf.getInt();

    byte[] data = new byte[length];
    pipe.readFully(data);
    return new String(data, StandardCharsets.UTF_8);
}
```

## Mise à Jour de la Présence

### Détection des Informations NetBeans

#### Fichier Actif

```java
private FileObject getCurrentFileObject() {
    TopComponent activated = TopComponent.getRegistry().getActivated();
    if (activated != null) {
        EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
        if (ec != null && ec.getDocument() != null) {
            Document doc = ec.getDocument();
            Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
            if (streamDesc instanceof DataObject) {
                return ((DataObject) streamDesc).getPrimaryFile();
            }
        }
    }
    return null;
}
```

#### Projet Actif

```java
private String detectCurrentProject() {
    FileObject file = getCurrentFileObject();
    if (file != null) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
    }
    return null;
}
```

#### Type de Fichier

```java
private String detectFileType() {
    FileObject file = getCurrentFileObject();
    if (file != null) {
        String mimeType = file.getMIMEType();
        if (mimeType.contains("java")) return "Java";
        if (mimeType.contains("xml")) return "XML";
        // ... etc
    }
    return null;
}
```

### Fréquence de Mise à Jour

#### Mises à Jour Périodiques

```java
timer.scheduleAtFixedRate(rcpSchedule, new Date(), 12000l);
```

- **Fréquence** : Toutes les 12 secondes
- **Raison** : Détecter les changements qui ne déclenchent pas d'événements

#### Mises à Jour Immédiates

```java
registryListener = (evt) -> {
    if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
        updateRCP(false);
    }
};
TopComponent.getRegistry().addPropertyChangeListener(registryListener);
```

- **Événement** : Changement de fenêtre active
- **Réaction** : Mise à jour immédiate de la présence

### Mapping des Assets

Les asset keys Discord sont mappés depuis le type de fichier détecté :

```java
private String getAssetKeyForFileType(String fileType) {
    switch (fileType) {
        case "Java":
        case "Properties":
        case "Kotlin":
        case "Groovy":
            return "java";
        case "XML":
            return "maven";
        default:
            return "java";
    }
}
```

Les assets disponibles sur le Discord Developer Portal sont : `first` (NetBeans logo), `java`, `maven`.

## Gestion du Lifecycle

### Démarrage

```
NetBeans startup
       ↓
@OnShowing → Installer.run()
       ↓
RCPSchedule created
       ↓
DiscordIPCClient.connect()
       ↓
Handshake → READY
       ↓
First updateRCP(true)
```

### Exécution

```
Every 12 seconds:
  run() → updateRCP(false)

On window change:
  propertyChange() → updateRCP(false)

If disconnected:
  run() → reconnect attempt
```

### Fermeture

```
NetBeans exit
       ↓
JVM Shutdown Hook OR Installer.close()
       ↓
stopRPC()
       ↓
DiscordIPCClient.close()
  → clearPresence()
  → sendFrame(OP_CLOSE, "{}")
  → pipe.close()
```

## Configuration Discord

### Créer une Application Discord

1. Aller sur [Discord Developer Portal](https://discord.com/developers/applications)
2. Cliquer "New Application"
3. Donner un nom (ex: "Netbeans")
4. Copier l'**Application ID** (Client ID)

### Configurer les Assets

Dans votre application Discord, Rich Presence > Art Assets :

1. **`first`** (Large Image) : Logo NetBeans - 512x512px minimum
2. **`java`** (Small Image) : Icône Java - 512x512px minimum
3. **`maven`** (Small Image) : Icône Maven - 512x512px minimum

Les noms doivent correspondre exactement aux clés utilisées dans le code.

## Personnalisation

### Ajouter de Nouveaux Assets

1. Uploader l'image dans Discord Developer Portal (Rich Presence > Art Assets)
2. Ajouter le mapping dans `RCPSchedule.getAssetKeyForFileType()` :

```java
case "Python":
    return "python";  // Nécessite un asset "python" sur Discord
```

### Modifier le Format d'Affichage

Dans `RCPSchedule.updateRCP()` :

```java
// Modifier les details/state
details = "Project: " + projectName;
state = "File: " + fileName;
```

## Troubleshooting

### Discord ne montre pas la présence

1. **Discord est-il ouvert ?** La présence ne fonctionne que si Discord est lancé
2. **Application ID correct ?** Vérifier dans le panneau de configuration
3. **Assets configurés ?** Les clés `first` et `java` doivent exister dans Discord
4. **Paramètres Discord** : Paramètres > Activité > "Afficher le jeu en cours"

### Erreur "Could not connect to Discord"

- Discord n'est pas lancé
- Discord est en train de démarrer (attendre quelques secondes)
- Le plugin essaie automatiquement de se reconnecter toutes les 12 secondes

### La présence ne disparaît pas à la fermeture

Discord a un délai naturel de quelques secondes (~10-15s) après la déconnexion IPC. C'est un comportement normal côté Discord.

## Limites et Contraintes

### Limites Discord RPC

- **Taille des champs** : Details/State/Image Text : 128 caractères max
- **Fréquence** : Recommandé 1 update/seconde max (NetbeansRPC : 1/12 secondes)
- **Assets** : 300 assets max par application, 512x512px minimum

### Limites Plateforme

- **Windows** : Named pipes (`\\.\pipe\discord-ipc-X`) - Supporté
- **Linux/macOS** : Unix sockets (`/tmp/discord-ipc-X`) - Non implémenté actuellement

## Ressources

### Documentation Officielle

- [Discord Developer Portal](https://discord.com/developers/docs/topics/rpc)
- [Discord RPC Protocol](https://discord.com/developers/docs/topics/rpc#rpc)

### Communauté

- [Discord Developers](https://discord.gg/discord-developers)
