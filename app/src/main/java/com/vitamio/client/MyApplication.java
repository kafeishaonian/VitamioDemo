package com.vitamio.client;

import android.app.Application;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by Hongmingwei on 2017/11/6.
 * Email: 648600445@qq.com
 */

public class MyApplication extends Application {

    private static MyApplication instance = null;

    private MediaPlayer mediaPlayer;

    public static MyApplication getInstance(){
        return  instance ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        Vitamio.isInitialized(getApplicationContext());
    }

}
