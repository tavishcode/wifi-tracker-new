package urop.wifitracker;

import android.content.Context;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for SD Card presence and if the memory is SD card (not phone memory)
        if (!EnvironmentCompat.getStorageState(new File("/storage/extSdCard")).equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "Error! No SDCARD Found!", Toast.LENGTH_LONG).show();
        } else {
            file = new File(Environment.getExternalStorageDirectory() + "/UROP-WIFI-TRACKER", "localizationData.txt");
        }

    }

    public void writeToFile(String macAddress, float x, float y, int wifiSignalStrength){
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
            outputStreamWriter.write(macAddress + " " + x + " " + y + " " + wifiSignalStrength);
            outputStreamWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
}
