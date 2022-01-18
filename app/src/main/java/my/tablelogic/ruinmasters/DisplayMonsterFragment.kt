// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

private const val ARG_MONSTER = "ARG_MONSTER"

class PageAdapterMonster(fm: FragmentManager, lc: Lifecycle, m: Monster) : FragmentStateAdapter(fm, lc) {
    val monster = m

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> { return MonsterStatFragment.newInstance(monster) }
            1 -> { return MonsterCombatFragment.newInstance(monster) }
            2 -> { return MonsterAbilityFragment.newInstance(monster) }
        }
        return MonsterStatFragment.newInstance(monster)
    }
}

class DisplayMonsterFragment(var monster: Monster) : DialogFragment() {
    private lateinit var myContext: Context
    private lateinit var myView: View

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            monster = it.getSerializable(ARG_MONSTER) as Monster
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_display_monster, container, false)
        val monsterName = myView.findViewById<TextView>(R.id.tvMonsterName)
        val dismissButton = myView.findViewById<AppCompatButton>(R.id.btnMonsterDismiss)

        val viewPagerMonster = myView.findViewById<ViewPager2>(R.id.viewPagerMonster)
        viewPagerMonster.adapter = PageAdapterMonster(childFragmentManager, lifecycle, monster)

        val tabLayoutMonster = myView.findViewById<TabLayout>(R.id.tabLayoutMonster)
        TabLayoutMediator(tabLayoutMonster, viewPagerMonster) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.m_tab_1)
                1 -> tab.text = resources.getString(R.string.m_tab_2)
                2 -> tab.text = resources.getString(R.string.m_tab_3)
            }
        }.attach()
        tabLayoutMonster.setFont()

        monsterName.text = monster.name
        dismissButton.setOnClickListener { dismiss() }

        return myView
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
                    tabViewChild.typeface = resources.getFont(R.font.header)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setWidthPercent(98)
    }

    private fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        @JvmStatic
        fun newInstance(monster: Monster) =
            DisplayMonsterFragment(monster).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER, monster)
                }
            }
    }
}
