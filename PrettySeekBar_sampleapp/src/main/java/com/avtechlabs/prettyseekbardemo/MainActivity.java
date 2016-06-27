package com.avtechlabs.prettyseekbardemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.avtechlabs.prettyseekbar.PrettySeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;
    FloatingActionButton fab;
    MediaMetadataRetriever songMetaData;
    Uri mediapath;
    byte[] albumArt;
    TextView songName, songDuration;
    PrettySeekBar prettySeekBar;
    int duration;
    boolean update, playing = false;
    Thread progressThread = null;
    ProgressUpdater progressUpdater = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        songName = (TextView)findViewById(R.id.textViewSongName);
        songDuration = (TextView)findViewById(R.id.textViewDuration);
        prettySeekBar = (PrettySeekBar)findViewById(R.id.prettySeekBar);
        player = MediaPlayer.create(this, R.raw.gangnam);
        int sleepTime = prettySeekBar.setMaxProgress(600);
        //Toast.makeText(this, player.getDuration() / 1000 + " seconds total progress..", Toast.LENGTH_LONG).show();
        //Toast.makeText(this, sleepTime + " seconds before progress..", Toast.LENGTH_LONG).show();

        mediapath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.gangnam);
        songMetaData = new MediaMetadataRetriever();
        songMetaData.setDataSource(this, mediapath);
        songName.setText("PrettySeekBar Demo");
        albumArt = songMetaData.getEmbeddedPicture();
        Bitmap songImage = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
        //prettySeekBar.setImageResource(songImage);

        prettySeekBar.setOnPrettySeekBarChangeListener(new PrettySeekBar.OnPrettySeekBarChangeListener() {
            @Override
            public void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched) {

                if(touched){
                    //Toast.makeText(getApplicationContext(), progress + "", Toast.LENGTH_LONG).show();
                    Log.d("Adhithyan", progress + "");
                    player.pause();
                    playing = false;
                    if(player != null)
                        player.seekTo(progress * 1000);

                    updateDuration(progress);
                    if(progressUpdater != null)
                        progressUpdater.setLoopVariable(progress);
                    playing = true;
                    player.start();
                }

            }

            @Override
            public void onStartTrackingTouch(PrettySeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(PrettySeekBar seekBar) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playback(View v){
        if(player.isPlaying()){
            player.pause();
            fab.setImageResource(android.R.drawable.ic_media_play);

            prettySeekBar.pauseProgress();
            playing = false;

        }else{
            player.start();
            fab.setImageResource(android.R.drawable.ic_media_pause);
            playing = true;

            if(progressThread == null){
                progressUpdater = new ProgressUpdater(player.getDuration());
                progressThread = new Thread(progressUpdater);
                progressThread.start();
            }
            prettySeekBar.makeProgress();

        }
    }

    public void updateDuration(final int durationInSeconds){
        this.update = update;

        final int minutes = durationInSeconds / 60;
        final int seconds = durationInSeconds % 60;

        final String preMinutes, preSeconds;

        preMinutes = (minutes < 10) ? "0" : "";
        preSeconds = (seconds < 10) ? "0" : "";

        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    songDuration.setText(preMinutes + minutes + ":" + preSeconds + seconds);



                    if(duration == player.getDuration() / 1000){
                        fab.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
        });

    }

    class ProgressUpdater implements Runnable{
        int duration, i;
        public ProgressUpdater(int duration){
            this.duration = duration;
            this.i = 0;
        }

        @Override
        public void run() {
            for(; i<duration; i++){

                try {
                    while(!playing)
                        Thread.sleep(1000);

                updateDuration(i);
                Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public void setLoopVariable(int i){
            this.i = i;
        }
    }


}
