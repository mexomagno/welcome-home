package org.duckdns.mexomagno.mpdwelcome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainScreen extends AppCompatActivity {
    static private final String TAG = MainScreen.class.getSimpleName();
    private Context this_context;
    IntentFilter ui_update_filter;
    BroadcastReceiver ui_update_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = (intent.getAction() == null ? "null" : intent.getAction());
            if (action == Constants.ACTION_UPDATE_STATUS){
                // Get status message
                String status_message = (intent.getStringExtra(Constants.EXTRA_NEW_STATUS));
                status_message = (status_message == null ? "null" : status_message);
                // update
                ((TextView) (findViewById(R.id.text_status))).setText(status_message);
            }
        }
    };
    IntentFilter server_discover_filter;
    BroadcastReceiver server_discover_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = (intent.getAction() == null ? "null" : intent.getAction());
            if (action.equals(ServerDiscover.ACTION_SERVER_FOUND)){
                showLoading(false);
                updateStatus("Server Found!");
                String server_ip = intent.getStringExtra(ServerDiscover.EXTRA_SERVER_IP);
                String server_port = intent.getStringExtra(ServerDiscover.EXTRA_SERVER_PORT);
                // Show server ip and port on screen
                updateServerInfo(server_ip, server_port);
            }
            if (action.equals(ServerDiscover.ACTION_SERVER_NOT_FOUND)){
                showLoading(false);
                updateStatus("Server NOT found :(");
            }
        }
    };
    Intent server_discover_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this_context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        ToggleButton toggle_button = (ToggleButton)findViewById(R.id.toggle_button);
        toggle_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "Button pressed");
                if (isChecked){
                    startService(server_discover_intent);
                    showLoading(true);
                }else{
                    stopService(server_discover_intent);
                    showLoading(false);
                    updateStatus("Doing nothing");
                    updateServerInfo("-", "-");
                }
            }
        });
        // Create ui updater
        ui_update_filter = new IntentFilter();
        ui_update_filter.addAction(Constants.ACTION_UPDATE_STATUS);
        registerReceiver(ui_update_receiver, ui_update_filter);
        updateStatus("Doing nothing");
        showLoading(false);
        // Initialize some variables
        server_discover_filter = new IntentFilter();
        server_discover_filter.addAction(ServerDiscover.ACTION_SERVER_FOUND);
        server_discover_filter.addAction(ServerDiscover.ACTION_SERVER_NOT_FOUND);
        registerReceiver(server_discover_receiver, server_discover_filter);
        // Create service to discover our server's ip
        server_discover_intent = new Intent(this, ServerDiscover.class);
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
        unregisterReceiver(ui_update_receiver);
    }

    /*---------------------- Own Logic -------------------------*/
    private void updateStatus(String s) {
        // Get status textview
        ((TextView) (findViewById(R.id.text_status))).setText(s);
    }
    private void showLoading(boolean show){
        ProgressBar spinner = (ProgressBar)findViewById(R.id.spinner);
        TextView status = (TextView)findViewById(R.id.text_status);
        if (show){
            spinner.setVisibility(View.VISIBLE);
            status.setVisibility(View.GONE);
        }else{
            spinner.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
        }
    }
    private void updateServerInfo(String server_ip, String server_port){
        ((TextView)findViewById(R.id.text_server_ip)).setText(server_ip);
        ((TextView)findViewById(R.id.text_server_port)).setText(server_port);
    }
}
