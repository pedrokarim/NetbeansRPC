# Structure Détaillée du Projet NetbeansRPC

Ce document explique en détail l'organisation et la structure du projet NetbeansRPC.

## Vue d'Ensemble

NetbeansRPC est un module NetBeans qui intègre Discord Rich Presence pour afficher l'activité de développement en temps réel sur Discord.

## Arborescence Complète

```
NetbeansRPC/
├── pom.xml                                          # Configuration Maven
├── README.md                                        # Documentation principale
├── LICENSE                                          # Licence GPL-3.0
├── note.txt                                         # Notes de développement
├── .gitignore                                       # Fichiers ignorés par Git
│
├── doc/                                             # Documentation
│   ├── netbeans-plugin-development-guide.md        # Guide de développement plugins
│   ├── project-structure.md                         # Ce fichier
│   └── discord-rpc-integration.md                   # Intégration Discord RPC
│
├── src/                                             # Code source
│   └── main/
│       ├── java/                                    # Code Java
│       │   └── fr/pedrokarim/netbeansrpc/
│       │       ├── Installer.java                   # Point d'entrée du module
│       │       └── RCPSchedule.java                 # Logique Discord RPC
│       │
│       ├── resources/                               # Ressources
│       │   └── fr/pedrokarim/netbeansrpc/
│       │       ├── Bundle.properties                # Métadonnées du module
│       │       ├── layer.xml                        # Configuration NetBeans
│       │       └── icon/                            # Icônes du module
│       │           └── vectorpaint.png
│       │
│       └── nbm/                                     # Configuration NBM
│           └── manifest.mf                          # Manifest NetBeans
│
└── target/                                          # Dossier de build (généré)
    ├── classes/                                     # Classes compilées
    ├── NetbeansRPC-1.2.nbm                         # Module packagé
    └── ...                                          # Autres artifacts
```

## Description des Fichiers Principaux

### 1. pom.xml

**Rôle** : Configuration Maven du projet

**Contenu clé** :
- **packaging** : `nbm` (NetBeans Module)
- **version** : 1.2
- **plugins** : 
  - `nbm-maven-plugin` (v3.13) : Création du module NetBeans
  - `maven-compiler-plugin` (v2.5.1) : Compilation Java 8
  - `maven-jar-plugin` (v2.4) : Création du JAR
- **dependencies** : 
  - APIs NetBeans (RELEASE110)
  - discord-rpc (20230409) - Bibliothèque Discord
  - JNA (5.13.0) - Java Native Access

**Points importants** :
```xml
<packaging>nbm</packaging>  <!-- Type de package : NetBeans Module -->
<version>1.2</version>       <!-- Version du plugin -->
```

### 2. Installer.java

**Rôle** : Point d'entrée et gestionnaire du lifecycle du module

**Emplacement** : `src/main/java/fr/pedrokarim/netbeansrpc/Installer.java`

**Fonctionnalités** :
- Démarre automatiquement avec NetBeans grâce à `@OnShowing`
- Initialise le Timer pour les mises à jour périodiques
- Crée et démarre `RCPSchedule`
- Gère la fermeture propre du module

**Code clé** :
```java
@OnShowing
public class Installer extends ModuleInstall implements Runnable {
    private static Timer timer;
    private static RCPSchedule rcpSchedule;
    
    public Installer() {
        timer = new Timer();
        rcpSchedule = new RCPSchedule();
        timer.scheduleAtFixedRate(rcpSchedule, new Date(), 12000l);
    }
}
```

**Lifecycle** :
1. Constructeur appelé → Initialisation
2. `run()` vide (pour compatibilité `@OnShowing`)
3. `close()` → Nettoyage avant fermeture

### 3. RCPSchedule.java

**Rôle** : Cœur de la logique Discord RPC

**Emplacement** : `src/main/java/fr/pedrokarim/netbeansrpc/RCPSchedule.java`

**Responsabilités** :

#### a) Initialisation Discord RPC
```java
private String applicationId = "621768079386345477";

public void initializeDiscordRPC() {
    DiscordEventHandlers handlers = new DiscordEventHandlers();
    handlers.ready = (user) -> System.out.println("Discord RPC Ready!");
    DiscordRPC.initialise(applicationId, handlers, true, "");
    // Démarre le thread de callbacks
}
```

#### b) Détection du Contexte NetBeans
- `getCurrentFileObject()` : Obtient le fichier actif
- `detectCurrentFile()` : Nom du fichier en cours
- `detectCurrentProject()` : Nom du projet
- `detectFileType()` : Type de fichier (Java, XML, etc.)

**APIs NetBeans utilisées** :
- `TopComponent.getRegistry()` : Fenêtres actives
- `EditorCookie` : Document en cours d'édition
- `FileOwnerQuery` : Projet propriétaire d'un fichier
- `ProjectUtils` : Informations sur les projets

#### c) Mise à Jour de la Présence
```java
private void updateRCP(Boolean timestamp) {
    DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
    
    // Configuration de la présence
    builder.setDetails("📁 " + projectName);
    builder.setState("📝 Editing " + fileName);
    builder.setLargeImageKey("netbeans");
    builder.setSmallImageKey("java");
    
    presence = builder.build();
    DiscordRPC.updatePresence(presence);
}
```

#### d) Listener sur les Changements
```java
private void setupListener() {
    registryListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                updateRCP(false);
            }
        }
    };
    TopComponent.getRegistry().addPropertyChangeListener(registryListener);
}
```

#### e) Thread de Callbacks Discord
```java
callbackThread = new Thread(() -> {
    while (running && !Thread.currentThread().isInterrupted()) {
        DiscordRPC.runCallbacks();
        Thread.sleep(500);
    }
}, "RPC-Callback-Handler");
```

### 4. Bundle.properties

**Rôle** : Métadonnées et localisation du module

**Emplacement** : `src/main/resources/fr/pedrokarim/netbeansrpc/Bundle.properties`

**Contenu** :
```properties
OpenIDE-Module-Name=NetbeansRPC
```

**Utilité** :
- Nom affiché dans NetBeans
- Support de la localisation (Bundle_fr.properties, etc.)
- Descriptions courte et longue du module

### 5. layer.xml

**Rôle** : Configuration de la layer NetBeans

**Emplacement** : `src/main/resources/fr/pedrokarim/netbeansrpc/layer.xml`

**Contenu actuel** :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.2//EN" 
    "http://www.netbeans.org/dtds/filesystem-1_2.dtd">
<filesystem>
</filesystem>
```

**Utilité potentielle** :
- Enregistrement d'actions dans les menus
- Configuration de l'UI
- Déclaration de services
- Options et préférences

### 6. manifest.mf

**Rôle** : Manifest du module NetBeans

**Emplacement** : `src/main/nbm/manifest.mf`

**Contenu type** :
```
Manifest-Version: 1.0
OpenIDE-Module: fr.pedrokarim.NetbeansRPC
OpenIDE-Module-Specification-Version: 1.2
OpenIDE-Module-Implementation-Version: 1.2
```

## Architecture du Code

### Diagramme de Classes Simplifié

```
┌─────────────────────────┐
│      Installer          │
│   (ModuleInstall)       │
│   @OnShowing            │
├─────────────────────────┤
│ - timer: Timer          │
│ - rcpSchedule: RCPSch.. │
├─────────────────────────┤
│ + Installer()           │
│ + run()                 │
│ + close()               │
└───────────┬─────────────┘
            │ crée et gère
            ▼
┌─────────────────────────┐
│    RCPSchedule          │
│    (TimerTask)          │
├─────────────────────────┤
│ - presence: DiscordRich │
│ - applicationId: String │
│ - callbackThread: Thr.. │
│ - registryListener: Pr..│
├─────────────────────────┤
│ + run()                 │
│ + initializeDiscordRPC()│
│ + updateRCP()           │
│ + getCurrentFileObject()│
│ + detectCurrentFile()   │
│ + detectCurrentProject()│
│ + detectFileType()      │
│ + setupListener()       │
│ + shutdown()            │
└─────────────────────────┘
```

### Flux d'Exécution

```
1. NetBeans démarre
   ↓
2. Installer.Installer() appelé
   ↓
3. Timer créé avec RCPSchedule
   ↓
4. RCPSchedule.initializeDiscordRPC()
   - Connexion à Discord
   - Démarrage du thread de callbacks
   - Configuration du listener TopComponent
   ↓
5. updateRCP(true) - Première mise à jour avec timestamp
   ↓
6. Timer schedule updates every 12 seconds
   - RCPSchedule.run() appelé
   - updateRCP(false) sans nouveau timestamp
   ↓
7. Listener détecte changements de fenêtre
   - PropertyChangeListener trigger
   - updateRCP(false) immédiat
   ↓
8. NetBeans se ferme
   ↓
9. Installer.close()
   - Timer.cancel()
   - RCPSchedule.shutdown()
   - DiscordRPC.shutdown()
```

## Dépendances Externes

### Discord RPC Library

**Dépendance** :
```xml
<dependency>
    <groupId>io.github.kawaxte</groupId>
    <artifactId>discord-rpc</artifactId>
    <version>20230409</version>
</dependency>
```

**Classes utilisées** :
- `DiscordRPC` : API principale
- `DiscordRichPresence` : Modèle de présence
- `DiscordEventHandlers` : Gestionnaires d'événements

**Documentation** : [GitHub - kawaxte/discord-rpc](https://github.com/kawaxte/discord-rpc)

### JNA (Java Native Access)

**Dépendances** :
```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.13.0</version>
</dependency>
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.13.0</version>
</dependency>
```

**Utilité** : Permet à la bibliothèque Discord RPC d'interagir avec les bibliothèques natives Discord

### APIs NetBeans

**Module** : `org.netbeans.api:*:RELEASE110`

**APIs utilisées** :

1. **org-netbeans-api-annotations-common**
   - Annotations pour la déclaration de composants

2. **org-openide-windows**
   - `TopComponent` : Gestion des fenêtres
   - `TopComponent.Registry` : Registre des composants

3. **org-openide-modules**
   - `ModuleInstall` : Lifecycle des modules
   - `OnShowing` : Annotation de démarrage

4. **org-netbeans-modules-projectapi**
   - `Project` : Représentation d'un projet
   - `FileOwnerQuery` : Trouver le projet d'un fichier
   - `ProjectUtils` : Utilitaires projet

5. **org-openide-filesystems**
   - `FileObject` : Abstraction de fichier
   - `FileUtil` : Utilitaires fichiers

6. **org-openide-text**
   - `EditorCookie` : Accès aux documents en édition

7. **org-openide-loaders**
   - `DataObject` : Représentation de fichiers

8. **org-openide-nodes**
   - `Node` : Nœuds dans l'arbre de projet

9. **org-openide-util**
   - `Exceptions` : Gestion des exceptions
   - `Lookup` : Découverte de services

10. **org-openide-util-ui**
    - Utilitaires UI

11. **org-openide-util-lookup**
    - Système Lookup

## Configuration Discord

### Application Discord

L'ID d'application Discord est codé en dur :
```java
private String applicationId = "621768079386345477";
```

### Assets Discord Requis

Pour que les images s'affichent dans Discord, configurez ces assets dans le [Discord Developer Portal](https://discord.com/developers/applications) :

1. **netbeans** (Large Image)
   - Logo NetBeans IDE
   - Dimensions recommandées : 512x512px minimum

2. **java** (Small Image)
   - Icône Java ou langage
   - Dimensions recommandées : 512x512px minimum

### Structure de la Présence Discord

```javascript
{
  "details": "📁 Nom du Projet",          // 1ère ligne
  "state": "📝 Editing NomDuFichier.java", // 2ème ligne
  "timestamps": {
    "start": 1234567890                    // Timestamp de début
  },
  "assets": {
    "large_image": "netbeans",             // Asset défini dans Discord
    "large_text": "NetBeans IDE",          // Tooltip de la grande image
    "small_image": "java",                 // Asset défini dans Discord
    "small_text": "Programming in Java"    // Tooltip de la petite image
  }
}
```

## Build et Packaging

### Commandes Maven

```bash
# Compilation
mvn clean compile

# Création du NBM
mvn clean package
# Génère : target/NetbeansRPC-1.2.nbm

# Test en environnement isolé
mvn nbm:run-platform

# Nettoyage
mvn clean
```

### Contenu du NBM

Le fichier `.nbm` est un ZIP contenant :
```
NetbeansRPC-1.2.nbm
├── Info/
│   └── info.xml                    # Métadonnées du module
├── netbeans/
│   └── modules/
│       ├── NetbeansRPC.jar         # Module principal
│       └── ext/                     # Dépendances externes
│           ├── discord-rpc-*.jar
│           ├── jna-*.jar
│           └── jna-platform-*.jar
└── META-INF/
    └── MANIFEST.MF
```

## Extension et Personnalisation

### Ajouter de Nouvelles Détections de Langages

Dans `RCPSchedule.detectFileType()` :

```java
private String detectFileType() {
    FileObject file = getCurrentFileObject();
    if (file != null) {
        String mimeType = file.getMIMEType();
        
        // Ajouter de nouveaux types MIME
        if (mimeType.contains("rust")) return "Rust";
        if (mimeType.contains("go")) return "Go";
        // ...
    }
    return null;
}
```

### Modifier la Fréquence de Mise à Jour

Dans `Installer.java` :

```java
// 12000ms = 12 secondes
timer.scheduleAtFixedRate(rcpSchedule, new Date(), 12000l);

// Modifier pour 30 secondes :
timer.scheduleAtFixedRate(rcpSchedule, new Date(), 30000l);
```

### Ajouter des Actions Utilisateur

Créer une nouvelle classe `MonAction.java` et la déclarer dans `layer.xml`.

### Ajouter des Options de Configuration

Utiliser l'API NetBeans Options pour permettre à l'utilisateur de configurer :
- Application ID Discord
- Fréquence de mise à jour
- Langages détectés
- Format de l'affichage

## Points d'Attention

### Thread Safety

- Le `callbackThread` doit être correctement arrêté
- Les mises à jour UI doivent être faites sur l'EDT si nécessaire
- Le `Timer` doit être annulé proprement

### Gestion de la Mémoire

- Les listeners doivent être retirés dans `shutdown()`
- Les ressources natives (Discord RPC) doivent être libérées
- Pas de références circulaires

### Erreurs Silencieuses

Le code capture les exceptions sans les remonter :
```java
catch (Exception e) {
    // Silently handle errors
}
```

**Pourquoi ?** Pour éviter de faire crasher NetBeans en cas d'erreur Discord/API.

**Alternative recommandée** :
```java
catch (Exception e) {
    LOG.log(Level.WARNING, "Failed to detect file", e);
}
```

## Tests et Debugging

### Logger les Événements

Ajouter des logs pour déboguer :

```java
private static final Logger LOG = Logger.getLogger(RCPSchedule.class.getName());

LOG.info("Discord RPC initialized");
LOG.log(Level.FINE, "Updating presence: project={0}, file={1}", 
    new Object[]{projectName, fileName});
```

### Vérifier la Présence Discord

1. Ouvrir Discord
2. Vérifier votre profil
3. Ouvrir un projet dans NetBeans
4. Éditer un fichier
5. Attendre 12 secondes ou changer de fichier

### Déboguer le Module

```bash
mvnDebug nbm:run-platform
```

Puis attacher un débogueur sur le port 8000.

## Conclusion

NetbeansRPC est un plugin compact et efficace qui démontre :
- L'intégration d'APIs externes (Discord)
- L'utilisation des APIs NetBeans Platform
- La gestion du lifecycle d'un module
- Les tâches périodiques et listeners d'événements

La structure est simple mais extensible, permettant d'ajouter facilement de nouvelles fonctionnalités.
