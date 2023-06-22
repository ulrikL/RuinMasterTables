// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// CC BY-NC-SA License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
import kotlin.math.roundToInt
import kotlin.random.Random

private const val ARG_MONSTER = "ARG_MONSTER"

class PageAdapterMonster(fm: FragmentManager, lc: Lifecycle, m: Monster) : FragmentStateAdapter(fm, lc) {
    private var monster = m
    private var statTab : MonsterStatFragment = MonsterStatFragment.newInstance(monster)
    private var combatTab : MonsterCombatFragment = MonsterCombatFragment.newInstance(monster)
    private var abilityTab : MonsterAbilityFragment = MonsterAbilityFragment.newInstance(monster)

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> { return statTab }
            1 -> { return combatTab }
            2 -> { return abilityTab }
        }
        return statTab
    }

    fun updateTabs() {
        if (statTab.isVisible) {
            statTab.updateMonster()
        }
        if (combatTab.isVisible) {
            combatTab.updateMonster()
        }
    }
}

class DisplayMonsterFragment(var monster: Monster) : DialogFragment() {
    private lateinit var myContext: Context
    private lateinit var myView: View
    private lateinit var myPageAdapterMonster: PageAdapterMonster

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
        val randomizeButton = myView.findViewById<AppCompatButton>(R.id.btnMonsterRandomize)

        val viewPagerMonster = myView.findViewById<ViewPager2>(R.id.viewPagerMonster)
        myPageAdapterMonster = PageAdapterMonster(childFragmentManager, lifecycle, monster)
        viewPagerMonster.adapter = myPageAdapterMonster

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

        if (monster.stats.traits.phy_die.isNotEmpty() ||
            monster.stats.traits.min_die.isNotEmpty() ||
            monster.stats.traits.int_die.isNotEmpty() ||
            monster.stats.traits.cha_die.isNotEmpty() ) {
            randomizeButton.setOnClickListener { randomizeMonster() }
        } else {
            randomizeButton.isEnabled = false
            randomizeButton.isClickable = false
        }

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

    private fun getRandomInt(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        val rand = Random(System.nanoTime())
        return (start..end).random(rand)
    }

    private fun doDieRollWithModifier(text : String): Int {
        val regex = "\\b(\\d*)d(\\d*)([+\\-]*)(\\d*)".toRegex()
        val match = regex.find(text)
        var dieResult = 0

        if (match != null &&  match.value.isNotBlank()) {
            val numberOfDice : Int = match.groupValues[1].toInt()
            val diceType : Int = match.groupValues[2].toInt()
            val modifyAdd : Boolean = (match.groupValues[3]=="+")
            val modifyRemove : Boolean = (match.groupValues[3]=="-")
            val modifier : Int = if (match.groupValues[4] != "") match.groupValues[4].toInt() else 0
            debug("Found '${match.value}' with group1=$numberOfDice and group2=$diceType in string.")
            for (i in 1..numberOfDice) {
                val roll = getRandomInt(1, diceType)
                debug("Rolled 1D$diceType and got ${roll}.")
                dieResult += roll
            }
            if (modifyAdd) {
                debug("Found '${modifier}' to add to die result (${dieResult}).")
                dieResult += modifier
            }
            if (modifyRemove) {
                debug("Found '${modifier}' to remove from die result (${dieResult}).")
                dieResult -= modifier
            }
            debug("Got ${match.value}=$dieResult.")
        }
        return dieResult
    }

    private fun randomizeMonster() {
        val t = monster.stats.traits
        val o = monster.stats.other

        t.phy = doDieRollWithModifier(t.phy_die)
        t.min = doDieRollWithModifier(t.min_die)
        t.int = doDieRollWithModifier(t.int_die)
        t.cha = doDieRollWithModifier(t.cha_die)

        // Calculate HP=(Physique + Mind + 1D10) * size
        o.hp = (((t.phy + t.min + getRandomInt(1,10)).toDouble()) * o.siz).roundToInt()
        // Calculate carrying capacity as physique * size^2. No clear guidance in rules.
        o.car = (t.phy.toDouble() * o.siz * o.siz).roundToInt()

        o.db = when {
            t.phy in 21..25 -> 1
            t.phy in 26..28 -> 2
            t.phy > 28 -> 3
            else -> 0
        }
        myPageAdapterMonster.updateTabs()
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::DisplayMonsterFragment", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::DisplayMonsterFragment", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::DisplayMonsterFragment", message)
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
