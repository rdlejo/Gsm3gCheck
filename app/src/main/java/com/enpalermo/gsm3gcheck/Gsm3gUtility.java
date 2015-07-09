package com.enpalermo.gsm3gcheck;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;


/**
 * Created by Ricardo on 27/04/2015.
 */
public class Gsm3gUtility {



    private static ArrayList<String> logs=new ArrayList<String>();



    private static long lastExec=-1;
    private static String codesetnetpref=null;

    private static int execs=0;
    public static final String APP_TAG="Gsm3gCheck";

    private static RootCommand Cmd=new RootCommand();


    public static boolean canSU(boolean force_check){
        return Cmd.canSU(force_check);
    }

    public static NetworkInfo getNetworkInfoMobile(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }



    public static void Log(LogType t,String s){
        switch (t)
        {
            case Debug:
                Log.d(APP_TAG,s);
                break;
            case Error:
                Log.e(APP_TAG, s);
                break;
            case Info:
                Log.i(APP_TAG, s);
                break;
            default:
                Log.v(APP_TAG, s);
                return;
        }
        synchronized(logs)
        {
            logs.add(s);
            if(logs.size()>60)
            {
                for(int i=0;i<20;i++)
                    logs.remove(0);
            }
        }
    }

    public static String getLogs(int max)
    {
        Object[] objectList = logs.toArray();
        int l=objectList.length;

        String[] strArr = Arrays.copyOf(objectList, l, String[].class);

        StringBuilder sb = new StringBuilder();

        int lineas=0;
        for(int i=(l-1);i>=0;i--)
        {
            if(lineas++>=max)
                break;
            sb.append(strArr[i]);
            sb.append('\n');
        }
        return sb.toString();

    }

    public static String getInfoString(Context context){
        StringBuilder sb=new StringBuilder();

        String api = getPreferredNetworkTypeCode(context);

        boolean root=canSU(true);

        if(api.length()==0)
            sb.append("Error: API No detected!!\nReport to developer!\n\n");
        else if(!root)
            sb.append("Warning: This APP require ROOT access!\n\n");

        sb.append("ROOT: ");
        sb.append(root);
        sb.append("\n\n");


        NetworkInfo info= getNetworkInfoMobile(context);


        if(info==null){
            sb.append("Connection Mobile is NULL\n");

        }
        else {
            sb.append("Connection Mobile (");
            sb.append(info.getTypeName());
            sb.append(")\n");

            sb.append("State: ");
            sb.append(info.getState().toString());
            sb.append(" ");
            sb.append(info.getDetailedState().toString());
            sb.append("\n");
            sb.append("Subtype: ");
            sb.append(info.getSubtype());
            sb.append(" ");
            sb.append(info.getSubtypeName());
            sb.append("\n");
            sb.append("Available: ");
            sb.append(info.isAvailable());
            sb.append("\n");
            sb.append("Connected: ");
            sb.append(info.isConnected());
            sb.append("\n\n");
        }

        info=getNetworkInfo(context);

        if(info==null){
            sb.append("Connection Active is NULL\n");

        }
        else {
            sb.append("Connection Active (");
            sb.append(info.getTypeName());
            sb.append(")\n");

            sb.append("State: ");
            sb.append(info.getState().toString());
            sb.append(" ");
            sb.append(info.getDetailedState().toString());
            sb.append("\n");
            sb.append("Subtype: ");
            sb.append(info.getSubtype());
            sb.append(" ");
            sb.append(info.getSubtypeName());
            sb.append("\n");
            sb.append("Available: ");
            sb.append(info.isAvailable());
            sb.append("\n");
            sb.append("Connected: ");
            sb.append(info.isConnected());
            sb.append("\n\n");
        }

        sb.append("API setPreferredNetworkType: ");
        sb.append(api);
        sb.append('\n');

        sb.append("getprop gsm.network.type: ");
        sb.append(getPropGsmNetworkType());
        sb.append('\n');

        return sb.toString();

    }

    private static String getPropGsmNetworkType(){
        RootCommand.CommandResult result=Cmd.sh.runWaitFor("getprop gsm.network.type",true);
        return result.success()?result.stdout.trim():"";
    }

    public static int resetIfNeed(Context context, boolean forced){

        String codeAPI=getPreferredNetworkTypeCode(context);
        if(codeAPI.length()==0 || !canSU(false))
            return -1;

        //Log(LogType.Info,"Check Start...");
        if(!forced)
        {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int callState=tm.getCallState();

            if(callState != TelephonyManager.CALL_STATE_IDLE){
                Log(LogType.Info, "Event cancel (call state " + Integer.toString(callState) + "): " + new Date().toString());
                return -1;
            }



            NetworkInfo info= getNetworkInfoMobile(context);
            if(info==null || !info.isAvailable()) {
                Log(LogType.Error, "No mobile network detected!");

                return -1;
            }

            if(info.isConnected())
                return -1;

        }

        PowerManager.WakeLock mWakeLock = null;

        long cMilis=System.currentTimeMillis();
        String netType=getPropGsmNetworkType();
        Log(LogType.Info, "gsm.network.type: " + netType);

        if(forced || netType.compareToIgnoreCase("Unknown")==0 || (forced=(lastExec!=-1 && (cMilis-lastExec)>=Gsm3gSetup.FORCE_INTERVAL)))
        {
            // obtain the wake lock
            PowerManager pm = (PowerManager) context.getSystemService(Service.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, APP_TAG);
            mWakeLock.acquire();

            String report="NetType: "+netType+". Switch 2G-3G complete " + (forced ? "(Force)" : "") + ": " + Integer.toString(++execs);
            String command="su -c 'service call phone "+codeAPI+" i32 1 && sleep 2 && service call phone "+codeAPI+" i32 0 && echo \"`date` "+report+"\" >>/sdcard/Gsm3gCheck.log'";
            RootCommand.CommandResult result=Cmd.sh.runWaitFor(command,false);
            Log(LogType.Info, report);
            forced=result.success();
        }
        if(forced || lastExec==-1)
            lastExec=cMilis;
        if(mWakeLock!=null)
            mWakeLock.release();
        return forced?execs:-1;
    }

    public static String getPreferredNetworkTypeCode(Context context) {
        if(codesetnetpref==null)
        {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                Class ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                Class stub = ITelephonyClass.getDeclaringClass();
                Field field=null;
                int fnl=10000;
                Field[] fields=stub.getDeclaredFields();

                for(Field f:fields)
                {
                    String s=f.getName();
                    int sl=s.length();

                    if(sl>=35 && s.substring(0,35).equals("TRANSACTION_setPreferredNetworkType"))
                    {
                        if(sl<fnl) {
                            field = f;
                            if(sl==35)
                                break;
                            fnl=sl;
                        }
                    }
                }

                if(field!=null)
                {
                    field.setAccessible(true);
                    codesetnetpref = String.valueOf(field.getInt(null));
                    Log(LogType.Info, "API (" + field.getName() + "): " + codesetnetpref);

                }
                else {
                    codesetnetpref = "";
                    Log(LogType.Error, "API setPreferredNetworkType no detected!");

                }

            } catch (Exception e) {
                Log(LogType.Error, "Exception on detect setPreferredNetworkType: "+e.getMessage());
                codesetnetpref = "";
            }

        }
        return codesetnetpref;

    }
}
