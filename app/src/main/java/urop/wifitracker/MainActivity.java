package urop.wifitracker;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BroadcastReceiver wifiScanReceiver;
    Button startScan;
    ListView list;
    final int LOCATION_PERMISSION=1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
        else
        {
            setupWifiTracker();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
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
            }
        };
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
}
