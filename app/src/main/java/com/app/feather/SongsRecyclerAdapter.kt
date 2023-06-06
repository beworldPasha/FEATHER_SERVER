package com.app.feather

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.app.feather.databinding.SongItemBinding
import com.feather.PlaylistSong
import com.squareup.picasso.Picasso

class SongsRecyclerAdapter(
    private val songs: Array<PlaylistSong>,
    private val applicationNavigationController: NavController?
): RecyclerView.Adapter<SongsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SongItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.songName.setText(song.title)
        holder.artistName.setText(song.artist)
        Picasso.get().load(song.image).into(holder.songImage)
    }

    inner class ViewHolder(binding: SongItemBinding): RecyclerView.ViewHolder(binding.root) {
        val songImage = binding.songImage
        val artistName = binding.songArtistName
        val songName = binding.songName

        init {
            binding.songItem.setOnClickListener {
//                applicationNavigationController?.navigate(
//
//                )
            }
        }
    }
}