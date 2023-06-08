package com.app.feather

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.app.feather.databinding.SongItemBinding
import com.feather.PlaylistSong
import com.squareup.picasso.Picasso

class SongsRecyclerAdapter(
    private val songs: Array<PlaylistSong>,
    private val activity: FragmentActivity?,
    private val playlistName: String?
) : RecyclerView.Adapter<SongsRecyclerAdapter.ViewHolder>() {
    private var previousSong: SongItemBinding? = null
    private var firstSongItemBinding: SongItemBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SongItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.songName.text = song.title
        holder.artistName.text = song.artist
        Picasso.get().load(song.image).into(holder.songImage)
    }

    interface CallBackMethod {
        fun playerSlideUp(
            songImage: String?,
            artistName: String?,
            songName: String?,
            playlistName: String?
        )
    }

    inner class ViewHolder(binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val songImage = binding.songImage
        val artistName = binding.songArtistName
        val songName = binding.songName

        init {
            val player = activity
                ?.findViewById<FragmentContainerView>(R.id.miniPlayerFragmentContainerView)
                ?.getFragment<PlayerFragment>()

            binding.songItem.setOnClickListener {
                val song = songs[position]

                previousSong?.songItem?.setBackgroundColor(
                    ContextCompat
                        .getColor(activity?.applicationContext!!, android.R.color.transparent))

                previousSong = binding

                binding.songItem.setBackgroundColor(
                    ContextCompat
                        .getColor(activity?.applicationContext!!, R.color.chooseItem))

                player?.start(song.image, song.artist, song.title, playlistName)
                activity.startService(
                    Intent(activity, AudioPlayerService::class.java)
                )
            }
        }
    }
}
