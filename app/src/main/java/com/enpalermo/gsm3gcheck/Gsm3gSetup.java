package com.enpalermo.gsm3gcheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

/**
 * Created by Ricardo on 27/04/2015.
 */
public class Gsm3gSetup extends BroadcastReceiver {


    private static long startSession=-1;
    private static int events=1;
    private static long lastEvent=-1;
    private static final int EXEC_INTERVAL = 7 * 60 * 1000;
    public static final int FORCE_INTERVAL = 420 * 60 * 1000;


    @Override
    public void onReceive(final Context ctx, final Intent intent) {

        // Si no existe acceso a la API no tiene sentido programar eventos...
        if(Gsm3gUtility.getPreferredNetworkTypeCode(ctx).length()==0)
            return;

        long cMillis=System.currentTimeMillis();

        boolean inicioSession;
        if(startSession==-1)
        {
            Intent eventService = new Intent(ctx, Gsm3gService.class);
            ctx.startService(eventService);
            startSession=cMillis;
            inicioSession=true;
        }
        else {

            if((cMillis-startSession)<180000 || (cMillis-lastEvent)<30000)
                return;
            Gsm3gUtility.Log(LogType.Info,"Gsm3gSetup: "+Integer.toString(++events));

            inicioSession=false;
        }
        lastEvent=cMillis;
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, Gsm3gEvent.class); // explicit
        // intent
        PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i,0);

        if(!inicioSession)
            alarmManager.cancel(intentExecuted);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND,inicioSession?120:10);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,now.getTimeInMillis(), EXEC_INTERVAL, intentExecuted);
    }
}
