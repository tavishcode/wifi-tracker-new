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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private File file;
    private BroadcastReceiver wifiScanReceiver;
    private Button startScan;
    private ListView list;
    final int PERMISSION=1;
    private Boolean isReceiverRegistered=false;
    private Boolean isScanManuallyTriggered= false;
    private JSONObject currentCoordinates=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "localizationData.txt");
        if(arePermissionsGranted())
        {
            Log.i("Main Activity","Permissions were Granted!");
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
        if(isReceiverRegistered)
        {
            unregisterReceiver(wifiScanReceiver);
            isReceiverRegistered=false;
        }
        super.onPause();
    }

    protected void onResume()
    {
        if(!isReceiverRegistered)
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
                isScanManuallyTriggered=true;
                ApiClient.getCoordinates().enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("Main Activity","Failed to get coordinates");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try
                        {
                            Log.i("Main Activity","Sent request for location");
                            String res= response.body().string();
                            Log.i("Main Activity",res);
                            currentCoordinates = new JSONObject(res);
                            wifiManager.startScan();
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        list= (ListView)findViewById(R.id.list);
        wifiScanReceiver= new BroadcastReceiver()
        {
            public void onReceive(Context c, Intent intent)
            {
                if(isScanManuallyTriggered)
                {
                    List<ScanResult> wifiScanList = wifiManager.getScanResults();
                    Log.i("Main Activity",String.valueOf(wifiScanList.size()));
                    ListAdapter adapter = new ListAdapter(MainActivity.this, wifiScanList);
                    list = (ListView) findViewById(R.id.list);
                    list.setAdapter(adapter);
                    FileOutputStream fOut = null;
                    try
                    {
                        fOut = new FileOutputStream(file,true);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                        long timestamp = System.currentTimeMillis();
                        for(int i=0;i<wifiScanList.size();i++)
                        {
                            ScanResult result= wifiScanList.get(i);

                            if(currentCoordinates!=null)
                            {
                                outputStreamWriter.append(timestamp + " " + result.BSSID
                                        + " " + currentCoordinates.getDouble("x")+ " "
                                        + currentCoordinates.getDouble("y") + " " + result.level +"\n");
                            }
                            else
                            {
                                outputStreamWriter.append(timestamp + " " + result.BSSID
                                        + " 1.0 " + "2.0 " + result.level +"\n");
                            }
                        }
                        outputStreamWriter.close();
                        fOut.flush();
                        fOut.close();
                        Toast.makeText(MainActivity.this,"Scan Completed",Toast.LENGTH_SHORT).show();
                    }
                    catch (JSONException | IOException e)
                    {
                        e.printStackTrace();
                    }
                    isScanManuallyTriggered=false;
                }
            }
        };
        if(!isReceiverRegistered)
        {
            registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isReceiverRegistered=true;
        }
    }

    private boolean arePermissionsGranted() {
        Log.i("Main Activity","Listing Permissions");
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
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION);
            Log.i("Main Activity",listPermissionsNeeded.toString());
            return false;
        }
        return true;
    }
}
