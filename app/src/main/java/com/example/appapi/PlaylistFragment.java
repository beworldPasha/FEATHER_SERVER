package com.example.appapi;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feather.FeatherAPI;
import com.feather.Playlist;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class PlaylistFragment extends Fragment {
    private ImageView mImageView;
    private TextView mPlaylistName;
    private TextView mArtistName;
    private Playlist mPlaylist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlaylist = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    private void setPlaylistInfo(@NonNull Playlist playlist) {
        try {
            URL url = new URL(playlist.image);
            mImageView.setImageBitmap(BitmapFactory.decodeStream(
                    url.openConnection().getInputStream())
            );

            mPlaylistName.setText(playlist.name);
            mArtistName.setText(playlist.artists[0]);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.playlist_image);
        mPlaylistName = view.findViewById(R.id.playlist_name);
        mArtistName = view.findViewById(R.id.artist_name);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPlaylist = (Playlist) bundle.getSerializable(MainFragment.KEY_OBJECT);
            setPlaylistInfo(mPlaylist);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                    getContext(), LinearLayoutManager.VERTICAL, false);

            RecyclerView playlistRecycler = view.findViewById(R.id.playlist_songs_recycler);
            playlistRecycler.setLayoutManager(linearLayoutManager);
            playlistRecycler.setAdapter(new RecyclerSongsAdapter(getContext(),
                    Arrays.asList(mPlaylist.songs))); // On = O(1)
        }
    }
}