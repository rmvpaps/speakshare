package com.rmvpaps.speakshare;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;


import android.app.Activity;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.*;
import android.media.audiofx.NoiseSuppressor;

import java.io.IOException;


public class LocalSpeak extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    public static final int LOCALMODE = 0;
    public static final int NETWORKMODE = 1;
    public int opmode = LOCALMODE;
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    
    int minBufSize;
    boolean status = true;
    AudioRecord recorder;
	AudioTrack speaker;
	
	

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
    
    private void stopRecording() {
        /*mRecorder.stop();
        mRecorder.release();
        mRecorder = null;*/
    	status = false;
    	if(recorder!=null)
    	{
	    	recorder.stop();
	    	recorder.release();
	    	recorder = null;
    	}
    	
    	if(speaker!=null)
    	{
    		speaker.stop();
    		speaker.release();
        	speaker = null;
    	}
    	
    	
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop speaking");
                } else {
                    setText("Start speaking");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public LocalSpeak() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        setContentView(ll);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startRecording() 
    {
       /* mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();*/
    	Context cont = getApplicationContext();
    	AudioManager am = (AudioManager) cont.getSystemService(AUDIO_SERVICE);
    	am.setMode(AudioManager.MODE_IN_CALL);
    	am.setSpeakerphoneOn(false);

    	
    	//Audio Configuration. 
    	int sampleRate = 44100;      //How much will be ideal?
    	int channelConfig = AudioFormat.CHANNEL_IN_MONO;    
    	int channelConfig2 = AudioFormat.CHANNEL_OUT_MONO;   
    	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;      
    	minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    	int minBufSize2 = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
         
    	
    	recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
    	recorder.startRecording();
        
    	if(opmode == LOCALMODE)
    	{
    	
    	speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig2,audioFormat,minBufSize*2,AudioTrack.MODE_STREAM);
    	speaker.play();
        status=true;
        
        Thread streamThread = new Thread(new Runnable() {

            public void run() {
                try {
                	byte[] buffer = new byte[minBufSize];
                	while(status == true){

                	
				        //reading data from MIC into buffer
				        int minBufSize = recorder.read(buffer, 0, buffer.length);
				        speaker.write(buffer, 0, minBufSize);
                	}
	                } catch (Exception e) {
	                    Log.e("VS", "SomeException");
	                } 
	
	
	            }
	
	        });
	        streamThread.start();
    	}
    }
        
    @Override
    public void onStop()
    {
    	super.onStop();
    	stopRecording();
    	
    }
   

    
}

