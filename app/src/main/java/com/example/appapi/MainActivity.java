package com.example.appapi;

import androidx.appcompat.app.AppCompatActivity;
import com.feather.*;


import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static ImageView imageView;
    private static TextView songName;
    private static TextView artistName;
    private static EditText editor;
    private static Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.song_image);
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.artist_name);
        editor = findViewById(R.id.editor);

        buttonStart = findViewById(R.id.button);
        buttonStart.setOnClickListener(listener);

    }

    View.OnClickListener listener = view -> {
        if (editor.getText().toString().equals("")) return;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FeatherAPI.getInstance().startSession("84.246.85.148", 8000);

        String data = editor.getText().toString();

        Song song = FeatherAPI.getInstance().fetchData(data, Song.class);

        songName.setText(song.name);
        artistName.setText(song.artist);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(song.url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        FeatherAPI.getInstance().closeSession();
    };


}