package fr.pedrokarim.netbeansrpc;

import io.github.kawaxte.presence.DiscordEventHandlers;
import io.github.kawaxte.presence.DiscordRPC;
import io.github.kawaxte.presence.DiscordRichPresence;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TimerTask;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author karim
 */
public class RCPSchedule extends TimerTask {

    private DiscordRichPresence presence;
    private PropertyChangeListener registryListener;
    private String currentProject;
    private String currentFile;
    private String currentFileType;
    private Thread callbackThread;
    private volatile boolean running = true;
    private long startTimestamp = 0;

    public RCPSchedule() {
        try {
            initializeDiscordRPC();
            setupListener();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void setupListener() {
        // Listen for changes in the active TopComponent
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

    @Override
    public void run() {
        try {
            updateRCP(false);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private FileObject getCurrentFileObject() {
        try {
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null) {
                EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
                if (ec != null && ec.getDocument() != null) {
                    Document doc = ec.getDocument();
                    Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
                    if (streamDesc instanceof DataObject) {
                        DataObject dataObject = (DataObject) streamDesc;
                        return dataObject.getPrimaryFile();
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle errors in file detection
        }
        return null;
    }

    private String detectCurrentFile() {
        FileObject file = getCurrentFileObject();
        return file != null ? file.getNameExt() : null;
    }

    private String detectCurrentProject() {
        FileObject file = getCurrentFileObject();
        if (file != null) {
            try {
                Project project = FileOwnerQuery.getOwner(file);
                if (project != null) {
                    return ProjectUtils.getInformation(project).getDisplayName();
                }
            } catch (Exception e) {
                // Silently handle errors in project detection
            }
        }
        return null;
    }

    private String detectFileType() {
        FileObject file = getCurrentFileObject();
        if (file != null) {
            try {
                String mimeType = file.getMIMEType();
                
                // Map MIME types to readable language names
                if (mimeType.contains("java")) return "Java";
                if (mimeType.contains("xml")) return "XML";
                if (mimeType.contains("html")) return "HTML";
                if (mimeType.contains("javascript")) return "JavaScript";
                if (mimeType.contains("css")) return "CSS";
                if (mimeType.contains("python")) return "Python";
                if (mimeType.contains("json")) return "JSON";
                if (mimeType.contains("properties")) return "Properties";
                if (mimeType.contains("kotlin")) return "Kotlin";
                if (mimeType.contains("groovy")) return "Groovy";
                if (mimeType.contains("php")) return "PHP";
                if (mimeType.contains("typescript")) return "TypeScript";
                if (mimeType.contains("markdown")) return "Markdown";
                if (mimeType.contains("yaml")) return "YAML";
                if (mimeType.contains("sql")) return "SQL";
                
                // Fallback to extension
                String ext = file.getExt().toUpperCase();
                if (!ext.isEmpty()) return ext;
            } catch (Exception e) {
                // Silently handle errors in file type detection
            }
        }
        return null;
    }

    public void initializeDiscordRPC() {
        try {
            String applicationId = DiscordRPCSettings.getApplicationId();
            
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = (user) -> {
                System.out.println("Discord RPC Ready! Connected as: " + user.username);
                updateUIStatus("Connected to Discord as " + user.username);
            };
            
            handlers.disconnected = (errorCode, message) -> {
                System.err.println("Discord RPC Disconnected: " + message);
                updateUIStatus("Disconnected from Discord: " + message);
            };
            
            handlers.errored = (errorCode, message) -> {
                System.err.println("Discord RPC Error: " + message);
                updateUIStatus("Error: " + message);
            };
            
            DiscordRPC.initialise(applicationId, handlers, true, "");
            
            // Start callback thread
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
            callbackThread.setDaemon(true);
            callbackThread.start();
            
            System.out.println("Discord RPC initialized with Application ID: " + applicationId);
            updateRCP(true);
        } catch (Exception e) {
            System.err.println("Failed to initialize Discord RPC: " + e.getMessage());
            updateUIStatus("Failed to initialize: " + e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }
    
    private void updateRCP(Boolean timestamp) {
        try {
            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
            
            if (timestamp != null && timestamp) {
                startTimestamp = System.currentTimeMillis() / 1000;
            }
            
            if (DiscordRPCSettings.isShowTimestamp() && startTimestamp > 0) {
                builder.setStartTimestamp(startTimestamp);
            }
            
            // Detect via NetBeans APIs
            String fileName = detectCurrentFile();
            String projectName = detectCurrentProject();
            String fileType = detectFileType();
            
            // Store for UI updates
            currentProject = projectName;
            currentFile = fileName;
            currentFileType = fileType;
            
            // Set presence details based on settings
            if (DiscordRPCSettings.isShowProject() && projectName != null) {
                builder.setDetails("📁 " + projectName);
            } else {
                builder.setDetails("Working in NetBeans IDE");
            }
            
            if (DiscordRPCSettings.isShowFile() && fileName != null) {
                builder.setState("📝 Editing " + fileName);
            } else {
                builder.setState("Idle");
            }
            
            builder.setLargeImageKey("netbeans");
            builder.setLargeImageText("NetBeans IDE");
            
            if (fileType != null) {
                builder.setSmallImageKey("java"); // Note: Ensure assets are configured in Discord Developer Portal
                builder.setSmallImageText("Programming in " + fileType);
            } else {
                builder.setSmallImageKey("java");
                builder.setSmallImageText("NetBeans");
            }
            
            presence = builder.build();
            
            // Send to Discord!
            DiscordRPC.updatePresence(presence);
            
            // Update UI panel if open
            updateUIPresence();
        } catch (Exception e) {
            System.err.println("Failed to update Discord presence: " + e.getMessage());
        }
    }
    
    private void updateUIStatus(String message) {
        try {
            DiscordRPCPanel panel = DiscordRPCPanel.getInstance();
            if (panel != null) {
                panel.updateStatus(message);
            }
        } catch (Exception e) {
            // Ignore if UI is not available
        }
    }
    
    private void updateUIPresence() {
        try {
            DiscordRPCPanel panel = DiscordRPCPanel.getInstance();
            if (panel != null) {
                panel.updatePresenceInfo(currentProject, currentFile, currentFileType);
            }
        } catch (Exception e) {
            // Ignore if UI is not available
        }
    }

    public void shutdown() {
        running = false;
        if (registryListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(registryListener);
        }
        if (callbackThread != null) {
            callbackThread.interrupt();
        }
        try {
            DiscordRPC.shutdown();
            updateUIStatus("Discord RPC shutdown complete");
        } catch (Exception e) {
            System.err.println("Error during Discord RPC shutdown: " + e.getMessage());
        }
    }
    
}
