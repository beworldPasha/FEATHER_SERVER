package com.example.appapi;

import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.feather.FeatherAPI;
import com.feather.Playlist;
import com.feather.Song;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

public class MainFragment extends Fragment {
    protected final static String KEY_OBJECT = "PlaylistObject";
    private static ImageView imageView;
    private static TextView songName;
    private static TextView artistName;
    private static EditText editor;
    private static Button buttonStart;
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying;
    private static Button mPlaylistButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.song_image);
        songName = view.findViewById(R.id.song_name);
        artistName = view.findViewById(R.id.artist_name);
        editor = view.findViewById(R.id.editor);

        buttonStart = view.findViewById(R.id.button);
        buttonStart.setOnClickListener(listener);

        mPlaylistButton = view.findViewById(R.id.playlist_button);
        mPlaylistButton.setOnClickListener(playlistButtonListener);

        isPlaying = false;

        setPolicy();
    }

    private void setPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    protected void setImageSong(String urlSong) {
        try {
            URL url = new URL(urlSong);
            imageView.setImageBitmap(BitmapFactory.decodeStream(
                    url.openConnection().getInputStream())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setInfo(String artist, String nameSong) {
        artistName.setText(artist);
        songName.setText(nameSong);
    }

    protected void startSong(String songURL) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder().setUsage(
                            AudioAttributes.USAGE_MEDIA
                    ).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

            mediaPlayer.reset();
            mediaPlayer.setDataSource(songURL);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener((m) -> mediaPlayer.start());
            isPlaying = true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void fetchSong(String name) {
        Song song = FeatherAPI.getInstance().fetchData(name, Song.class);

        startSong(song.url);
        setImageSong(song.image);
        setInfo(song.artist, song.name);
    }

    @Override
    public void onResume() {
        super.onResume();
        FeatherAPI.getInstance().startSession("84.246.85.148", 65231);
    }

    View.OnClickListener listener = view -> {
        if (editor.getText().toString().equals("")) return;

        String data = editor.getText().toString();
        fetchSong(data);
    };

    View.OnClickListener playlistButtonListener = view -> {
        if (editor.getText().toString().equals("")) return;

        String data = editor.getText().toString();
        Playlist playlist = FeatherAPI.getInstance().
                fetchData(data, Playlist.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_OBJECT, playlist);

        Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_playlistFragment, bundle);
    };
}