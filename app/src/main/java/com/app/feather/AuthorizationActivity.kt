package com.app.feather

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.app.feather.databinding.ActivityAuthorizationBinding
import kotlin.math.abs

class AuthorizationActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityAuthorizationBinding

    private val startSize = 20f
    private val endSize = 30f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)

        setupViewPager()

        layoutBinding.pager.adapter = AuthPagerAdapter(this)
    }

    private fun setupViewPager() {
        //layoutBinding.signUpHeader.textSize = startSize
        //layoutBinding.signInHeader.textSize = endSize

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = 500

        layoutBinding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                animateHeaders(positionOffset, position, animator)

                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                animator.start()
            }
        })
    }

    fun animateHeaders(positionOffset: Float, position: Int, animator: ValueAnimator) {
        animator.addUpdateListener {
            val sizeCurrentTab =
                endSize - (endSize - startSize) * (abs(positionOffset - position))
            val sizeNextTab =
                startSize + (endSize - startSize) * (abs(positionOffset - position))

            layoutBinding.signInHeader.textSize = sizeCurrentTab
            layoutBinding.signUpHeader.textSize = sizeNextTab
        }
    }
}