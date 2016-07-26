package org.duckdns.mexomagno.mpdwelcome;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by mexomagno on 25-07-2016.
 */
public class ServerDiscover extends Service {
    static private final String TAG = ServerDiscover.class.getSimpleName();
    // actions to broadcast
    static public final String ACTION_SERVER_FOUND = "org.duckdns.mexomagno.mpdwelcome.action.ACTION_SERVER_FOUND";
    static public final String ACTION_SERVER_NOT_FOUND = "org.duckdns.mexomagno.mpdwelcome.action.ACTION_SERVER_FOUND";
    static public final String EXTRA_SERVER_IP = "org.duckdns.mexomagno.mpdwelcome.action.EXTRA_SERVER_IP";
    static public final String EXTRA_SERVER_PORT = "org.duckdns.mexomagno.mpdwelcome.action.EXTRA_SERVER_PORT";

    //-------------
    static private final int SEARCH_TIMEOUT = 10000;
    static private final int BUFFER_SIZE = 1024;
    private final String BROADCAST_IP = "192.168.0.255";
    private final int BROADCAST_PORT = 50000;
    private final String SECRET = "laminatenicida";
    private Context this_context;
    private DatagramSocket socket;
    private String udp_result;
    private Thread networking_thread;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");
        this_context = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bound");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "startCommand received");
        // Search for server
        Log.d(TAG, "Attempting to search for server...");
        String server = searchServer();
        Log.d(TAG, "Server: " + (server == null ? "null" : server));
        Intent server_ip_intent = new Intent(server == null ? ACTION_SERVER_NOT_FOUND : ACTION_SERVER_FOUND);
        if (server != null){
            String host = server.split(":")[0];
            String port = server.split(":")[1];
            server_ip_intent.putExtra(EXTRA_SERVER_IP, host);
            server_ip_intent.putExtra(EXTRA_SERVER_PORT, port);
        }
        sendBroadcast(server_ip_intent);
        stopSelf();
        return Service.STOP_FOREGROUND_REMOVE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
        Utils.sendToast(this, "Adiós!");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbinded");
        return super.onUnbind(intent);
    }

    // ----------- Service logic -----------------

    /**
     * Search for server broadcast messages
     * @return
     */
    private String searchServer() {
        // Create networking thread
        networking_thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    InetAddress broadcast_ip = InetAddress.getByName(BROADCAST_IP);
                    byte[] recvbuff = new byte[BUFFER_SIZE];
                    if (socket == null || socket.isClosed()) {
                        socket = new DatagramSocket(BROADCAST_PORT, broadcast_ip);
                        socket.setBroadcast(true);
                    }
                    DatagramPacket packet = new DatagramPacket(recvbuff, BUFFER_SIZE);
                    Log.d(TAG, "Waiting for UDP broadcast...");
                    socket.setSoTimeout(SEARCH_TIMEOUT);
                    boolean ismyservice = false;
                    String sender_ip = null;
                    String message = null;
                    while (!isMyService(message)) {
                        socket.receive(packet);
                        sender_ip = packet.getAddress().getHostAddress();
                        message = new String(packet.getData()).trim();
                    }
                    if (message == null) throw new Exception("This shit should not happen");
                    Log.d(TAG, "'" + (sender_ip == null ? "null" : sender_ip) + "' sent: '" + (message == null ? "null" : message) + "'");
                    socket.close();
                    udp_result = sender_ip + ":" + message.split(":")[1];
                } catch (Exception e) {
                    // Could happen if timeout or other stuff
                    Log.e(TAG, "Error!: " + e);
                    Utils.sendToast(this_context, "Error :( :");
                }
            }
        });
        networking_thread.start();
        try {
            networking_thread.join();
        }catch(InterruptedException e){
            Log.e(TAG, "Error in thread join!!: " + e);
        }
        return udp_result;
    }
    private boolean isMyService(String data){
        if (data == null) return false;
        if (data.split(":").length != 2) return false;
        String possible_secret = data.split(":")[0];
        String possible_port = data.split(":")[1];
        if (!possible_secret.equals(SECRET) || !possible_port.matches("\\d+"))
            return false;
        long wannabe_port = Long.parseLong(possible_port);
        if (wannabe_port > 65535 || wannabe_port < 1024)
            return false;
        return true;
    }
}
