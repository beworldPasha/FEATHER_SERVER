package com.app.feather

import PlayerManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.app.feather.databinding.FragmentPlayerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class PlayerFragment : Fragment() {
    private var songName: String? = null
    private var songArtist: String? = null
    private var songImage: String? = null
    private var playlistName: String? = null

    val songImageTag = "SONG_IMAGE_TAG"
    val artistNameTag = "ARTIST_TAG"
    val playlistNameTag = "PLAYLIST_NAME"
    val songNameTag = "SONG_NAME"

    private var callbackInterface: CallBackMethod? = null

    private lateinit var binding: FragmentPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            songName = it.getString(songNameTag)
            songArtist = it.getString(artistNameTag)
            songImage = it.getString(songImageTag)
            playlistName = it.getString(playlistNameTag)
        }
    }

    interface CallBackMethod {
        fun playerSlideDown()
        fun playerSlideUp(
            songImage: String?,
            artistName: String?,
            songName: String?,
            playlistName: String?
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbackInterface = context as? CallBackMethod
    }

    override fun onDetach() {
        super.onDetach()
        callbackInterface = null
    }

    fun start(
        songImage: String?,
        artistName: String?,
        songName: String?,
        playlistName: String?
    ) {
        this.songName = songName
        this.songArtist = artistName
        this.songImage = songImage
        this.playlistName = playlistName

        runPlayer()
    }

    private fun runPlayer() {
        if (songName != null && songImage != null && songArtist != null && playlistName != null) {
            val playerBinding = activity
                ?.findViewById<FragmentContainerView>(R.id.miniPlayerFragmentContainerView)
                ?.getFragment<PlayerFragment>()?.binding!!

            APIManager(context).getSong(songArtist!!, playlistName!!, songName!!) { song ->
                playerBinding.miniPlayNameSong.text = "$songName\n$playlistName"
                playerBinding.songName.text = songName
                playerBinding.songName.requestLayout()
                playerBinding.playlistName.text = playlistName
                Picasso.get().load(songImage).into(playerBinding.songImage)

                val playListButton =
                    activity
                        ?.findViewById<FragmentContainerView>(
                            R.id.applicationNavigationFragmentContainerView
                        )?.getFragment<NavHostFragment>()
                        ?.view?.findViewById<FloatingActionButton>(R.id.playlistSongsButton)


                val player = PlayerManager
                    .getInstance(requireContext(), playerBinding, activity)
                player.setMediaUrl(song!!.url)
                player.play()

                binding.miniPlayerManageButton.setIconResource(R.drawable.pause_icon)
                binding.managerSongButton.setIconResource(R.drawable.pause_icon)
                playListButton?.setImageResource(R.drawable.pause_icon)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        binding = FragmentPlayerBinding.bind(view)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callbackInterface?.playerSlideDown()
                isEnabled = false

                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        runPlayer()

        return view
    }

}