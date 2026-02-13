package fr.pedrokarim.netbeansrpc;

import java.util.Date;
import java.util.Timer;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import org.openide.windows.OnShowing;


/**
 *
 * @author karim
 */

@OnShowing
public class Installer extends ModuleInstall implements Runnable {

    public static final Logger log = Logger.getLogger("Installer");
    private static Timer timer;
    private static RCPSchedule rcpSchedule;

    public Installer() {
        super();
        
        // Only start if enabled in settings
        if (DiscordRPCSettings.isEnabled()) {
            startRPC();
        }
        
        System.out.println("[PluginRPC] NetbeansRPC has loaded.");
    }
    
    private static synchronized void startRPC() {
        if (timer == null) {
            timer = new Timer();
            rcpSchedule = new RCPSchedule();
            timer.scheduleAtFixedRate(rcpSchedule, new Date(), 12000l); // Updates every 12 seconds
            System.out.println("[PluginRPC] Discord RPC started.");
        }
    }
    
    public static synchronized void stopRPC() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (rcpSchedule != null) {
            rcpSchedule.shutdown();
            rcpSchedule = null;
        }
        System.out.println("[PluginRPC] Discord RPC stopped.");
    }
    
    public static synchronized void restartRPC() {
        stopRPC();
        try {
            Thread.sleep(1000); // Wait a bit before restarting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startRPC();
    }

    @Override
    public void run() {
        
    }

    @Override
    public void close() {
        stopRPC();
        System.out.println("[PluginRPC] NetbeansRPC is closed.");
    }
}
