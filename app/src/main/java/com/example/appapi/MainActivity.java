package com.example.appapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
    };


}