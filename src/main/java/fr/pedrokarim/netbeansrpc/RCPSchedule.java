/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    private String applicationId = "621768079386345477";
    private Thread callbackThread;
    private volatile boolean running = true;

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

    private String detectCurrentFile() {
        try {
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null) {
                EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
                if (ec != null && ec.getDocument() != null) {
                    Document doc = ec.getDocument();
                    Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
                    if (streamDesc instanceof DataObject) {
                        DataObject dataObject = (DataObject) streamDesc;
                        FileObject file = dataObject.getPrimaryFile();
                        return file.getNameExt();
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle errors in file detection
        }
        return null;
    }

    private String detectCurrentProject() {
        try {
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null) {
                EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
                if (ec != null && ec.getDocument() != null) {
                    Document doc = ec.getDocument();
                    Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
                    if (streamDesc instanceof DataObject) {
                        DataObject dataObject = (DataObject) streamDesc;
                        FileObject file = dataObject.getPrimaryFile();
                        Project project = FileOwnerQuery.getOwner(file);
                        if (project != null) {
                            return ProjectUtils.getInformation(project).getDisplayName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle errors in project detection
        }
        return null;
    }

    private String detectFileType() {
        try {
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null) {
                EditorCookie ec = activated.getLookup().lookup(EditorCookie.class);
                if (ec != null && ec.getDocument() != null) {
                    Document doc = ec.getDocument();
                    Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
                    if (streamDesc instanceof DataObject) {
                        DataObject dataObject = (DataObject) streamDesc;
                        FileObject file = dataObject.getPrimaryFile();
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
                        
                        // Fallback to extension
                        String ext = file.getExt().toUpperCase();
                        if (!ext.isEmpty()) return ext;
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle errors in file type detection
        }
        return null;
    }

    public void initializeDiscordRPC() {
        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = (user) -> System.out.println("Discord RPC Ready!");
            
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
            callbackThread.start();
            
            System.out.println("Discord RPC initialized!");
            updateRCP(true);
        } catch (Exception e) {
            System.err.println("Failed to initialize Discord RPC: " + e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }
    
    private void updateRCP(Boolean timestamp) {
        try {
            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
            
            if (timestamp != null && timestamp) {
                builder.setStartTimestamp(System.currentTimeMillis() / 1000);
            }
            
            // Detect via NetBeans APIs
            String fileName = detectCurrentFile();
            String projectName = detectCurrentProject();
            String fileType = detectFileType();
            
            // Set presence details
            if (projectName != null) {
                builder.setDetails("📁 " + projectName);
            } else {
                builder.setDetails("Working in NetBeans IDE");
            }
            
            if (fileName != null) {
                builder.setState("📝 Editing " + fileName);
            } else {
                builder.setState("Idle");
            }
            
            builder.setLargeImageKey("netbeans");
            builder.setLargeImageText("NetBeans IDE");
            
            if (fileType != null) {
                builder.setSmallImageKey("java"); // Could be enhanced with file type icons
                builder.setSmallImageText("Programming in " + fileType);
            } else {
                builder.setSmallImageKey("java");
                builder.setSmallImageText("NetBeans");
            }
            
            presence = builder.build();
            
            // CRUCIAL: Send to Discord!
            DiscordRPC.updatePresence(presence);
        } catch (Exception e) {
            System.err.println("Failed to update Discord presence: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("Error during Discord RPC shutdown: " + e.getMessage());
        }
    }
    
}
