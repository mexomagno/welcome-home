package org.duckdns.mexomagno.mpdwelcome;

import android.content.Context;
import android.os.Handler;
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
}
