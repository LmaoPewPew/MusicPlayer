package thu.sn.madmusicplayer;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private boolean paused = true;
    List<String> songs = new ArrayList<>();
    ContentResolver cr;
    MediaPlayer mediaPlayer;
    TextView songInfo;


    //TODO:
    // check permission
    // randomize songs
    // Ex 7
    // Ex 9
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSongChange = findViewById(R.id.changeSongBtn);

        songInfo = findViewById(R.id.songTxtInfo);
        mediaPlayer = new MediaPlayer();

        ///////////////////////////
        setWindowColor();
        setSettings(btnPlay, btnSongChange);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        ///////////////////////////

        songs = getMusicList();
        addSongsToMediaPlayer(mediaPlayer, songs);
        btnPlay.setOnClickListener(v -> playPauseButton(btnPlay));
        btnSongChange.setOnClickListener(v -> changeRandomSong());
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
        }

        return super.onOptionsItemSelected(item);
    }

    /****/
    private void setSettings(Button btnPlay, Button btnSongChange) {
        btnPlay.setBackgroundColor(setThemeColor().getColor());
        btnPlay.setTextColor(setTextColor());
        btnSongChange.setBackgroundColor(setThemeColor().getColor());
        btnSongChange.setTextColor(setTextColor());
        btnPlay.setTextSize(20);
        btnSongChange.setTextSize(20);

        ///////////////////////////
        songInfo.setTextColor(setTextColor());
        TextView txt = findViewById(R.id.songtxt);
        txt.setTextColor(setTextColor());
    }

    public void setWindowColor() {
        getWindow().setStatusBarColor(setThemeColor().getColor());
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(setThemeColor());
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


    /***/

    @SuppressLint("SetTextI18n")
    private void playPauseButton(Button btnPlay) {
        if (checkPermission()) {
            if (paused) {
                paused = false;
                btnPlay.setText("II Pause");
                mediaPlayer.start();
            } else {
                paused = true;
                btnPlay.setText("▶ Play");
                mediaPlayer.pause();
            }

            songInfo.setText("Interpret: " + songs.get(1) + "\n\nTitle: " + songs.get(2) + "\n\nAlbum: " + songs.get(3));
        } else createToast("Please allow the app to access your storage!");
    }


    private void changeRandomSong() {
        if (checkPermission()) {
            int random = (int) (Math.random() * songs.size());

            songInfo.setText(random);
        } else createToast("Please allow the app to access your storage!");

    }

    @SuppressLint("Range")
    public List<String> getMusicList() {

        List<String> vs = new ArrayList<>();
        cr = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = cr.query(songUri, null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {

                @SuppressLint("Range") String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                @SuppressLint("Range") String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                @SuppressLint("Range") String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns._ID));

                if (cur.getInt(cur.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC)) > 0) {

                    Cursor pCur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{id}, null);

                    while (pCur.moveToNext()) {
                        @SuppressLint("Range") String path = pCur.getString(pCur.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                        vs.add(path);
                        vs.add(artist);
                        vs.add(title);
                        vs.add(album);
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
        return vs;
    }

    public void addSongsToMediaPlayer(MediaPlayer mediaPlayer, List<String> songs) {
        for (String song : songs) {
            try {
                mediaPlayer.setDataSource(song);
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

