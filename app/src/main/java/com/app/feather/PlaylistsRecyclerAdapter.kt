package com.app.feather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.app.feather.databinding.PlaylistCardViewBinding
import com.feather.Playlist
import com.squareup.picasso.Picasso

class PlaylistsRecyclerAdapter(
    private val playlists: ArrayList<Playlist>,
    private val applicationNavigationController: NavController?
) :
    RecyclerView.Adapter<PlaylistsRecyclerAdapter.ViewHolder>() {
    val playlistImageTag = "IMAGE_TAG"
    val playlistNameTag = "PLAYLIST_NAME"
    val artistNameTag = "ARTIST_TAG"
    val songsTag = "SONGS_TAG"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            PlaylistCardViewBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name
        holder.artistName.text = playlist.artists[0]
        Picasso.get().load(playlist.image).into(holder.imagePlaylist)
    }

    override fun getItemCount(): Int = playlists.size

    inner class ViewHolder(
        binding: PlaylistCardViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val imagePlaylist = binding.playlistImage
        val playlistName = binding.playlistName
        val artistName = binding.artistName

        init {
            binding.playlistCardView.setOnClickListener {
                applicationNavigationController?.navigate(
                    R.id.action_homeFragment_to_playlistFragment,
                    Bundle().apply {
                        putString(playlistNameTag, playlists[position].name)
                        putString(artistNameTag, playlists[position].artists[0])
                        putString(playlistImageTag, playlists[position].image)
                        putSerializable(songsTag, playlists[position].songs)
                    }
                )
            }
        }
    }


}