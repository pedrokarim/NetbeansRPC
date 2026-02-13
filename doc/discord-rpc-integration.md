# Intégration Discord Rich Presence dans NetbeansRPC

Ce document explique en détail comment Discord Rich Presence est intégré dans le plugin NetbeansRPC et comment fonctionne la communication avec Discord.

## Table des Matières

1. [Qu'est-ce que Discord Rich Presence ?](#quest-ce-que-discord-rich-presence)
2. [Architecture de l'Intégration](#architecture-de-lintégration)
3. [Bibliothèque Discord RPC](#bibliothèque-discord-rpc)
4. [Initialisation](#initialisation)
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
│  🖥️  NetBeans IDE                   │
│                                     │
│  📁 MyAwesomeProject                │  ← Details
│  📝 Editing Main.java               │  ← State
│  💻 Programming in Java             │  ← Small image text
│  ⏱️  01:23:45 elapsed               │  ← Timestamp
└─────────────────────────────────────┘
```

### Éléments de la Présence

- **Large Image** : Logo de l'application (NetBeans)
- **Small Image** : Icône contextuelle (langage de programmation)
- **Details** : Première ligne de texte (nom du projet)
- **State** : Deuxième ligne de texte (fichier en cours)
- **Timestamps** : Temps écoulé ou restant
- **Party** : Informations de groupe (non utilisé ici)
- **Buttons** : Boutons cliquables (non utilisés ici)

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
         │ JNA
         ▼
┌──────────────────┐
│  discord-rpc     │  ← Bibliothèque Java
│  (kawaxte)       │
└────────┬─────────┘
         │ IPC/Named Pipes
         ▼
┌──────────────────┐
│  Discord Client  │  ← Application Discord
│  (Running)       │
└──────────────────┘
```

### Composants

1. **RCPSchedule** : Classe gérant la logique RPC
2. **discord-rpc** : Bibliothèque Java wrapper
3. **JNA** : Java Native Access pour les appels système
4. **Discord Client** : Application Discord locale
5. **IPC/Named Pipes** : Communication inter-processus

## Bibliothèque Discord RPC

### Dépendance Maven

```xml
<dependency>
    <groupId>io.github.kawaxte</groupId>
    <artifactId>discord-rpc</artifactId>
    <version>20230409</version>
</dependency>
```

### Origine

- **Repository** : [kawaxte/discord-rpc](https://github.com/kawaxte/discord-rpc)
- **Type** : Fork amélioré de discord-rpc-java
- **Langage** : Java avec bindings natifs via JNA

### Classes Principales

#### DiscordRPC

API principale pour interagir avec Discord :

```java
// Initialisation
DiscordRPC.initialise(String applicationId, 
                      DiscordEventHandlers handlers, 
                      boolean autoRegister, 
                      String steamId)

// Mise à jour
DiscordRPC.updatePresence(DiscordRichPresence presence)

// Callbacks (à appeler régulièrement)
DiscordRPC.runCallbacks()

// Fermeture
DiscordRPC.shutdown()
```

#### DiscordRichPresence

Modèle de données pour la présence :

```java
DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();

builder.setDetails(String details)              // Ligne 1
       .setState(String state)                  // Ligne 2
       .setStartTimestamp(long timestamp)       // Timestamp début
       .setEndTimestamp(long timestamp)         // Timestamp fin
       .setLargeImageKey(String key)            // Grande image
       .setLargeImageText(String text)          // Tooltip grande image
       .setSmallImageKey(String key)            // Petite image
       .setSmallImageText(String text)          // Tooltip petite image
       .setPartyId(String id)                   // ID de groupe
       .setPartySize(int size)                  // Taille groupe
       .setPartyMax(int max)                    // Max groupe
       .setMatchSecret(String secret)           // Secret de match
       .setJoinSecret(String secret)            // Secret de join
       .setSpectateSecret(String secret)        // Secret spectateur
       .setInstance(byte instance);             // Instance

DiscordRichPresence presence = builder.build();
```

#### DiscordEventHandlers

Gestionnaires d'événements Discord :

```java
DiscordEventHandlers handlers = new DiscordEventHandlers();

handlers.ready = (user) -> {
    // Appelé quand Discord est prêt
    System.out.println("Connected as: " + user.username);
};

handlers.disconnected = (errorCode, message) -> {
    // Appelé en cas de déconnexion
    System.err.println("Disconnected: " + message);
};

handlers.errored = (errorCode, message) -> {
    // Appelé en cas d'erreur
    System.err.println("Error: " + message);
};

handlers.joinGame = (joinSecret) -> {
    // Appelé quand quelqu'un veut rejoindre
};

handlers.spectateGame = (spectateSecret) -> {
    // Appelé quand quelqu'un veut observer
};

handlers.joinRequest = (user) -> {
    // Appelé quand quelqu'un demande à rejoindre
};
```

## Initialisation

### Code d'Initialisation dans NetbeansRPC

```java
public void initializeDiscordRPC() {
    try {
        // 1. Créer les handlers
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Discord RPC Ready!");
        
        // 2. Initialiser la connexion
        DiscordRPC.initialise(applicationId, handlers, true, "");
        
        // 3. Démarrer le thread de callbacks
        callbackThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                DiscordRPC.runCallbacks();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "RPC-Callback-Handler");
        callbackThread.start();
        
        System.out.println("Discord RPC initialized!");
        
        // 4. Première mise à jour
        updateRCP(true);
        
    } catch (Exception e) {
        System.err.println("Failed to initialize Discord RPC: " + e.getMessage());
        Exceptions.printStackTrace(e);
    }
}
```

### Étapes Détaillées

#### 1. Création des Event Handlers

```java
DiscordEventHandlers handlers = new DiscordEventHandlers();
handlers.ready = (user) -> System.out.println("Discord RPC Ready!");
```

- Définit ce qui se passe quand Discord est prêt
- `user` contient le nom d'utilisateur, discriminator, etc.

#### 2. Initialisation de la Connexion

```java
DiscordRPC.initialise(applicationId, handlers, true, "");
```

**Paramètres** :
- `applicationId` : ID de l'application Discord (voir Configuration)
- `handlers` : Gestionnaires d'événements
- `autoRegister` : `true` pour enregistrer automatiquement l'application
- `steamId` : ID Steam (vide si non utilisé)

**Ce qui se passe** :
1. Connexion au client Discord local via IPC
2. Authentification avec l'application ID
3. Enregistrement du protocole discord-* (si autoRegister)
4. Appel du handler `ready` si succès

#### 3. Thread de Callbacks

```java
callbackThread = new Thread(() -> {
    while (running && !Thread.currentThread().isInterrupted()) {
        DiscordRPC.runCallbacks();
        Thread.sleep(500);
    }
}, "RPC-Callback-Handler");
callbackThread.start();
```

**Pourquoi ?** Discord RPC nécessite que `runCallbacks()` soit appelé régulièrement pour :
- Traiter les événements entrants
- Maintenir la connexion active
- Recevoir les réponses de Discord

**Fréquence** : Toutes les 500ms (recommandé entre 100ms et 1000ms)

#### 4. Première Mise à Jour

```java
updateRCP(true);
```

Envoie la présence initiale avec un timestamp de début.

## Mise à Jour de la Présence

### Code de Mise à Jour

```java
private void updateRCP(Boolean timestamp) {
    try {
        // 1. Créer le builder
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
        
        // 2. Timestamp (optionnel)
        if (timestamp != null && timestamp) {
            builder.setStartTimestamp(System.currentTimeMillis() / 1000);
        }
        
        // 3. Détecter les informations NetBeans
        String fileName = detectCurrentFile();
        String projectName = detectCurrentProject();
        String fileType = detectFileType();
        
        // 4. Configurer Details (ligne 1)
        if (projectName != null) {
            builder.setDetails("📁 " + projectName);
        } else {
            builder.setDetails("Working in NetBeans IDE");
        }
        
        // 5. Configurer State (ligne 2)
        if (fileName != null) {
            builder.setState("📝 Editing " + fileName);
        } else {
            builder.setState("Idle");
        }
        
        // 6. Images
        builder.setLargeImageKey("netbeans");
        builder.setLargeImageText("NetBeans IDE");
        
        if (fileType != null) {
            builder.setSmallImageKey("java");
            builder.setSmallImageText("Programming in " + fileType);
        } else {
            builder.setSmallImageKey("java");
            builder.setSmallImageText("NetBeans");
        }
        
        // 7. Construire et envoyer
        presence = builder.build();
        DiscordRPC.updatePresence(presence);
        
    } catch (Exception e) {
        System.err.println("Failed to update Discord presence: " + e.getMessage());
    }
}
```

### Détection des Informations NetBeans

#### Fichier Actif

```java
private String detectCurrentFile() {
    FileObject file = getCurrentFileObject();
    return file != null ? file.getNameExt() : null;
}

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

**Processus** :
1. Obtenir le `TopComponent` actif (fenêtre)
2. Chercher un `EditorCookie` (indique un éditeur)
3. Obtenir le `Document` ouvert
4. Récupérer le `DataObject` associé
5. Extraire le `FileObject`

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

**Processus** :
1. Obtenir le fichier actif
2. Trouver le projet propriétaire via `FileOwnerQuery`
3. Extraire le nom d'affichage du projet

#### Type de Fichier

```java
private String detectFileType() {
    FileObject file = getCurrentFileObject();
    if (file != null) {
        String mimeType = file.getMIMEType();
        
        // Mapper MIME type → Langage
        if (mimeType.contains("java")) return "Java";
        if (mimeType.contains("xml")) return "XML";
        if (mimeType.contains("html")) return "HTML";
        // ...
        
        // Fallback sur l'extension
        String ext = file.getExt().toUpperCase();
        if (!ext.isEmpty()) return ext;
    }
    return null;
}
```

**Méthodes** :
1. Vérifier le MIME type du fichier
2. Mapper vers un nom de langage lisible
3. Fallback sur l'extension de fichier

### Fréquence de Mise à Jour

#### Mises à Jour Périodiques

```java
timer.scheduleAtFixedRate(rcpSchedule, new Date(), 12000l);
```

- **Fréquence** : Toutes les 12 secondes
- **Raison** : Détecter les changements qui ne déclenchent pas d'événements

#### Mises à Jour Immédiates

```java
registryListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateRCP(false);
        }
    }
};
TopComponent.getRegistry().addPropertyChangeListener(registryListener);
```

- **Événement** : Changement de fenêtre active
- **Réaction** : Mise à jour immédiate de la présence

## Gestion du Lifecycle

### Démarrage

```
NetBeans startup
       ↓
Installer.Installer()
       ↓
RCPSchedule created
       ↓
initializeDiscordRPC()
       ↓
Discord connected
```

### Exécution

```
Every 12 seconds:
  run() → updateRCP(false)

On window change:
  propertyChange() → updateRCP(false)

Continuous (every 500ms):
  callbackThread → runCallbacks()
```

### Fermeture

```java
public void shutdown() {
    running = false;
    
    // 1. Retirer le listener
    if (registryListener != null) {
        TopComponent.getRegistry().removePropertyChangeListener(registryListener);
    }
    
    // 2. Arrêter le thread de callbacks
    if (callbackThread != null) {
        callbackThread.interrupt();
    }
    
    // 3. Fermer Discord RPC
    try {
        DiscordRPC.shutdown();
    } catch (Exception e) {
        System.err.println("Error during Discord RPC shutdown: " + e.getMessage());
    }
}
```

**Importance** : Libérer proprement les ressources pour éviter :
- Fuite mémoire
- Connexions Discord pendantes
- Threads zombies

## Configuration Discord

### Créer une Application Discord

1. Aller sur [Discord Developer Portal](https://discord.com/developers/applications)
2. Cliquer "New Application"
3. Donner un nom (ex: "NetBeans IDE")
4. Copier l'**Application ID** (Client ID)

### Configurer Rich Presence

Dans votre application Discord :

1. Aller dans **Rich Presence** → **Art Assets**
2. Ajouter les images :

#### Large Image : `netbeans`
- Uploader le logo NetBeans
- Nom : `netbeans` (utilisé dans le code)
- Dimensions : 512x512px minimum

#### Small Image : `java`
- Uploader un icône Java/langage
- Nom : `java` (utilisé dans le code)
- Dimensions : 512x512px minimum

### Utiliser votre Application ID

Modifier `RCPSchedule.java` :

```java
private String applicationId = "VOTRE_APPLICATION_ID_ICI";
```

## Personnalisation

### Changer le Format d'Affichage

#### Modifier Details/State

```java
// Actuel
builder.setDetails("📁 " + projectName);
builder.setState("📝 Editing " + fileName);

// Alternative 1 : Sans émojis
builder.setDetails("Project: " + projectName);
builder.setState("File: " + fileName);

// Alternative 2 : Avec plus d'infos
builder.setDetails(projectName + " • " + fileType);
builder.setState("Editing " + fileName + " (" + fileSize + " lines)");
```

#### Ajouter des Boutons

```java
builder.setDetails("📁 " + projectName);
builder.setState("📝 Editing " + fileName);
// Nouveau : Boutons (nécessite configuration dans Discord)
```

**Note** : Les boutons nécessitent une configuration dans le Developer Portal.

### Ajouter Plus de Langages

Dans `detectFileType()` :

```java
// Ajouter de nouveaux types
if (mimeType.contains("rust")) return "Rust";
if (mimeType.contains("golang") || mimeType.contains("go")) return "Go";
if (mimeType.contains("typescript")) return "TypeScript";
if (mimeType.contains("markdown")) return "Markdown";
if (mimeType.contains("yaml")) return "YAML";
```

### Icônes Dynamiques par Langage

```java
// Au lieu d'un seul "java", choisir dynamiquement
String smallIcon = "java"; // default
if (fileType.equals("Python")) smallIcon = "python";
if (fileType.equals("JavaScript")) smallIcon = "javascript";
if (fileType.equals("Go")) smallIcon = "golang";

builder.setSmallImageKey(smallIcon);
```

**Prérequis** : Uploader les icônes dans Discord avec les bons noms.

### Ajouter des Statistiques

```java
// Exemple : Nombre de lignes
int lineCount = getLineCount(getCurrentFileObject());
builder.setState("Editing " + fileName + " (" + lineCount + " lines)");

// Exemple : Temps de codage
long startTime = /* sauvegarder au démarrage */;
long elapsed = (System.currentTimeMillis() - startTime) / 1000;
builder.setDetails("Coding for " + formatDuration(elapsed));
```

## Troubleshooting

### Discord ne montre pas la présence

**Vérifications** :

1. **Discord est-il ouvert ?**
   - La présence ne fonctionne que si Discord est lancé

2. **Application ID correct ?**
   - Vérifier que `applicationId` correspond à votre app Discord

3. **Assets configurés ?**
   - Les clés `netbeans` et `java` doivent exister dans Discord

4. **Paramètres Discord**
   - Vérifier : Paramètres → Activité → "Afficher le jeu en cours"

5. **Callback thread actif ?**
   - Vérifier les logs : "Discord RPC initialized!"

### Erreur "Could not connect to Discord"

**Causes possibles** :

1. Discord n'est pas lancé
2. Discord est en train de démarrer (attendre quelques secondes)
3. Problème de permissions (rare sur Windows/Mac)

**Solution** :
```java
// Ajouter un retry mechanism
private void initializeWithRetry(int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        try {
            DiscordRPC.initialise(applicationId, handlers, true, "");
            System.out.println("Discord RPC connected!");
            return;
        } catch (Exception e) {
            System.err.println("Attempt " + (i+1) + " failed, retrying...");
            Thread.sleep(2000);
        }
    }
    System.err.println("Could not connect to Discord after " + maxAttempts + " attempts");
}
```

### La présence ne se met pas à jour

**Vérifications** :

1. **Timer actif ?**
   ```java
   System.out.println("Timer running: " + (timer != null));
   ```

2. **Callbacks appelés ?**
   ```java
   // Ajouter dans le callback thread
   System.out.println("Callback tick");
   ```

3. **updatePresence appelé ?**
   ```java
   System.out.println("Updating presence: " + fileName);
   ```

4. **Exceptions silencieuses ?**
   - Retirer les `catch` vides pour voir les erreurs

### Assets non trouvés

**Message Discord** : "Unknown asset"

**Solution** :
1. Aller dans Discord Developer Portal
2. Rich Presence → Art Assets
3. Vérifier que les noms correspondent exactement :
   - `netbeans` (pas `NetBeans` ou `netbeans-logo`)
   - `java` (pas `Java` ou `java-icon`)

### Fuite mémoire / Thread zombie

**Symptôme** : NetBeans lent après plusieurs heures

**Cause** : Le thread de callbacks n'est pas arrêté

**Solution** : Vérifier que `shutdown()` est bien appelé dans `Installer.close()` :

```java
@Override
public void close() {
    if (timer != null) {
        timer.cancel();
        timer = null;
    }
    if (rcpSchedule != null) {
        rcpSchedule.shutdown(); // CRUCIAL
    }
    System.out.println("[PluginRPC] NetbeansRPC is closed.");
}
```

## Limites et Contraintes

### Limites Discord RPC

- **Taille des champs** :
  - Details : 128 caractères max
  - State : 128 caractères max
  - Large/Small Image Text : 128 caractères max

- **Fréquence de mise à jour** :
  - Recommandé : 1 update par seconde max
  - NetbeansRPC : 1 update toutes les 12 secondes

- **Assets** :
  - 300 assets max par application
  - 512x512px minimum
  - 1024x1024px recommandé

### Limites JNA

- Nécessite des bibliothèques natives
- Peut ne pas fonctionner sur toutes les architectures
- Dépend de la plateforme (Windows/Mac/Linux)

### Sécurité

- L'Application ID est visible dans le code
- Pas de secrets sensibles dans le RPC
- La communication est locale (IPC)

## Ressources

### Documentation Officielle

- [Discord Developer Portal](https://discord.com/developers/docs/rich-presence/how-to)
- [Discord RPC Best Practices](https://discord.com/developers/docs/rich-presence/best-practices)

### Bibliothèques

- [kawaxte/discord-rpc](https://github.com/kawaxte/discord-rpc) - Bibliothèque utilisée
- [JNA Documentation](https://github.com/java-native-access/jna)

### Communauté

- [Discord Developers](https://discord.gg/discord-developers)
- [Stack Overflow - discord-rpc tag](https://stackoverflow.com/questions/tagged/discord-rpc)

## Conclusion

L'intégration Discord RPC dans NetbeansRPC démontre :

- ✅ Communication inter-processus (IPC)
- ✅ Utilisation d'APIs natives via JNA
- ✅ Détection en temps réel via NetBeans APIs
- ✅ Gestion propre du lifecycle
- ✅ Architecture extensible

Le système est robuste, léger et facilement personnalisable pour d'autres IDEs ou applications.
