package org.duckdns.mexomagno.mpdwelcome;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by mexomagno on 25-07-2016.
 *
 * This service constantly looks for the service on the network.
 * When it finds it, it then
 */
public class ServerDiscover extends Service {
    static private final String TAG = ServerDiscover.class.getSimpleName();
    // actions to broadcast
    static public final String ACTION_SERVER_FOUND = "org.duckdns.mexomagno.mpdwelcome.action.ACTION_SERVER_FOUND";
    static public final String ACTION_SERVER_NOT_FOUND = "org.duckdns.mexomagno.mpdwelcome.action.ACTION_SERVER_NOT_FOUND";
    static public final String ACTION_CONNECTED_TO_SERVICE = "org.duckdns.mexomagno.mpdwelcope.action.ACTION_CONNECTED_TO_SERVICE";
    static public final String ACTION_DISCONNECTED_FROM_SERVICE = "org.duckdns.mexomagno.mpdwelcope.action.ACTION_DISCONNECTED_FROM_SERVICE";
    static public final String EXTRA_SERVER_IP = "org.duckdns.mexomagno.mpdwelcome.action.EXTRA_SERVER_IP";
    static public final String EXTRA_SERVER_PORT = "org.duckdns.mexomagno.mpdwelcome.action.EXTRA_SERVER_PORT";

    //-------------
    static private final int SEARCH_TIMEOUT = 10000;
    static private final int BUFFER_SIZE = 1024;
    private final String BROADCAST_IP = "192.168.0.255";
    private final int BROADCAST_PORT = 50000;
    private final String SECRET = "laminatenicida";
    private String SERVICE_IP;
    private String SERVICE_PORT;
    private Thread service_link;
    private Context this_context;
    private DatagramSocket socket;
    private Thread networking_thread;
    private IntentFilter server_data_filter;
    private BroadcastReceiver server_data_receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = (intent.getAction() == null ? "null" : intent.getAction());
            if (action.equals(ACTION_SERVER_FOUND)){
                // Great. Now we start communicating with the service
                SERVICE_IP = intent.getStringExtra(EXTRA_SERVER_IP);
                SERVICE_PORT = intent.getStringExtra(EXTRA_SERVER_PORT);
                Log.d(TAG, "Server Found at " + SERVICE_IP + ":" + SERVICE_PORT);
                Log.d(TAG, "Sending 'hello' to the service");
                service_link.start();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");
        this_context = this;
        // Register broadcast receiver for a server found
        server_data_filter = new IntentFilter();
        server_data_filter.addAction(ACTION_SERVER_FOUND);
        server_data_filter.addAction(ACTION_SERVER_NOT_FOUND);
        registerReceiver(server_data_receiver, server_data_filter);
        // Talker
        service_link = new Thread(){
            @Override
            public void run() {
                try{
                    while(!isInterrupted()){
                        // Talk to service
                        if (Utils.getWifiSignalLevel(this_context) > Constants.MIN_WIFI_STRENGTH_FOR_SENDING){
                            JSONObject message = new JSONObject();
                            try {
                                message.put(Constants.KEY_SECRET, SECRET);
                                message.put(Constants.KEY_USERNAME, "mexomagno");
                                message.put(Constants.KEY_COMMAND, "distance");
                                message.put(Constants.KEY_ARGS, "" + Utils.getWifiSignalLevel(this_context));
                            }catch(JSONException e){
                                Log.e(TAG, "Error: " + e);
                            }
                            sendToService(message);
                        }
                        Thread.sleep(Constants.RESEND_DELAY);
                    }
                }catch(InterruptedException e){
                    Log.e(TAG, "Service link interrupted " + e);
                }
            }
        };
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
        Log.d(TAG, "Starting server search...");
        startSearch();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
        unregisterReceiver(server_data_receiver);
        service_link.interrupt();
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
    private void startSearch() {
        // Create networking thread
        networking_thread = createNetworkingThread();
        networking_thread.start();
        /*try {
            networking_thread.join();
        }catch(InterruptedException e){
            Log.e(TAG, "Error in thread join!!: " + e);
        }
        return udp_result;*/

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
    private Thread createNetworkingThread(){
        return new Thread(new Runnable(){
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
                    //socket.setSoTimeout(SEARCH_TIMEOUT);
                    String sender_ip = null;
                    String message = null;
                    while (!isMyService(message)) {
                        Log.d(TAG, "Waiting for UDP broadcast...");
                        socket.receive(packet);
                        sender_ip = packet.getAddress().getHostAddress();
                        message = new String(packet.getData()).trim();
                    }
                    if (message == null) throw new Exception("This shit should not happen");
                    Log.d(TAG, "'" + (sender_ip == null ? "null" : sender_ip) + "' sent: '" + (message == null ? "null" : message) + "'");
                    socket.close();
                    // Broadcast response
                    String port = message.split(":")[1];
                    Intent intent = new Intent(ACTION_SERVER_FOUND);
                    intent.putExtra(EXTRA_SERVER_IP, sender_ip);
                    intent.putExtra(EXTRA_SERVER_PORT, port);
                    sendBroadcast(intent);
                } catch (Exception e) {
                    // Could happen if timeout or other stuff
                    Log.e(TAG, "Error!: " + e);
                    Utils.sendToast(this_context, "Oops, ocurrió un error");
                }
            }

        });
    }

    private void sendToService(final JSONObject message){
        if (SERVICE_IP == null || SERVICE_PORT == null) {
            Log.e(TAG, "Error: No service has been found yet");
            return;
        }
        final Integer server_port;
        try {
            server_port = Integer.parseInt(SERVICE_PORT);
        }catch (Exception e){
            Log.e(TAG, "Error converting port '" + SERVICE_PORT + "': " + e);
            Log.e(TAG, "Message could not be sent to service");
            return;
        }
        Thread networking_thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SERVICE_IP, server_port);
                    PrintWriter output = new PrintWriter(socket.getOutputStream());
                    output.print(message);
                    output.flush();
                    output.close();
                    socket.close();
                    Log.d(TAG, "Message sent");
                    Utils.updateStatus(this_context, "Connected");
                }catch (Exception e){
                    Log.e(TAG, "Error connecting to service: " + e);
                    Utils.updateStatus(this_context, "Disconnected");
                    return;
                }
            }
        });
        networking_thread.start();
    }
}
