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
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber
import android.support.v4.view.ViewCompat.setAlpha
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.BadParcelableException


class SettingsActivity : BaseActivity(), ViewPager.OnPageChangeListener, SettingsFragment.SettingsFragmentListener {

    private var settingTitles: Array<String>? = null
    private var pagerAdapter: PagerAdapter? = null
    private var actionBar: ActionBar? = null
    private val PAGE_NUM = 10

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
            supportActionBar!!.setTitle(R.string.activity_settings_title)
            actionBar = supportActionBar
        }

        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        // Fix for crash with BadPacelException
        // https://stackoverflow.com/questions/49228979/badparcelableexceptionclassnotfoundexception-when-unmarshalling-android-suppor
        viewPager.offscreenPageLimit = 2;
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
            super.onBackPressed()
        } else {
            try {
                viewPager.currentItem = 0
            } catch (e: BadParcelableException) {
                Timber.e("ViewPager Error: " + e.message)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.menu_settings)
            val itemLen = menu.size()
            for (i in 0 until itemLen) {
                val drawable = menu.getItem(i).icon
                if (drawable != null) {
                    drawable.mutate()
                    drawable.setColorFilter(resources.getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
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
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun navigatePageNumber(page: Int) {
        if(page in 1..(PAGE_NUM - 1)) {
            viewPager.currentItem = page
        }
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
        settingTitles = resources.getStringArray(R.array.settings_titles)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (actionBar != null) {
            actionBar!!.title = settingTitles!![position]
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> AlarmSettingsFragment()
                2 -> MqttSettingsFragment()
                3 -> DeviceSensorsFragment()
                4 -> NotificationsSettingsFragment()
                5 -> CameraSettingsFragment()
                6 -> ScreenSettingsFragment()
                7 -> WeatherSettingsFragment()
                8 -> PlatformSettingsFragment()
                9 -> AboutFragment()
                else -> SettingsFragment()
            }
        }
        override fun getCount(): Int {
            return PAGE_NUM
        }
    }

    companion object {
        val SUPPORT_URL:String = "https://thanksmister.com/android-mqtt-alarm-panel/"
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}