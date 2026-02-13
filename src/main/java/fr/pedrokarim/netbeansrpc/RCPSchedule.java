package fr.pedrokarim.netbeansrpc;

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

    private DiscordIPCClient ipcClient;
    private PropertyChangeListener registryListener;
    private String currentProject;
    private String currentFile;
    private String currentFileType;
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
            // Reconnect if disconnected
            if (ipcClient != null && !ipcClient.isConnected()) {
                System.out.println("[PluginRPC] Attempting to reconnect...");
                ipcClient.close();
                ipcClient = new DiscordIPCClient(DiscordRPCSettings.getApplicationId());
                if (ipcClient.connect()) {
                    updateUIStatus("Reconnected to Discord");
                }
            }
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

            ipcClient = new DiscordIPCClient(applicationId);
            if (ipcClient.connect()) {
                updateUIStatus("Connected to Discord");
                startTimestamp = System.currentTimeMillis() / 1000;
                System.out.println("[PluginRPC] Discord IPC initialized with Application ID: " + applicationId);
                updateRCP(true);
            } else {
                updateUIStatus("Could not connect to Discord. Is Discord running?");
            }
        } catch (Exception e) {
            System.err.println("[PluginRPC] Failed to initialize Discord IPC: " + e.getMessage());
            updateUIStatus("Failed to initialize: " + e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    private void updateRCP(Boolean timestamp) {
        if (ipcClient == null || !ipcClient.isConnected()) {
            return;
        }

        try {
            if (timestamp != null && timestamp) {
                startTimestamp = System.currentTimeMillis() / 1000;
            }

            String fileName = detectCurrentFile();
            String projectName = detectCurrentProject();
            String fileType = detectFileType();

            currentProject = projectName;
            currentFile = fileName;
            currentFileType = fileType;

            String details;
            if (DiscordRPCSettings.isShowProject() && projectName != null) {
                details = projectName;
            } else {
                details = "Working in NetBeans IDE";
            }

            String state;
            if (DiscordRPCSettings.isShowFile() && fileName != null) {
                state = "Editing " + fileName;
            } else {
                state = "Idle";
            }

            String smallImageKey = null;
            String smallImageText = null;
            if (fileType != null) {
                smallImageKey = getAssetKeyForFileType(fileType);
                smallImageText = "Programming in " + fileType;
            }

            long ts = DiscordRPCSettings.isShowTimestamp() ? startTimestamp : 0;

            ipcClient.updatePresence(
                    details, state,
                    "first", "NetBeans IDE",
                    smallImageKey, smallImageText,
                    ts
            );

            updateUIPresence();
        } catch (Exception e) {
            System.err.println("[PluginRPC] Failed to update Discord presence: " + e.getMessage());
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

    public void shutdown() {
        running = false;
        if (registryListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(registryListener);
        }
        if (ipcClient != null) {
            ipcClient.close();
            ipcClient = null;
        }
        updateUIStatus("Discord RPC shutdown complete");
        System.out.println("[PluginRPC] Discord RPC shutdown complete");
    }
}
