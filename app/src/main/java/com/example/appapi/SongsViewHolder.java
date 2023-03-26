package com.example.appapi;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongsViewHolder extends RecyclerView.ViewHolder {
    protected ImageView imageSong;
    protected TextView songName;
    protected TextView artistName;
    public SongsViewHolder(@NonNull View itemView) {
        super(itemView);

        imageSong = itemView.findViewById(R.id.card_song_image);
        songName = itemView.findViewById(R.id.card_song_name);
        artistName = itemView.findViewById(R.id.card_song_artist_name);

        itemView.findViewById(R.id.card_view_song);
    }
}
