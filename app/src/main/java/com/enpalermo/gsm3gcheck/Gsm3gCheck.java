package com.enpalermo.gsm3gcheck;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;



public class Gsm3gCheck extends Activity {

    private String versionName="";
    private String packageName="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsm3g_check);
        refreshInfo(false,false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gsm3g_check, menu);
        return true;
    }

    public void refreshInfo(boolean force,boolean log) {

        TextView t = (TextView) findViewById(R.id.textView);
        if(force)
            Gsm3gUtility.resetIfNeed(getBaseContext(),force);

        if(log)
            t.setText(Gsm3gUtility.getLogs(20));
        else
            t.setText(Gsm3gUtility.getInfoString(getBaseContext()));

    }

    public void about(){
        String versionName="";
        int versionNumber=1;
        String packageName=this.getPackageName();
        try {
            PackageManager pm=getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(packageName, 0);
            versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;

        }
        catch (PackageManager.NameNotFoundException e)
        {}

        String about=packageName + "\n\n" + "Version: "+versionName + "\nBuild: "+Integer.toString(versionNumber)+"\n\nAuthor:\nRicardo Lejovitzky\nwww.enpalermo.com\ninfo@enpalermo.com\n";
        TextView t = (TextView) findViewById(R.id.textView);
        t.setText(about);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            refreshInfo(false,false);
            return true;
        }
        if(id == R.id.action_force)
        {
            refreshInfo(true,false);
            return true;
        }
        if(id == R.id.action_log)
        {
            refreshInfo(false,true);
            return true;
        }
        if(id == R.id.action_about)
        {
            about();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
