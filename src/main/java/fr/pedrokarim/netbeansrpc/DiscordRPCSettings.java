package fr.pedrokarim.netbeansrpc;

import java.util.prefs.Preferences;

/**
 * Manages Discord RPC settings using Java Preferences API
 */
public class DiscordRPCSettings {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(DiscordRPCSettings.class);
    
    private static final String KEY_ENABLED = "discord.rpc.enabled";
    private static final String KEY_APP_ID = "discord.app.id";
    private static final String KEY_SHOW_PROJECT = "discord.show.project";
    private static final String KEY_SHOW_FILE = "discord.show.file";
    private static final String KEY_SHOW_TIMESTAMP = "discord.show.timestamp";
    
    // Default values
    private static final String DEFAULT_APP_ID = "621768079386345477";
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_SHOW_PROJECT = true;
    private static final boolean DEFAULT_SHOW_FILE = true;
    private static final boolean DEFAULT_SHOW_TIMESTAMP = true;
    
    /**
     * Check if Discord RPC is enabled
     */
    public static boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }
    
    /**
     * Enable or disable Discord RPC
     */
    public static void setEnabled(boolean enabled) {
        prefs.putBoolean(KEY_ENABLED, enabled);
    }
    
    /**
     * Get the Discord Application ID
     */
    public static String getApplicationId() {
        return prefs.get(KEY_APP_ID, DEFAULT_APP_ID);
    }
    
    /**
     * Set the Discord Application ID
     */
    public static void setApplicationId(String appId) {
        if (appId != null && !appId.trim().isEmpty()) {
            prefs.put(KEY_APP_ID, appId.trim());
        }
    }
    
    /**
     * Check if project name should be shown
     */
    public static boolean isShowProject() {
        return prefs.getBoolean(KEY_SHOW_PROJECT, DEFAULT_SHOW_PROJECT);
    }
    
    /**
     * Set whether to show project name
     */
    public static void setShowProject(boolean show) {
        prefs.putBoolean(KEY_SHOW_PROJECT, show);
    }
    
    /**
     * Check if file name should be shown
     */
    public static boolean isShowFile() {
        return prefs.getBoolean(KEY_SHOW_FILE, DEFAULT_SHOW_FILE);
    }
    
    /**
     * Set whether to show file name
     */
    public static void setShowFile(boolean show) {
        prefs.putBoolean(KEY_SHOW_FILE, show);
    }
    
    /**
     * Check if timestamp should be shown
     */
    public static boolean isShowTimestamp() {
        return prefs.getBoolean(KEY_SHOW_TIMESTAMP, DEFAULT_SHOW_TIMESTAMP);
    }
    
    /**
     * Set whether to show timestamp
     */
    public static void setShowTimestamp(boolean show) {
        prefs.putBoolean(KEY_SHOW_TIMESTAMP, show);
    }
    
    /**
     * Save all settings (flush to disk)
     */
    public static void saveSettings() {
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Failed to save Discord RPC settings: " + e.getMessage());
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    public static void resetToDefaults() {
        setEnabled(DEFAULT_ENABLED);
        setApplicationId(DEFAULT_APP_ID);
        setShowProject(DEFAULT_SHOW_PROJECT);
        setShowFile(DEFAULT_SHOW_FILE);
        setShowTimestamp(DEFAULT_SHOW_TIMESTAMP);
        saveSettings();
    }
}
