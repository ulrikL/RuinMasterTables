// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PageAdapter(fm:FragmentManager, lc:Lifecycle) : FragmentStateAdapter(fm, lc) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> { return TableFragment() }
            1 -> { return EncounterFragment() }
        }
        return TableFragment()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById<TextView>(R.id.custom_title)).typeface = Typeface.createFromAsset(assets, "fonts/Becker.ttf")

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = PageAdapter(supportFragmentManager, lifecycle)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                0 -> tab.text = "Tables"
                1 -> tab.text = "Monsters"
            }
        }.attach()
        tabLayout.setFont()
    }

    private fun TabLayout.setFont() {
        val viewGroup = getChildAt(0) as ViewGroup
        val tabsCount = viewGroup.childCount
        for (j in 0 until tabsCount) {
            val viewGroupChildAt = viewGroup.getChildAt(j) as ViewGroup
            val tabChildCount = viewGroupChildAt.childCount
            for (i in 0 until tabChildCount) {
                val tabViewChild = viewGroupChildAt.getChildAt(i)
                if (tabViewChild is TextView) {
                    tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/BarcelonaITCStd-Bold.otf")
                }
            }
        }
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::MainActivity", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::MainActivity", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::MainActivity", message)
    }
}
