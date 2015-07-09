package com.enpalermo.gsm3gcheck;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by Ricardo on 27/04/2015.
 */
public class Gsm3gEvent extends BroadcastReceiver{


    @Override
    public void onReceive(final Context ctx, final Intent intent) {
        if ((Gsm3gUtility.resetIfNeed(ctx, false) % 2)==0)
            System.gc();
    }

}
