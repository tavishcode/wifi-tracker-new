package urop.wifitracker;

import android.os.Bundle;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    File file;
    BroadcastReceiver wifiScanReceiver;
    Button startScan;
    ListView list;
    final int PERMISSION=1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check for SD Card presence and if the memory is SD card (not phone memory)
        /*if (!EnvironmentCompat.getStorageState(new File("/storage/extSdCard")).equalsIgnoreCase(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(getApplicationContext(), "Error! No SDCARD Found!", Toast.LENGTH_LONG).show();
        }
        else
        {*/
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "localizationData.txt");
        //}
        if(arePermissionsGranted())
        {
            setupWifiTracker();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    setupWifiTracker();
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Permission was denied!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    protected void onPause()
    {
        unregisterReceiver(wifiScanReceiver);
        super.onPause();
    }

    protected void onResume()
    {
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    void setupWifiTracker()
    {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        startScan= (Button)findViewById(R.id.startScan);
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.startScan();
            }
        });
        list= (ListView)findViewById(R.id.list);
        wifiScanReceiver= new BroadcastReceiver()
        {
            public void onReceive(Context c, Intent intent)
            {
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                ListAdapter adapter = new ListAdapter(MainActivity.this, wifiScanList);
                list = (ListView) findViewById(R.id.list);
                list.setAdapter(adapter);
                FileOutputStream fOut = null;
                try
                {
                    fOut = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                    for(int i=0;i<wifiScanList.size();i++)
                    {
                        ScanResult result= wifiScanList.get(i);
                        outputStreamWriter.append(result.BSSID+ " " + 1.0 + " " + 2.0 + " " + result.level +"\n");
                    }
                    outputStreamWriter.close();
                    fOut.flush();
                    fOut.close();
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }

    public void writeToFile(String macAddress, float x, float y, int wifiSignalStrength){
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
            outputStreamWriter.append(macAddress + " " + x + " " + y + " " + wifiSignalStrength+"\n");
            outputStreamWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private boolean arePermissionsGranted() {
        int writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int readPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        int locationPermission= ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION);
            return false;
        }
        return true;
    }
}
