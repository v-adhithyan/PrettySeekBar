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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

        mediapath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.gangnam);
        songMetaData = new MediaMetadataRetriever();
        songMetaData.setDataSource(this, mediapath);
        songName.setText(songMetaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        albumArt = songMetaData.getEmbeddedPicture();
        Bitmap songImage = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
        //prettySeekBar.setImageResource(songImage);

        player = MediaPlayer.create(this, R.raw.gangnam);

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



        }else{
            player.start();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }
}
