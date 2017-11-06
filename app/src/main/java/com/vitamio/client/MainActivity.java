package com.vitamio.client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.FileUtils;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends Activity implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener{
    /**
     * TAG
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * View
     */
    private ProgressBar mProgress;
    private TextView downloadRateView, loadRateView;
    private VideoView mVideoView;
    /**
     * params
     */
    private MediaPlayerController mMediaPlayer;
    private String path = "http://baobab.wdjcdn.com/145076769089714.mp4";
    private Uri uri;
    private static final int TYPE_BATTERY = 0x0001;
    private static final int TYPE_TIME = 0x0002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_main);
        initView();
        initData();
        registerBoradcastReceiver();
    }

    private void initWindow(){
        //自定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = MainActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        //必须写这个，初始化加载库文件
        Vitamio.isInitialized(this);
        //设置视频解码监听
        toggleHideyBar();
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
    }

    public void toggleHideyBar(){
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

    }


    private void initView(){
        mVideoView = (VideoView) findViewById(R.id.buffer);
        mMediaPlayer = new MediaPlayerController(this, mVideoView, this);
        mMediaPlayer.setVideoName("白火锅 x 红火锅");
        mProgress = (ProgressBar) findViewById(R.id.probar);
        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
    }


    private void initData(){
        uri = Uri.parse(path);
        mVideoView.setVideoURI(uri);//设置视频播放地址
        mMediaPlayer.show(5000);
        mMediaPlayer.setOnItemClickListener(mListener);
        mVideoView.setMediaController(mMediaPlayer);
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);//画质  标清
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
        new Thread(mRunnable).start();
    }

    /**
     * 更新缓存进度
     * @param mp
     * @param percent
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

    /**
     * 获取下载速度
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()){
                    mVideoView.pause();
                    mProgress.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                mProgress.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s"+ "  ");
        }

        return true;
    }

    /**
     * 横屏切换设置全屏
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mVideoView != null){
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

    private MediaPlayerController.OnItemClickListener mListener = new MediaPlayerController.OnItemClickListener() {
        @Override
        public void itemClick(View view) {
            switch (view.getId()){
                case R.id.mediacontroller_quality1:
                    break;
            }
        }
    };

    public void registerBoradcastReceiver(){
        //注册电量广播监听
        IntentFilter intent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryBroadcastReceiver, intent);
    }

    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                //把它转成百分比
                Message msg = new Message();
                msg.obj = (level * 100) / scale + "";
                msg.what = TYPE_BATTERY;
                mHandler.sendMessage(msg);
            }
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TYPE_BATTERY:
                    mMediaPlayer.setBattery(msg.obj.toString());
                    break;
                case TYPE_TIME:
                    mMediaPlayer.setTime(msg.obj.toString());
                    break;
            }
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");
                String date = mFormat.format(new Date());
                Message msg = new Message();
                msg.obj = date;
                msg.what = TYPE_TIME;
                mHandler.sendMessage(msg);
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(batteryBroadcastReceiver);
        } catch (IllegalArgumentException ex) {

        }
    }


    /** 扫描SD卡 */
//    private class ScanVideoTask extends AsyncTask<Void, File, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            eachAllMedias(Environment.getExternalStorageDirectory());
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(File... values) {
//            mAdapter.add(values[0]);
//            mAdapter.notifyDataSetChanged();
//        }
//
//        /** 遍历所有文件夹，查找出视频文件 */
//        public void eachAllMedias(File f) {
//            if (f != null && f.exists() && f.isDirectory()) {
//                File[] files = f.listFiles();
//                if (files != null) {
//                    for (File file : f.listFiles()) {
//                        if (file.isDirectory()) {
//                            eachAllMedias(file);
//                        } else if (file.exists() && file.canRead() && FileUtils.isVideoOrAudio(file)) {
//                            publishProgress(file);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
