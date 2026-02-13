package fr.pedrokarim.netbeansrpc;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Discord RPC Configuration and Status Panel
 */
@TopComponent.Description(
        preferredID = "DiscordRPCPanel",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "fr.pedrokarim.netbeansrpc.DiscordRPCPanel"
)
@ActionReference(
        path = "Menu/Window",
        position = 333
)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DiscordRPCPanel",
        preferredID = "DiscordRPCPanel"
)
@Messages({
    "CTL_DiscordRPCPanel=Discord Rich Presence",
    "HINT_DiscordRPCPanel=Discord Rich Presence Configuration"
})
public final class DiscordRPCPanel extends TopComponent {

    private JTextField appIdField;
    private JCheckBox enabledCheckBox;
    private JCheckBox showProjectCheckBox;
    private JCheckBox showFileCheckBox;
    private JCheckBox showTimestampCheckBox;
    private JTextArea statusArea;
    private JLabel statusLabel;

    public DiscordRPCPanel() {
        initComponents();
        setName(Bundle.CTL_DiscordRPCPanel());
        setToolTipText(Bundle.HINT_DiscordRPCPanel());
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel with settings
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("<html><h2>Discord Rich Presence Configuration</h2></html>");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Enabled checkbox
        gbc.gridy++;
        gbc.gridwidth = 2;
        enabledCheckBox = new JCheckBox("Enable Discord Rich Presence", true);
        enabledCheckBox.addActionListener(e -> toggleRPC());
        mainPanel.add(enabledCheckBox, gbc);

        // Application ID
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel appIdLabel = new JLabel("Discord Application ID:");
        mainPanel.add(appIdLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        appIdField = new JTextField(DiscordRPCSettings.getApplicationId(), 20);
        mainPanel.add(appIdField, gbc);

        // Show project name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        showProjectCheckBox = new JCheckBox("Show project name", DiscordRPCSettings.isShowProject());
        mainPanel.add(showProjectCheckBox, gbc);

        // Show file name
        gbc.gridy++;
        showFileCheckBox = new JCheckBox("Show file name", DiscordRPCSettings.isShowFile());
        mainPanel.add(showFileCheckBox, gbc);

        // Show timestamp
        gbc.gridy++;
        showTimestampCheckBox = new JCheckBox("Show elapsed time", DiscordRPCSettings.isShowTimestamp());
        mainPanel.add(showTimestampCheckBox, gbc);

        // Buttons
        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveSettings());
        mainPanel.add(saveButton, gbc);

        gbc.gridx = 1;
        JButton reconnectButton = new JButton("Reconnect to Discord");
        reconnectButton.addActionListener(e -> reconnect());
        mainPanel.add(reconnectButton, gbc);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));

        statusLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        statusArea = new JTextArea(10, 40);
        statusArea.setEditable(false);
        statusArea.setText("Discord Rich Presence for NetBeans IDE\n\nWaiting for updates...");
        JScrollPane scrollPane = new JScrollPane(statusArea);
        statusPanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main component
        add(mainPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel infoLabel = new JLabel("<html><small>Configure your Discord Application at: "
                + "<a href='https://discord.com/developers/applications'>Discord Developer Portal</a></small></html>");
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void toggleRPC() {
        boolean enabled = enabledCheckBox.isSelected();
        DiscordRPCSettings.setEnabled(enabled);
        
        if (enabled) {
            Installer.restartRPC();
            updateStatus("Discord RPC enabled");
        } else {
            Installer.stopRPC();
            updateStatus("Discord RPC disabled");
        }
    }

    private void saveSettings() {
        String newAppId = appIdField.getText().trim();
        if (!newAppId.isEmpty()) {
            DiscordRPCSettings.setApplicationId(newAppId);
        }
        
        DiscordRPCSettings.setShowProject(showProjectCheckBox.isSelected());
        DiscordRPCSettings.setShowFile(showFileCheckBox.isSelected());
        DiscordRPCSettings.setShowTimestamp(showTimestampCheckBox.isSelected());
        DiscordRPCSettings.saveSettings();
        
        updateStatus("Settings saved successfully");
        
        // Restart RPC if enabled
        if (DiscordRPCSettings.isEnabled()) {
            reconnect();
        }
    }

    private void reconnect() {
        updateStatus("Reconnecting to Discord...");
        Installer.restartRPC();
        updateStatus("Reconnection initiated");
    }

    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("Status: " + message);
        }
        if (statusArea != null) {
            String currentText = statusArea.getText();
            statusArea.setText(currentText + "\n[" + new java.util.Date() + "] " + message);
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        }
    }

    public void updatePresenceInfo(String project, String file, String language) {
        StringBuilder info = new StringBuilder();
        info.append("Current Status:\n");
        info.append("  Project: ").append(project != null ? project : "N/A").append("\n");
        info.append("  File: ").append(file != null ? file : "N/A").append("\n");
        info.append("  Language: ").append(language != null ? language : "N/A").append("\n");
        
        if (statusArea != null) {
            String currentText = statusArea.getText();
            if (currentText.contains("Current Status:")) {
                // Replace the last status update
                int lastIndex = currentText.lastIndexOf("Current Status:");
                if (lastIndex > 0) {
                    currentText = currentText.substring(0, lastIndex);
                }
            }
            statusArea.setText(currentText + "\n" + info.toString());
        }
    }

    public static DiscordRPCPanel getInstance() {
        return (DiscordRPCPanel) TopComponent.getRegistry().getOpened()
                .stream()
                .filter(tc -> tc instanceof DiscordRPCPanel)
                .findFirst()
                .orElse(null);
    }
}
