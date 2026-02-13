package fr.pedrokarim.netbeansrpc;
/** Localizable strings for {@link fr.pedrokarim.netbeansrpc}. */
class Bundle {
    /**
     * @return <i>Discord Rich Presence</i>
     * @see DiscordRPCPanel
     */
    static String CTL_DiscordRPCPanel() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_DiscordRPCPanel");
    }
    /**
     * @return <i>Discord Rich Presence Configuration</i>
     * @see DiscordRPCPanel
     */
    static String HINT_DiscordRPCPanel() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "HINT_DiscordRPCPanel");
    }
    private Bundle() {}
}
