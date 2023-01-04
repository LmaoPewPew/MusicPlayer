package thu.sn.madmusicplayer;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private boolean isPaused = true;
    boolean hasOncePlayed = false;

    List<String> songs = new ArrayList<>();
    List<String> songPath = new ArrayList<>();

    ContentResolver contentResolver;
    MediaPlayer mediaPlayer;
    TextView songInfo;

    //TODO:
    // check permission
    // randomize songs
    // Ex 7
    // Ex 9
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // declare Views
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSongChange = findViewById(R.id.changeSongBtn);
        songInfo = findViewById(R.id.songInfo);

        setSettings(btnPlay, btnSongChange, songInfo);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);

        //MusicPlayer Methods
        mediaPlayer = new MediaPlayer();
        getMusicList();
        btnPlay.setOnClickListener(v -> playPauseButton(btnPlay));
        btnSongChange.setOnClickListener(v -> changeSongToRandom(btnPlay));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_Request) {
            requestPermissions(new String[]{MANAGE_EXTERNAL_STORAGE}, 1);
            createToast("Requesting permission");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * DESIGN_START
     **/
    private void setSettings(Button btnPlay, Button btnSongChange, TextView songInfo) {

        //declare the text views
        TextView songTxt = findViewById(R.id.songtxt);


        // set Status bar color
        getWindow().setStatusBarColor(setThemeColor().getColor());
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(setThemeColor());

        // set Button BG color
        btnPlay.setBackgroundColor(setThemeColor().getColor());
        btnSongChange.setBackgroundColor(setThemeColor().getColor());
        // set Button text color
        btnPlay.setTextColor(setTextColor());
        btnSongChange.setTextColor(setTextColor());
        // set Button Text Size
        btnPlay.setTextSize(20);
        btnSongChange.setTextSize(20);

        //set text color
        songTxt.setTextColor(setTextColor());
        songInfo.setTextColor(setTextColor());

    }


    @NonNull
    @Contract(" -> new")
    private ColorDrawable setThemeColor() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return new ColorDrawable(Color.parseColor("#282857"));
            case Configuration.UI_MODE_NIGHT_NO:
                return new ColorDrawable(Color.parseColor("#6088b3"));
            default:
                return new ColorDrawable(Color.parseColor("#1c8777"));
        }
    }

    private int setTextColor() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return Color.WHITE;
            case Configuration.UI_MODE_NIGHT_NO:
                return Color.BLACK;
            default:
                return Color.GRAY;
        }
    }

    /**
     * DESIGN_END
     **/

    @SuppressLint("SetTextI18n")
    private void playPauseButton(Button btnPlay) {
        if (hasOncePlayed) {
            if (isPaused) {
                isPaused = false;
                btnPlay.setText("▶ Play");
                mediaPlayer.pause();
            } else {
                isPaused = true;
                btnPlay.setText("II Pause");
                mediaPlayer.start();
            }
        } else createToast("No song has been selected");
    }


    @SuppressLint("SetTextI18n")
    private void changeSongToRandom(Button btnPlay) {
        try {
            if (checkPermission()) {
                int random = (int) (Math.random() * songs.size());
                hasOncePlayed = true;

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songPath.get(random));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } else {
                    isPaused = true;
                    btnPlay.setText("II Pause");
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songPath.get(random));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);
                }

                songInfo.setText(songs.get(random));

            } else createToast("Please allow the app to access your storage!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Error", "changeSongToRandom: " + e.getMessage());
        }
    }

    @SuppressLint("Range")
    public void getMusicList() {

        contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = contentResolver.query(songUri, null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {

                @SuppressLint("Range") String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                @SuppressLint("Range") String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                @SuppressLint("Range") String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns._ID));

                if (cur.getInt(cur.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC)) > 0) {

                    Cursor pCur = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{id}, null);

                    while (pCur.moveToNext()) {
                        @SuppressLint("Range") String path = pCur.getString(pCur.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                        songPath.add(path);
                        songs.add("Artist:   " + artist + "\n\nTitle:   " + title + "\n\nAlbum:   " + album);
                    }
                    pCur.close();
                }
            }
        }

        cur.close();
    }


    private boolean checkPermission() {
        int res = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // return (res == PackageManager.PERMISSION_GRANTED);

        return true;
    }


    public void createToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

