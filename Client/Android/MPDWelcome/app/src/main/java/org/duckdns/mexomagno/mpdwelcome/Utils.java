package org.duckdns.mexomagno.mpdwelcome;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mexomagno on 25-07-2016.
 */
public class Utils {
    static public void sendToast(final Context ctx, final String message){
        Handler h = new Handler(ctx.getMainLooper());
        // Although you need to pass an appropriate context
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    static public void updateStatus(final Context ctx, final String message){
        // Create broadcast
        Intent i = new Intent(Constants.ACTION_UPDATE_STATUS);
        i.putExtra(Constants.EXTRA_NEW_STATUS, message);
        ctx.sendBroadcast(i);
    }
    /*static public void getDistanceToRouter(final Context ctx){

        double freqInMHz = ctx.
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }*/
    static public int getWifiSignalLevel(final Context ctx){
        WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        return wm.calculateSignalLevel(wi.getRssi(), 100);
    }
}
