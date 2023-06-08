package com.app.feather

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.feather.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), PlayerFragment.CallBackMethod,
    SongsRecyclerAdapter.CallBackMethod {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var playerView: FragmentContainerView
    private lateinit var menuView: BottomNavigationView

    private var maxPlayerHeight by Delegates.notNull<Int>()
    private var minPlayerHeight by Delegates.notNull<Int>()
    private var maxMenuHeight by Delegates.notNull<Int>()
    private var minMenuHeight by Delegates.notNull<Int>()

    private lateinit var playerAnimationListener: OnClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(APIManager(applicationContext)) {
            uploadTokens(
                AccountsManager(applicationContext)
                    .getTokens(SharedPreferencesManager(this@MainActivity))
            )
        }

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.applicationNavigationFragmentContainerView
                ) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        playerView = binding.miniPlayerFragmentContainerView
        menuView = binding.bottomNavigationView

        playerAnimationListener = OnClickListener {
            playerSlideUp(null, null, null, null)
        }
        playerView.setOnClickListener(playerAnimationListener)
    }

    override fun onResume() {
        maxMenuHeight = 240
        minMenuHeight = 0

        minPlayerHeight = 144
        maxPlayerHeight = 1968 + maxMenuHeight + 58

        if (intent?.getStringExtra("PLAYBACK_PLAYER") == "PlayerFragment")
            playerView.performClick()

        super.onResume()
    }

    override fun onDestroy() {
        stopService(Intent(applicationContext, AudioPlayerService::class.java))
        closeSession()
        super.onDestroy()
    }

    private fun closeSession() {
        val userPreferencesManager = SharedPreferencesManager(this)
        if (!userPreferencesManager.isRemembered()) {
            userPreferencesManager.getUserLogin()?.let {
                AccountsManager(this).removeAccount(it)
            }
            userPreferencesManager.removeUserPreference()
        } //else userPreferencesManager.removeUserPreference()
    }

    override fun playerSlideDown() {
        if (playerView.height != maxPlayerHeight) return

        menuView.visibility = View.VISIBLE
        menuView.alpha = 0f

        val reverseFragmentAnimator = ValueAnimator
            .ofInt(maxPlayerHeight, minPlayerHeight)
        reverseFragmentAnimator?.addUpdateListener { animator ->
            val layoutParams = playerView.layoutParams
            layoutParams?.height = animator.animatedValue as Int
            playerView.layoutParams = layoutParams

            if (playerView.height in 800..1000) {
                binding.miniPlayerFragmentContainerView.getFragment<PlayerFragment>().view
                    ?.findViewById<ConstraintLayout>(R.id.miniPlayerLayout)?.visibility =
                    View.VISIBLE
                binding.miniPlayerFragmentContainerView.getFragment<PlayerFragment>().view
                    ?.findViewById<ConstraintLayout>(R.id.bigPlayLayout)?.visibility = View.GONE
            }
        }

        val reverseBottomNavAnimator = ValueAnimator
            .ofInt(minMenuHeight, maxMenuHeight)
        reverseBottomNavAnimator?.addUpdateListener { animator ->
            val layoutParams = menuView.layoutParams
            layoutParams?.height = animator.animatedValue as Int
            menuView.layoutParams = layoutParams
        }

        val visibilityAnimator = ObjectAnimator
            .ofFloat(menuView, "alpha", 0f, 1f)
        visibilityAnimator.duration = 500

        val reverseSetAnimator = AnimatorSet()
        reverseSetAnimator.playTogether(
            reverseFragmentAnimator,
            reverseBottomNavAnimator,
            visibilityAnimator
        )
        reverseSetAnimator.duration = 700

        reverseSetAnimator.start()
    }

    override fun playerSlideUp(
        songImage: String?,
        artistName: String?,
        songName: String?,
        playlistName: String?
    ) {
        if (playerView.height != minPlayerHeight) return

        val fragmentAnimator = ValueAnimator.ofInt(minPlayerHeight, maxPlayerHeight)
        fragmentAnimator?.addUpdateListener { animator ->
            val layoutParams = playerView.layoutParams
            layoutParams?.height = animator.animatedValue as Int
            playerView.layoutParams = layoutParams

            if (playerView.height in 800..1000) {
                binding.miniPlayerFragmentContainerView.getFragment<PlayerFragment>().view
                    ?.findViewById<ConstraintLayout>(R.id.miniPlayerLayout)?.visibility = View.GONE
                binding.miniPlayerFragmentContainerView.getFragment<PlayerFragment>().view
                    ?.findViewById<ConstraintLayout>(R.id.bigPlayLayout)?.visibility = View.VISIBLE
            }
        }

        val bottomNavAnimator = ValueAnimator.ofInt(maxMenuHeight, minMenuHeight)
        bottomNavAnimator?.addUpdateListener { animator ->
            val layoutParams = menuView.layoutParams
            layoutParams.height = animator.animatedValue as Int
            menuView.layoutParams = layoutParams

            if (menuView.height <= 5) menuView.visibility = View.GONE
        }

        val setAnimator = AnimatorSet()
        setAnimator.playTogether(fragmentAnimator, bottomNavAnimator)
        setAnimator.duration = 700

        setAnimator.start()

        setAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                navController.navigate(R.id.playerFragment, Bundle().apply {
                    val songImageTag = "SONG_IMAGE_TAG"
                    val artistNameTag = "ARTIST_TAG"
                    val playlistNameTag = "PLAYLIST_NAME"
                    val songNameTag = "SONG_NAME"

                    putString(songImageTag, songImage)
                    putString(artistNameTag, artistName)
                    putString(songNameTag, songName)
                    putString(playlistNameTag, playlistName)
                })
            }
        })
    }
}