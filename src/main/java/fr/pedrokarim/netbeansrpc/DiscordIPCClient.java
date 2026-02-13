package fr.pedrokarim.netbeansrpc;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Pure Java Discord IPC client using named pipes (Windows) / Unix sockets.
 * Replaces the native discord-rpc library to avoid classloader issues in NetBeans.
 */
public class DiscordIPCClient implements Closeable {

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;

    private RandomAccessFile pipe;
    private String applicationId;
    private volatile boolean connected = false;

    public DiscordIPCClient(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean connect() {
        for (int i = 0; i < 10; i++) {
            try {
                pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                sendHandshake();
                String response = readResponse();
                if (response != null && response.contains("READY")) {
                    connected = true;
                    System.out.println("[PluginRPC] Connected to Discord IPC pipe " + i);
                    return true;
                }
            } catch (IOException e) {
                // Try next pipe
                closePipe();
            }
        }
        System.err.println("[PluginRPC] Could not connect to Discord IPC");
        return false;
    }

    private void sendHandshake() throws IOException {
        String payload = "{\"v\":1,\"client_id\":\"" + applicationId + "\"}";
        sendFrame(OP_HANDSHAKE, payload);
    }

    public void updatePresence(String details, String state,
                               String largeImageKey, String largeImageText,
                               String smallImageKey, String smallImageText,
                               long startTimestamp) {
        if (!connected || pipe == null) {
            return;
        }

        try {
            StringBuilder activity = new StringBuilder();
            activity.append("{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":")
                    .append(ProcessHandle.current().pid())
                    .append(",\"activity\":{");

            if (details != null) {
                activity.append("\"details\":").append(jsonString(details)).append(",");
            }
            if (state != null) {
                activity.append("\"state\":").append(jsonString(state)).append(",");
            }
            if (startTimestamp > 0) {
                activity.append("\"timestamps\":{\"start\":").append(startTimestamp).append("},");
            }

            activity.append("\"assets\":{");
            if (largeImageKey != null) {
                activity.append("\"large_image\":").append(jsonString(largeImageKey)).append(",");
                activity.append("\"large_text\":").append(jsonString(largeImageText != null ? largeImageText : "")).append(",");
            }
            if (smallImageKey != null) {
                activity.append("\"small_image\":").append(jsonString(smallImageKey)).append(",");
                activity.append("\"small_text\":").append(jsonString(smallImageText != null ? smallImageText : ""));
            }
            // Remove trailing comma if present
            String assetsStr = activity.toString();
            if (assetsStr.endsWith(",")) {
                activity = new StringBuilder(assetsStr.substring(0, assetsStr.length() - 1));
            }

            activity.append("}}},\"nonce\":\"").append(UUID.randomUUID()).append("\"}");

            sendFrame(OP_FRAME, activity.toString());
            // Read response (non-blocking attempt)
            readResponse();
        } catch (IOException e) {
            System.err.println("[PluginRPC] Failed to update presence: " + e.getMessage());
            connected = false;
        }
    }

    public void clearPresence() {
        if (!connected || pipe == null) {
            return;
        }
        try {
            String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
                    + ProcessHandle.current().pid()
                    + "},\"nonce\":\"" + UUID.randomUUID() + "\"}";
            sendFrame(OP_FRAME, payload);
        } catch (IOException e) {
            connected = false;
        }
    }

    private void sendFrame(int opcode, String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(8 + payloadBytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(opcode);
        buffer.putInt(payloadBytes.length);
        buffer.put(payloadBytes);
        pipe.write(buffer.array());
    }

    private String readResponse() {
        try {
            byte[] header = new byte[8];
            pipe.readFully(header);
            ByteBuffer headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            int op = headerBuf.getInt();
            int length = headerBuf.getInt();

            if (length > 0 && length < 65536) {
                byte[] data = new byte[length];
                pipe.readFully(data);
                return new String(data, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // Response read failed
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    private void closePipe() {
        try {
            if (pipe != null) {
                pipe.close();
            }
        } catch (IOException e) {
            // ignore
        }
        pipe = null;
    }

    @Override
    public void close() {
        if (!connected && pipe == null) {
            return;
        }
        if (pipe != null) {
            try {
                // Clear presence before disconnecting (must be done while still "connected")
                clearPresence();
            } catch (Exception e) {
                // ignore
            }
            try {
                sendFrame(OP_CLOSE, "{}");
            } catch (IOException e) {
                // ignore
            }
            closePipe();
        }
        connected = false;
        System.out.println("[PluginRPC] Discord IPC connection closed.");
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
