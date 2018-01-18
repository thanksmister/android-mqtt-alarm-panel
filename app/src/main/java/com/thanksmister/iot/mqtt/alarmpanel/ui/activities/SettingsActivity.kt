/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber

class SettingsActivity : BaseActivity(), ViewPager.OnPageChangeListener {


    private var dotsCount: Int = 0
    private var dots: ArrayList<ImageView>? = null
    private var settingTitles: Array<String>? = null

    private var pagerAdapter: PagerAdapter? = null
    private var actionBar: ActionBar? = null

    private val inactivityHandler: Handler = Handler()
    private val inactivityCallback = Runnable {
        Toast.makeText(this@SettingsActivity, getString(R.string.toast_screen_timeout), Toast.LENGTH_LONG).show()
        dialogUtils.clearDialogs()
        finish()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.show()
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setTitle(R.string.text_alarm_settings)
            actionBar = supportActionBar
        }

        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(this)

        setPageViewController()
        stopDisconnectTimer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_help) {
            support()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.getCurrentItem() - 1
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (toolbar != null)
            toolbar.inflateMenu(R.menu.menu_settings)

        return true
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        Timber.d("onUserInteraction")
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    /**
     * We don't show screen saver on this screen
     */
    override fun showScreenSaver() {
        //na-da
    }

    private fun support() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL)))
        } catch (ex: android.content.ActivityNotFoundException) {
            Timber.e(ex.message)
        }
    }

    private fun setPageViewController() {
        dotsCount = pagerAdapter!!.count
        dots = ArrayList<ImageView>()
        settingTitles = resources.getStringArray(R.array.settings_titles)

        for (i in 0 until dotsCount) {
            val image = ImageView(this)
            image.setImageDrawable(resources.getDrawable(R.drawable.nonselecteditem_dot))
            dots!!.add(image)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(4, 0, 4, 0)
            viewPagerIndicator.addView(dots!![i], params)
        }

        dots!!.get(0).setImageDrawable(resources.getDrawable(R.drawable.selecteditem_dot))
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        for (i in 0 until dotsCount) {
            dots!![i].setImageDrawable(resources.getDrawable(R.drawable.nonselecteditem_dot))
        }
        dots!![position].setImageDrawable(resources.getDrawable(R.drawable.selecteditem_dot))
        if (actionBar != null) {
            actionBar!!.title = settingTitles!![position]
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AlarmSettingsFragment()
                1 -> MqttSettingsFragment()
                2 -> NotificationsSettingsFragment()
                3 -> CameraSettingsFragment()
                4 -> ScreenSettingsFragment()
                5 -> WeatherSettingsFragment()
                6 -> PlatformSettingsFragment()
                7 -> AboutFragment()
                else -> AboutFragment()
            }
        }
        override fun getCount(): Int {
            return 8
        }
    }

    companion object {
        val SUPPORT_URL:String = "https://thanksmister.com/android-mqtt-alarm-panel/"
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}