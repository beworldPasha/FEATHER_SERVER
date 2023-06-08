package com.app.feather

import PlayerManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.feather.databinding.FragmentPlaylistBinding
import com.feather.PlaylistSong
import com.squareup.picasso.Picasso


class PlaylistFragment : Fragment() {
    private var playlistName: String? = null
    private var artistName: String? = null
    private var imageUrl: String? = null
    private lateinit var songs: Array<PlaylistSong>

    val playlistImageTag = "IMAGE_TAG"
    val playlistNameTag = "PLAYLIST_NAME"
    val artistNameTag = "ARTIST_TAG"
    val songsTag = "SONGS_TAG"

    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistName = it.getString(playlistNameTag)
            artistName = it.getString(artistNameTag)
            imageUrl = it.getString(playlistImageTag)
            songs = it.getSerializable(songsTag) as Array<PlaylistSong>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        binding = FragmentPlaylistBinding.bind(view)

        Picasso.get().load(imageUrl).into(binding.mainPlaylistImage)
        binding.mainPlaylistToolbar.title = playlistName

        binding.mainPlaylistRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.mainPlaylistRecyclerView.adapter =
            SongsRecyclerAdapter(songs, activity, playlistName)


        if (PlayerManager.getInstance()?.player?.isPlaying == true)
            binding.playlistSongsButton.setImageResource(R.drawable.pause_icon)
        else binding.playlistSongsButton.setImageResource(R.drawable.play_icon)



        binding.mainPlaylistToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistSongsButton.setOnClickListener {
            if (PlayerManager.getInstance()?.isTrackLoaded() == true)
                PlayerManager.getInstance()?.togglePlayback()
            else binding.mainPlaylistRecyclerView.findViewHolderForAdapterPosition(0)
                ?.itemView?.performClick()
        }

        return view
    }
}