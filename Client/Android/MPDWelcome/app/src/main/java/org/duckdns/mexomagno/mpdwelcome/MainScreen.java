package org.duckdns.mexomagno.mpdwelcome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainScreen extends AppCompatActivity {
    static private final String TAG = MainScreen.class.getSimpleName();
    IntentFilter server_discover_filter;
    BroadcastReceiver server_discover_receiver;
    Intent server_discover_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        // Initialize some variables
        server_discover_filter = new IntentFilter();
        server_discover_filter.addAction(ServerDiscover.ACTION_SERVER_FOUND);
        server_discover_filter.addAction(ServerDiscover.ACTION_SERVER_NOT_FOUND);
        server_discover_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ServerDiscover.ACTION_SERVER_FOUND)){
                    String server_ip = intent.getStringExtra(ServerDiscover.EXTRA_SERVER_IP);
                    String server_port = intent.getStringExtra(ServerDiscover.EXTRA_SERVER_PORT);
                    // Show server ip on screen
                    TextView text_server_ip = (TextView)findViewById(R.id.text_server_ip);
                    text_server_ip.setText(server_ip);
                    TextView text_server_port = (TextView)findViewById(R.id.text_server_port);
                    text_server_port.setText(server_port);
                }
            }
        };
        registerReceiver(server_discover_receiver, server_discover_filter);
        // Create service to discover our server's ip
        server_discover_intent = new Intent(this, ServerDiscover.class);
        startService(server_discover_intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Kill service for now
        stopService(server_discover_intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(server_discover_receiver);
    }
}
