package urop.wifitracker;

import okhttp3.OkHttpClient;

/**
 * Created by tavish on 4/17/17.
 */

public class OkHttpSingleton {

    private static OkHttpSingleton okHttpSingleton;
    private OkHttpClient okHttpClient;

    private OkHttpSingleton(){

        okHttpClient= new OkHttpClient.Builder()
                .build();

    }

    public static OkHttpSingleton getOkHttpInstance() {
        if(okHttpSingleton==null) {

            return new OkHttpSingleton();
        }
        return okHttpSingleton;
    }

    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }

}
