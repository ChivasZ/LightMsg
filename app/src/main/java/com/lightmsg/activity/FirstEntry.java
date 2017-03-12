package com.lightmsg.activity;

import java.io.File;

import com.lightmsg.R;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class FirstEntry extends Activity {
    private static final String TAG = FirstEntry.class.getSimpleName();

    private SurfaceView sv;
    private MediaPlayer mp;
    private String 		fn = "/sdcard/Download/test.mp4";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.first_surface);

        sv = (SurfaceView) findViewById(R.id.sv);

        sv.getHolder().setFixedSize(176, 144);
        sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sv.getHolder().addCallback(new SurfaceCallBack());
        
        mp = new MediaPlayer();
        
    }

    private void play() {
        Log.d(TAG, "play()...");
        try {
            //File file=new File(Environment.getExternalStorageDirectory(), fn);
            File file=new File(fn);
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDisplay(sv.getHolder());
            mp.setDataSource(file.getAbsolutePath());
            mp.setLooping(true);
            mp.setOnCompletionListener(completionListener);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void stop() {
        Log.d(TAG, "stop()...");
        if (mp.isPlaying()) {
            mp.stop();
        }
    }
    
    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // TODO Auto-generated method stub
            Log.d(TAG, "MediaPlayer.OnCompletionListener.onCompletion()...");
        }
        
    };

    private final class SurfaceCallBack implements SurfaceHolder.Callback{

        @Override
        public void surfaceChanged(SurfaceHolder holder,
                int format, int width, int height) {
            // TODO Auto-generated method stub
            Log.d(TAG, "SurfaceCallBack.surfaceChanged()...");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            Log.d(TAG, "SurfaceCallBack.surfaceCreated()...");
            play();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            Log.d(TAG, "SurfaceCallBack.surfaceDestroyed()...");
            stop();
        }

    }
}
