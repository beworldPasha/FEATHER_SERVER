package com.app.feather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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
    private var applicationNavController: NavController? = null

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
        binding.mainPlaylistRecyclerView.adapter = SongsRecyclerAdapter(songs, null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mainPlaylistToolbar.setNavigationOnClickListener {
            activity?.apply {
                findViewById<FragmentContainerView>(R.id.applicationNavigationFragmentContainerView)
                    .getFragment<NavHostFragment>()
                    .navController
                    .navigate(R.id.action_playlistFragment_to_mainFragment)
            }

        }
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}