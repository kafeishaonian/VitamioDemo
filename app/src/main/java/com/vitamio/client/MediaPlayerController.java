package com.vitamio.client;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * 视频播放控制器
 * Created by Hongmingwei on 2017/10/31.
 * Email: 648600445@qq.com
 */

public class MediaPlayerController extends MediaController {
    /**
     * TAG
     */
    private static final String TAG = MediaPlayerController.class.getSimpleName();

    /**
     * View
     */
    private ImageButton img_back;//返回键
    private TextView mFileName;//文件名
    private ImageView img_battery;//电池电量显示
    private TextView textViewBattery;//文字显示电池
    private TextView textViewTime;//系统时间显示
    private LinearLayout mediacontroller_quality_ll; //画面质量1
    private TextView textViewQuality1; //画面质量1
    private TextView textViewQuality2; //画面质量2
    private TextView textViewQuality3; //画面质量3
    private ImageView mOperationBg;//提示图片
    private TextView mOperationTv;//提示文字
    private ImageView mIvScale;//缩放图标
    private SeekBar mProgress;
    private RelativeLayout mVolumeBrightnessLayout;

    /**
     * params
     */
    private Context mContext;
    private VideoView mVideoView;
    private Activity mActivity;
    private GestureDetector mGestureDetector;
    //设置mediaController高度为了使横屏时top显示在屏幕顶端
    private int controllerWidth = 0;
    private AudioManager mAudioManager;
    private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
    private static final int HIDEFRAM = 0;
    //最大声音
    private int mMaxVolume;
    // 当前声音
    private int mVolume = -1;
    //当前亮度
    private float mBrightness = -1f;
    //视频名称
    private String videoname;

    private String quality1 = ""; //画面质量1
    private String quality2 = ""; //画面质量2
    private String quality3 = ""; //画面质量3

    private OnItemClickListener onItemClickListener;

    private boolean progress_turn;
    private int progress;

    public interface OnItemClickListener {
        void itemClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public MediaPlayerController(Context context, VideoView videoView, Activity activity) {
        super(context);
        mContext = context;
        mVideoView = videoView;
        mActivity = activity;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        controllerWidth = windowManager.getDefaultDisplay().getWidth();
        mGestureDetector = new GestureDetector(mContext, new MyGestureDetector());
    }

    /**
     * 初始化视图
     * @return
     */
    @Override
    protected View makeControllerView() {
        View mView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.media_player_controller, this);
        mView.setMinimumHeight(controllerWidth);
        initView(mView);
        initData();
        initLinear();
        return mView;
    }

    /**
     * 初始化控件
     * @param mView
     */
    private void initView(View mView){
        img_back = (ImageButton) mView.findViewById(R.id.mediacontroller_top_back);
        img_battery = (ImageView) mView.findViewById(R.id.mediacontroller_imgbattery);
        textViewBattery = (TextView) mView.findViewById(R.id.mediacontroller_battery);
        textViewTime = (TextView) mView.findViewById(R.id.mediacontroller_time);
        mFileName = (TextView) mView.findViewById(R.id.mediacontroller_file_name);
        mIvScale = (ImageView) mView.findViewById(R.id.mediacontroller_scale);

        mediacontroller_quality_ll = (LinearLayout) mView.findViewById(R.id.mediacontroller_quality_ll);
        textViewQuality1 = (TextView) mView.findViewById(R.id.mediacontroller_quality1);
        textViewQuality2 = (TextView) mView.findViewById(R.id.mediacontroller_quality2);
        textViewQuality3 = (TextView) mView.findViewById(R.id.mediacontroller_quality3);
        mProgress = (SeekBar) mView.findViewById(R.id.mediacontroller_seekbar);
        mVolumeBrightnessLayout = (RelativeLayout) mView.findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) mView.findViewById(R.id.operation_bg);
        mOperationTv = (TextView) mView.findViewById(R.id.operation_tv);
        mOperationTv.setVisibility(View.GONE);
    }

    /**
     * 初始化事件点击
     */
    private void initLinear(){
        img_back.setOnClickListener(backListener);
        mIvScale.setOnClickListener(scaleListener);
        textViewQuality1.setClickable(true);
        textViewQuality1.setOnClickListener(backListener);
        textViewQuality2.setClickable(true);
        textViewQuality2.setOnClickListener(backListener);
        textViewQuality3.setClickable(true);
        textViewQuality3.setOnClickListener(backListener);
        mProgress.setOnSeekBarChangeListener(seekListener);
    }

    private void initData(){
        if (mFileName != null) {
            mFileName.setText(videoname);
        }
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }


    /**
     * 按钮事件处理
     */
    private OnClickListener backListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.mediacontroller_top_back:
                    if (mActivity != null){
                        mActivity.finish();
                    }
                    break;
                case R.id.mediacontroller_quality1:
                    if (mActivity != null) {
                        quality1 = textViewQuality1.getText().toString().trim();
                        if (mediacontroller_quality_ll.getVisibility() == View.VISIBLE) {
                            mediacontroller_quality_ll.setVisibility(GONE);
                        } else {
                            mediacontroller_quality_ll.setVisibility(VISIBLE);
                            show(6000);
                        }
                    }
                    break;
                case R.id.mediacontroller_quality2:
                    if (mActivity != null) {
                        quality2 = textViewQuality2.getText().toString().trim();
                        mediacontroller_quality_ll.setVisibility(GONE);
                        qualityChange(v);
                    }
                    break;
                case R.id.mediacontroller_quality3:
                    if (mActivity != null) {
                        quality3 = textViewQuality3.getText().toString().trim();
                        mediacontroller_quality_ll.setVisibility(GONE);
                        qualityChange(v);
                    }
                    break;
            }
        }
    };
    /**
     * 横竖屏切换
     */
    private OnClickListener scaleListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mActivity != null){
                switch (mActivity.getResources().getConfiguration().orientation){
                    case Configuration.ORIENTATION_LANDSCAPE://横屏
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case Configuration.ORIENTATION_PORTRAIT://竖屏
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                }
            }
        }
    };

    /**
     * 进度条事件监听
     */
    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("MYApp-MyMediaController-dispatchKeyEvent");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)){
            return true;
        }
        //处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                endGesture();
                if (progress_turn) {
                    onFinishSeekBar();
                    progress_turn = false;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 用户手势的监听
     */
    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //手势结束，控制显示或隐藏
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            progress = getProgress();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float startX = e1.getX();
            float startY = e1.getY();
            float endX = e2.getX();
            float endY = e2.getY();
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            int windowWidth = display.getWidth();
            int windowHeight = display.getHeight();
            if (Math.abs(endX - startX) < Math.abs(startY - endY)){//上下滑动
                if (startX > (windowWidth * 3.0 / 4.0)){ //右边屏幕
                    onVolumeSlide((startY - endY) / windowHeight);
                } else if (startX < (windowWidth * 1.0 / 4.0)){ //左边屏幕
                    onBrightnessSlide((startY - endY) / windowHeight);
                }
            } else {
//                onSeekChange((startX - endX) / windowWidth);
                onSeekChange((endX - startX) / 20);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM){
                mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
            } else {
                mLayout++;
            }
            if (mVideoView != null){
                mVideoView.setVideoLayout(mLayout, 0);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }



    private void qualityChange(View view){
        switch (view.getId()){
            case R.id.mediacontroller_quality2:
                textViewQuality1.setText(quality2);
                textViewQuality2.setText(quality1);
                break;
            case R.id.mediacontroller_quality3:
                textViewQuality1.setText(quality3);
                textViewQuality3.setText(quality1);
                break;
            default:
                break;
        }
        change();
    }

    private void change(){
        String string = textViewQuality1.getText().toString().trim();
        switch (string){
            case "流畅":
                Toast.makeText(mActivity,"流畅",Toast.LENGTH_LONG).show();
                mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_LOW);//画质 流畅
                break;
            case "标清":
                Toast.makeText(mActivity,"标清",Toast.LENGTH_LONG).show();
                mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);//画质 标清
                break;

            case "高清":
                Toast.makeText(mActivity,"高清",Toast.LENGTH_LONG).show();
                mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//画质 高清
                break;
        }
        mVideoView.resume();
    }

    /**
     * 显示或隐藏
     */
    private void toggleMediaControlsVisiblity(){
        if (isShowing()){
            hide();
        } else {
            show();
        }
    }

    /**
     * 设置视频文件名
     *
     * @param name
     */
    public void setVideoName(String name) {
        videoname = name;
        if (mFileName != null) {
            mFileName.setText(name);
        }
    }

    /**
     * 滑动改变声音
     * @param percent
     */
    private void onVolumeSlide(float percent){
        if (mVolume == -1){
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0){
                mVolume = 0;
            }
            mVolumeBrightnessLayout.setVisibility(VISIBLE);
            mOperationTv.setVisibility(VISIBLE);
        }
        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume){
            index = mMaxVolume;
        } else if (index < 0){
            index = 0;
        }
        if (index >= 10){
            mOperationBg.setImageResource(R.mipmap.volmn_100);
        } else if (index >= 5 && index < 10){
            mOperationBg.setImageResource(R.mipmap.volmn_60);
        } else if (index > 0 && index < 5){
            mOperationBg.setImageResource(R.mipmap.volmn_30);
        } else {
            mOperationBg.setImageResource(R.mipmap.volmn_no);
        }
        mOperationTv.setText((int) (((double) index / mMaxVolume) * 100) + "%");
        //改变声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    }

    /**
     * 滑动改变亮度
     * @param percent
     */
    private void onBrightnessSlide(float percent){
        if (mBrightness < 0){
            mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f){
                mBrightness = 0.50f;
            }
            if (mBrightness < 0.01f){
                mBrightness = 0.01f;
            }

            mVolumeBrightnessLayout.setVisibility(VISIBLE);
            mOperationTv.setVisibility(VISIBLE);
        }

        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        params.screenBrightness = mBrightness + percent;
        if (params.screenBrightness > 1.0f){
            params.screenBrightness = 1.0f;
        } else if (params.screenBrightness < 0.01f){
            params.screenBrightness = 0.01f;
        }
        mActivity.getWindow().setAttributes(params);

        mOperationTv.setText((int)(params.screenBrightness * 100) + "%");
        if (params.screenBrightness * 100 >= 90){
            mOperationBg.setImageResource(R.mipmap.light_100);
        }  else if (params.screenBrightness * 100 >= 80 && params.screenBrightness * 100 < 90) {
            mOperationBg.setImageResource(R.mipmap.light_90);
        } else if (params.screenBrightness * 100 >= 70 && params.screenBrightness * 100 < 80) {
            mOperationBg.setImageResource(R.mipmap.light_80);
        } else if (params.screenBrightness * 100 >= 60 && params.screenBrightness * 100 < 70) {
            mOperationBg.setImageResource(R.mipmap.light_70);
        } else if (params.screenBrightness * 100 >= 50 && params.screenBrightness * 100 < 60) {
            mOperationBg.setImageResource(R.mipmap.light_60);
        } else if (params.screenBrightness * 100 >= 40 && params.screenBrightness * 100 < 50) {
            mOperationBg.setImageResource(R.mipmap.light_50);
        } else if (params.screenBrightness * 100 >= 30 && params.screenBrightness * 100 < 40) {
            mOperationBg.setImageResource(R.mipmap.light_40);
        } else if (params.screenBrightness * 100 >= 20 && params.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.mipmap.light_30);
        } else if (params.screenBrightness * 100 >= 10 && params.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.mipmap.light_20);
        }
    }

    /**
     * 横向滑动改变进度
     * @param v
     */
    private void onSeekChange(float v){
//        if (mVideoView.isPlaying()){
//            //获取已播放时长
//            long currentPosition = mVideoView.getCurrentPosition();
//            //获取总时长
//            long duration = mVideoView.getDuration();
//            long position = (long) (currentPosition - ((v * duration) / 10));
//
//            if (position > duration){
//                //设置播放器从指定位置开始播放
//                mVideoView.seekTo(duration);
//            } else if (position < 0){
//                mVideoView.seekTo(0);
//            } else {
//                mVideoView.seekTo(position);
//            }
//        }

        //计算并显示 前进后退
        if (!progress_turn) {
            onStartSeekBar();
            progress_turn = true;
        }
        int change = (int) (v);
        if (change > 0) {
            mOperationBg.setImageResource(R.mipmap.right);
        } else {
            mOperationBg.setImageResource(R.mipmap.left);
        }
        mOperationTv.setVisibility(View.VISIBLE);

        mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        if (progress + change > 0) {
            if ((progress + change < 1000))
                mOperationTv.setText(setSeekBarChange(progress + change) + "/" + StringUtils.generateTime(mVideoView.getDuration()));
            else
                mOperationTv.setText(setSeekBarChange(1000) + "/" + StringUtils.generateTime(mVideoView.getDuration()));
        } else {
            mOperationTv.setText(setSeekBarChange(0) + "/" + StringUtils.generateTime(mVideoView.getDuration()));

        }
    }

    /**
     * 手势结束
     */
    private void endGesture(){
        mVolume = -1;
        mBrightness = -1f;
        mHandler.removeMessages(HIDEFRAM);
        mHandler.sendEmptyMessageDelayed(HIDEFRAM, 1);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long position;
            switch (msg.what){
                case HIDEFRAM:
                    mVolumeBrightnessLayout.setVisibility(GONE);
                    mOperationTv.setVisibility(GONE);
                    break;
            }
        }
    };

    public void setTime(String time){
        if (textViewTime != null){
            textViewTime.setText(time);
        }
    }

    //显示电量，
    public void setBattery(String stringBattery) {
        if (textViewTime != null && img_battery != null) {
            textViewBattery.setText(stringBattery + "%");
            int battery = Integer.valueOf(stringBattery);
            if (battery < 15)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_15));
            if (battery < 30 && battery >= 15)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_15));
            if (battery < 45 && battery >= 30)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_30));
            if (battery < 60 && battery >= 45)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_45));
            if (battery < 75 && battery >= 60)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_60));
            if (battery < 90 && battery >= 75)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_75));
            if (battery > 90)
                img_battery.setImageDrawable(getResources().getDrawable(R.mipmap.battery_90));
        }
    }


    /**
     * 播放与暂停
     */
    private void playOrPause(){
        if (mVideoView != null){
            if (mVideoView.isPlaying()){
                mVideoView.pause();
            } else {
                mVideoView.start();
            }
        }
    }



}
