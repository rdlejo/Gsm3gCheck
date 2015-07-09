package com.enpalermo.gsm3gcheck;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by Ricardo on 27/04/2015.
 */
public class Gsm3gService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags,final int startId) {
        Gsm3gUtility.Log(LogType.Info,"Service Start");
        return Service.START_STICKY;
    }

}
