# Guide Complet de Développement de Plugins NetBeans

Ce guide vous accompagne pas à pas dans la création de plugins (modules) pour NetBeans IDE.

## Table des Matières

1. [Introduction](#introduction)
2. [Prérequis](#prérequis)
3. [Architecture des Plugins NetBeans](#architecture-des-plugins-netbeans)
4. [Création d'un Premier Plugin](#création-dun-premier-plugin)
5. [Structure d'un Module NetBeans](#structure-dun-module-netbeans)
6. [APIs NetBeans Essentielles](#apis-netbeans-essentielles)
7. [Lifecycle d'un Module](#lifecycle-dun-module)
8. [Exemples Pratiques](#exemples-pratiques)
9. [Build et Distribution](#build-et-distribution)
10. [Ressources Complémentaires](#ressources-complémentaires)

## Introduction

Un plugin NetBeans (aussi appelé **module**) est une extension qui ajoute des fonctionnalités à l'IDE NetBeans. NetBeans est construit sur la **NetBeans Platform**, une architecture modulaire qui permet de créer des applications desktop riches.

### Qu'est-ce qu'un Module NetBeans ?

- Un module est un fichier JAR avec des métadonnées spécifiques (fichier `.nbm`)
- Il peut étendre l'IDE avec de nouvelles fonctionnalités
- Il s'intègre dans le système de modules NetBeans
- Il peut dépendre d'autres modules

## Prérequis

Avant de commencer, assurez-vous d'avoir :

- **NetBeans IDE** 11.0 ou supérieur
- **JDK** 8 ou supérieur (JDK 11+ recommandé)
- **Apache Maven** 3.6 ou supérieur
- Connaissance basique de Java
- Compréhension de Maven (optionnel mais recommandé)

## Architecture des Plugins NetBeans

### NetBeans Platform

La NetBeans Platform est basée sur plusieurs concepts clés :

1. **Modules** : Unités de code réutilisables (plugins)
2. **Lookup** : Système de découverte de services
3. **File System API** : Abstraction du système de fichiers
4. **Window System** : Gestion des fenêtres et composants UI
5. **Actions** : Système d'actions utilisateur

### Types de Modules

- **Module Simple** : Ajoute une fonctionnalité spécifique
- **Module Suite** : Groupe de modules liés
- **Application NetBeans Platform** : Application standalone basée sur la platform

## Création d'un Premier Plugin

### Méthode 1 : Avec Maven (Recommandé)

#### 1. Créer le Projet Maven

```bash
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.netbeans.archetypes \
  -DarchetypeArtifactId=netbeans-platform-app-archetype \
  -DarchetypeVersion=1.23
```

Ou créer manuellement un `pom.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>mon-plugin-netbeans</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>nbm</packaging>
    
    <name>Mon Plugin NetBeans</name>
    <description>Description de mon plugin</description>
    
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <netbeans.version>RELEASE120</netbeans.version>
    </properties>
    
    <build>
        <plugins>
            <!-- Plugin Maven pour NBM -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>nbm-maven-plugin</artifactId>
                <version>4.7</version>
                <extensions>true</extensions>
            </plugin>
            
            <!-- Compilation Java -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
    <dependencies>
        <!-- APIs NetBeans de base -->
        <dependency>
            <groupId>org.netbeans.api</groupId>
            <artifactId>org-netbeans-api-annotations-common</artifactId>
            <version>${netbeans.version}</version>
        </dependency>
        <dependency>
            <groupId>org.netbeans.api</groupId>
            <artifactId>org-openide-util</artifactId>
            <version>${netbeans.version}</version>
        </dependency>
    </dependencies>
</project>
```

#### 2. Créer la Structure du Projet

```
mon-plugin-netbeans/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/monplugin/
        │       └── MonAction.java
        └── resources/
            └── com/example/monplugin/
                ├── Bundle.properties
                └── layer.xml
```

### Méthode 2 : Avec l'Assistant NetBeans

1. Dans NetBeans : `File` → `New Project`
2. Catégorie : `NetBeans Modules`
3. Type : `Module` ou `Module Suite`
4. Suivez l'assistant

## Structure d'un Module NetBeans

### Fichiers Essentiels

#### 1. pom.xml
Le fichier de configuration Maven qui définit :
- Les dépendances du module
- Les plugins Maven nécessaires
- Les métadonnées du projet

#### 2. Bundle.properties
Fichier de localisation contenant les métadonnées du module :

```properties
OpenIDE-Module-Name=Mon Plugin NetBeans
OpenIDE-Module-Display-Category=Tools
OpenIDE-Module-Short-Description=Description courte
OpenIDE-Module-Long-Description=Description détaillée du plugin
```

#### 3. layer.xml
Fichier de configuration déclarant les contributions du module :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.2//EN" 
    "http://www.netbeans.org/dtds/filesystem-1_2.dtd">
<filesystem>
    <!-- Actions, menus, toolbars, etc. -->
</filesystem>
```

#### 4. Module Installer (Optionnel)
Classe pour initialiser/nettoyer le module :

```java
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {
    
    @Override
    public void restored() {
        // Appelé quand le module est chargé
        System.out.println("Module démarré !");
    }
    
    @Override
    public void close() {
        // Appelé avant la fermeture du module
        System.out.println("Module arrêté !");
    }
}
```

Déclarez-le dans `layer.xml` :

```xml
<filesystem>
    <folder name="Modules">
        <file name="com-example-monplugin-Installer.instance"/>
    </folder>
</filesystem>
```

## APIs NetBeans Essentielles

### 1. Lookup API
Le système de découverte de services :

```java
import org.openide.util.Lookup;

// Trouver un service
MaService service = Lookup.getDefault().lookup(MaService.class);

// Trouver tous les services
Collection<? extends MaService> services = 
    Lookup.getDefault().lookupAll(MaService.class);
```

### 2. Actions API
Créer des actions utilisateur :

```java
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ActionID(
    category = "Tools",
    id = "com.example.MonAction"
)
@ActionRegistration(
    displayName = "#CTL_MonAction"
)
@ActionReference(
    path = "Menu/Tools",
    position = 100
)
@NbBundle.Messages("CTL_MonAction=Mon Action")
public final class MonAction implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Code de l'action
        System.out.println("Action exécutée !");
    }
}
```

### 3. Window System API
Créer des fenêtres personnalisées :

```java
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;

@ConvertAsProperties(
    dtd = "-//com.example//MaFenetre//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "MaFenetreTopComponent",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
    mode = "editor",
    openAtStartup = false
)
@ActionID(
    category = "Window",
    id = "com.example.MaFenetre"
)
@ActionReference(
    path = "Menu/Window",
    position = 100
)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_MaFenetreAction"
)
@NbBundle.Messages({
    "CTL_MaFenetreAction=Ma Fenêtre",
    "CTL_MaFenetreTopComponent=Ma Fenêtre"
})
public final class MaFenetreTopComponent extends TopComponent {
    
    public MaFenetreTopComponent() {
        initComponents();
        setName(Bundle.CTL_MaFenetreTopComponent());
    }
    
    private void initComponents() {
        // Initialisation des composants UI
    }
}
```

### 4. FileSystem API
Accéder aux fichiers du projet :

```java
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// Obtenir un fichier
FileObject fo = FileUtil.getConfigFile("path/to/file");

// Lire le contenu
InputStream is = fo.getInputStream();

// Écrire du contenu
OutputStream os = fo.getOutputStream();
```

### 5. Project API
Travailler avec les projets :

```java
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;

// Obtenir les projets ouverts
Project[] openProjects = OpenProjects.getDefault().getOpenProjects();

// Informations sur un projet
ProjectInformation info = ProjectUtils.getInformation(project);
String projectName = info.getDisplayName();
```

### 6. Editor API
Interagir avec l'éditeur :

```java
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import javax.swing.text.Document;

// Obtenir le document actif
TopComponent activated = TopComponent.getRegistry().getActivated();
EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
if (ec != null) {
    Document doc = ec.getDocument();
    // Manipuler le document
}
```

## Lifecycle d'un Module

### Phases du Lifecycle

1. **Installation** : Le module est installé dans NetBeans
2. **Chargement** : Les classes du module sont chargées
3. **Restauration** : `restored()` est appelée (si Installer existe)
4. **Exécution** : Le module est actif
5. **Fermeture** : `close()` est appelée avant la désactivation
6. **Désinstallation** : Le module est retiré

### Utilisation de @OnShowing

Pour exécuter du code quand la fenêtre principale apparaît :

```java
import org.openide.windows.OnShowing;
import org.openide.modules.ModuleInstall;

@OnShowing
public class Installer extends ModuleInstall implements Runnable {
    
    @Override
    public void run() {
        // Code exécuté quand l'UI est prête
        System.out.println("UI prête !");
    }
}
```

## Exemples Pratiques

### Exemple 1 : Action Simple dans le Menu

```java
package com.example.monplugin;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

@ActionID(
    category = "Tools",
    id = "com.example.monplugin.HelloAction"
)
@ActionRegistration(
    displayName = "#CTL_HelloAction"
)
@ActionReference(
    path = "Menu/Tools",
    position = 1000
)
@Messages("CTL_HelloAction=Dire Bonjour")
public final class HelloAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Bonjour depuis mon plugin !");
    }
}
```

### Exemple 2 : Service Global

Définir une interface de service :

```java
package com.example.monplugin;

public interface MonService {
    String getMessage();
}
```

Implémenter le service :

```java
package com.example.monplugin.impl;

import com.example.monplugin.MonService;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = MonService.class)
public class MonServiceImpl implements MonService {
    
    @Override
    public String getMessage() {
        return "Message du service !";
    }
}
```

Utiliser le service :

```java
MonService service = Lookup.getDefault().lookup(MonService.class);
if (service != null) {
    String message = service.getMessage();
}
```

### Exemple 3 : Écouter les Changements de Fichier

```java
package com.example.monplugin;

import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MonFileListener extends FileChangeAdapter {
    
    public MonFileListener() {
        // Écouter un dossier spécifique
        FileObject projectDir = FileUtil.getConfigFile("Projects/");
        if (projectDir != null) {
            projectDir.addFileChangeListener(this);
        }
    }
    
    @Override
    public void fileDataCreated(FileEvent fe) {
        System.out.println("Fichier créé : " + fe.getFile().getPath());
    }
    
    @Override
    public void fileDeleted(FileEvent fe) {
        System.out.println("Fichier supprimé : " + fe.getFile().getPath());
    }
}
```

### Exemple 4 : Tâche Périodique (comme NetbeansRPC)

```java
package com.example.monplugin;

import java.util.Timer;
import java.util.TimerTask;
import org.openide.modules.ModuleInstall;
import org.openide.windows.OnShowing;

@OnShowing
public class Installer extends ModuleInstall implements Runnable {
    
    private Timer timer;
    private MaTache tache;
    
    @Override
    public void run() {
        timer = new Timer();
        tache = new MaTache();
        
        // Exécuter toutes les 10 secondes
        timer.scheduleAtFixedRate(tache, 0, 10000);
        
        System.out.println("Plugin démarré avec tâche périodique");
    }
    
    @Override
    public void close() {
        if (timer != null) {
            timer.cancel();
        }
        if (tache != null) {
            tache.cleanup();
        }
        System.out.println("Plugin arrêté");
    }
    
    private static class MaTache extends TimerTask {
        
        @Override
        public void run() {
            // Code exécuté périodiquement
            System.out.println("Tâche exécutée à " + System.currentTimeMillis());
        }
        
        public void cleanup() {
            // Nettoyage des ressources
        }
    }
}
```

## Build et Distribution

### Commandes Maven Utiles

```bash
# Compiler le module
mvn clean compile

# Créer le package NBM
mvn clean package

# Exécuter dans une plateforme de test
mvn nbm:run-platform

# Installer dans NetBeans local
mvn nbm:install

# Nettoyer les fichiers générés
mvn clean
```

### Structure du NBM Généré

Le fichier `.nbm` généré dans `target/` contient :
- Le JAR du module
- Les dépendances
- Les métadonnées (manifest, etc.)

### Distribution

Pour distribuer votre plugin :

1. **Update Center** : Créer un catalogue XML pour auto-update
2. **Fichier NBM** : Distribuer directement le fichier `.nbm`
3. **Maven Central** : Publier sur un repository Maven
4. **NetBeans Plugin Portal** : Soumettre au portail officiel

### Créer un Update Center

Créez un fichier `updates.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module_updates timestamp="00/00/0000">
    <module codenamebase="com.example.monplugin"
            distribution="http://example.com/mon-plugin-1.0.nbm"
            downloadsize="154321"
            homepage="http://example.com"
            moduleauthor="Votre Nom"
            needsrestart="false"
            releasedate="2024/01/01">
        <manifest 
            AutoUpdate-Show-In-Client="true"
            OpenIDE-Module="com.example.monplugin"
            OpenIDE-Module-Name="Mon Plugin"
            OpenIDE-Module-Specification-Version="1.0"/>
    </module>
</module_updates>
```

## Dépendances Courantes

### Ajouter des Dépendances NetBeans

```xml
<dependencies>
    <!-- Annotations -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-netbeans-api-annotations-common</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-util</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- UI Utilities -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-util-ui</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Lookup API -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-util-lookup</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Window System -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-windows</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Module System -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-modules</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- File System -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-filesystems</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Project API -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-netbeans-modules-projectapi</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Editor/Text API -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-text</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Data Systems -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-loaders</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
    
    <!-- Nodes API -->
    <dependency>
        <groupId>org.netbeans.api</groupId>
        <artifactId>org-openide-nodes</artifactId>
        <version>${netbeans.version}</version>
    </dependency>
</dependencies>
```

### Ajouter des Bibliothèques Externes

Pour inclure des bibliothèques tierces (comme discord-rpc dans NetbeansRPC) :

```xml
<dependency>
    <groupId>io.github.kawaxte</groupId>
    <artifactId>discord-rpc</artifactId>
    <version>20230409</version>
</dependency>
```

## Debugging

### Déboguer un Module

1. Ajouter des points d'arrêt dans votre code
2. Exécuter avec Maven :
   ```bash
   mvnDebug nbm:run-platform
   ```
3. Attacher le débogueur NetBeans (Debug → Attach Debugger)
4. Port par défaut : 8000

### Logs et Sorties

```java
// Utiliser le logger NetBeans
import java.util.logging.Logger;
import java.util.logging.Level;

private static final Logger LOG = Logger.getLogger(MaClasse.class.getName());

LOG.log(Level.INFO, "Message d'info");
LOG.log(Level.WARNING, "Attention !");
LOG.log(Level.SEVERE, "Erreur grave", exception);

// Afficher dans la console NetBeans
System.out.println("Debug : " + message);

// Afficher les exceptions
import org.openide.util.Exceptions;
Exceptions.printStackTrace(exception);
```

## Bonnes Pratiques

### 1. Gestion des Ressources
- Toujours nettoyer les ressources dans `close()`
- Utiliser `try-with-resources` pour les streams
- Annuler les timers et threads

### 2. Thread Safety
- Ne pas bloquer l'Event Dispatch Thread (EDT)
- Utiliser `RequestProcessor` pour les tâches longues :

```java
import org.openide.util.RequestProcessor;

private static final RequestProcessor RP = 
    new RequestProcessor("Mon Task", 5);

RP.post(() -> {
    // Tâche longue en arrière-plan
});
```

### 3. Localisation
- Utiliser des bundles pour les textes
- Supporter plusieurs langues

```properties
# Bundle.properties (anglais)
MSG_Hello=Hello World

# Bundle_fr.properties (français)
MSG_Hello=Bonjour le Monde
```

### 4. Performance
- Limiter la fréquence des updates
- Utiliser des caches si nécessaire
- Éviter les opérations coûteuses dans l'EDT

### 5. Erreurs
- Gérer les exceptions proprement
- Ne pas faire crasher NetBeans
- Logger les erreurs pour le debugging

## Ressources Complémentaires

### Documentation Officielle

- [NetBeans Platform Developer's Guide](https://netbeans.apache.org/tutorials/index.html)
- [NetBeans API Javadoc](https://bits.netbeans.org/dev/javadoc/)
- [NetBeans Wiki](https://cwiki.apache.org/confluence/display/NETBEANS/)

### Tutoriels

- [NetBeans Platform Quick Start](https://netbeans.apache.org/tutorials/nbm-google.html)
- [NetBeans Platform Learning Trail](https://netbeans.apache.org/kb/docs/platform.html)
- [Maven NetBeans Modules](https://netbeans.apache.org/tutorials/nbm-maven-quickstart.html)

### Communauté

- [NetBeans Mailing Lists](https://netbeans.apache.org/community/mailing-lists.html)
- [Stack Overflow - netbeans-platform tag](https://stackoverflow.com/questions/tagged/netbeans-platform)
- [GitHub - NetBeans Repository](https://github.com/apache/netbeans)

### Livres

- "NetBeans Platform for Beginners" (Geertjan Wielenga)
- "The Definitive Guide to NetBeans Platform" (Heiko Böck)

## Conclusion

Créer des plugins NetBeans permet d'étendre l'IDE avec des fonctionnalités personnalisées. La NetBeans Platform offre une architecture solide et des APIs riches pour développer des modules professionnels.

Points clés à retenir :
- Utiliser Maven pour la gestion du projet
- Comprendre le lifecycle des modules
- Exploiter les APIs NetBeans (Lookup, Actions, FileSystem, etc.)
- Tester régulièrement avec `mvn nbm:run-platform`
- Documenter et distribuer correctement

Bon développement de plugins NetBeans ! 🚀
