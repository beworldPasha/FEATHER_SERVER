package com.example.appapi;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feather.PlaylistSong;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class RecyclerSongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {
    private List<PlaylistSong> mSongs;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public RecyclerSongsAdapter(Context context, List<PlaylistSong> songs) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mSongs = songs;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recyclerViewItem = mLayoutInflater.inflate(
                R.layout.cardview_song, parent, false);

        return new SongsViewHolder(recyclerViewItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        PlaylistSong song = mSongs.get(position);

        try {
            URL url = new URL(song.image);
            holder.imageSong.setImageBitmap(BitmapFactory.decodeStream(
                    url.openConnection().getInputStream())
            );

            holder.songName.setText(song.artist);
            holder.artistName.setText(song.title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }
}
