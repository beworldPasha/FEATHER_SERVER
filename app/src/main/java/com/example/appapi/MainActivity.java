package com.example.appapi;

import androidx.appcompat.app.AppCompatActivity;

import com.feather.*;


import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static ImageView imageView;
    private static TextView songName;
    private static TextView artistName;
    private static EditText editor;
    private static Button buttonStart;

    private static MediaPlayer mediaPlayer;

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

        FeatherAPI.getInstance().startSession("84.246.85.148", 65231);

        String data = editor.getText().toString();

        Song song = FeatherAPI.getInstance().fetchData(data, Song.class);

        songName.setText(song.name);
        artistName.setText(song.artist);

        try {
            URL newurl = new URL(song.image);
            imageView.setImageBitmap(BitmapFactory.
                    decodeStream(newurl.openConnection().getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Toast.makeText(this, song.url, Toast.LENGTH_SHORT).show();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.url);

            mediaPlayer.setOnPreparedListener((m) -> {
                mediaPlayer.start();
            });

            mediaPlayer.prepareAsync();
        } catch (Exception exception) {
            exception.printStackTrace();
        }


        //FeatherAPI.getInstance().closeSession();
    };


}