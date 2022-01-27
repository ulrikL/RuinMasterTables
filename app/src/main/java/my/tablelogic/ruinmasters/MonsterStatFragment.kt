// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.roundToInt

private const val ARG_MONSTER = "ARG_MONSTER"

class MonsterStatFragment(private var monster: Monster) : Fragment() {
    private lateinit var myView: View
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
        myView = inflater.inflate(R.layout.fragment_monster_stat, container, false)
        val t = monster.stats.traits
        val s = monster.stats.skills
        val o = monster.stats.other
        val m = monster.stats.move
        myView.findViewById<TextView>(R.id.phy_stat).text = when (t.phy) { 0 -> "" else -> t.phy.toString() }
        myView.findViewById<TextView>(R.id.min_stat).text = when (t.min) { 0 -> "" else -> t.min.toString() }
        myView.findViewById<TextView>(R.id.int_stat).text = when (t.int) { 0 -> "" else -> t.int.toString() }
        myView.findViewById<TextView>(R.id.cha_stat).text = when (t.cha) { 0 -> "" else -> t.cha.toString() }
        myView.findViewById<TextView>(R.id.bur_skill).text = getString(R.string.skill, s.bur)
        myView.findViewById<TextView>(R.id.mel_skill).text = getString(R.string.skill, s.mel)
        myView.findViewById<TextView>(R.id.kno_skill).text = getString(R.string.skill, s.kno)
        myView.findViewById<TextView>(R.id.soc_skill).text = getString(R.string.skill, s.soc)
        myView.findViewById<TextView>(R.id.mag_skill).text = getString(R.string.skill, s.mag)
        myView.findViewById<TextView>(R.id.sur_skill).text = getString(R.string.skill, s.sur)
        myView.findViewById<TextView>(R.id.car_value).text = getString(R.string.carry, o.car)
        if (o.siz.rem(1).equals(0.0)) {
            myView.findViewById<TextView>(R.id.siz_value).text = (o.siz.roundToInt()).toString()
        } else {
            myView.findViewById<TextView>(R.id.siz_value).text = o.siz.toString()
        }
        myView.findViewById<TextView>(R.id.hp_value).text = o.hp.toString()
        myView.findViewById<TextView>(R.id.arm_value).text = o.arm.toString()
        myView.findViewById<TextView>(R.id.act_value).text = o.act
        if (m.land > 0)
            myView.findViewById<TextView>(R.id.mov_land).text = getString(R.string.move, m.land, (m.land/2))
        else
            myView.findViewById<TextView>(R.id.mov_land).text = getString(R.string.none)
        if (m.air > 0)
            myView.findViewById<TextView>(R.id.mov_air).text = getString(R.string.move, m.air, (m.air/2))
        else
            myView.findViewById<TextView>(R.id.mov_air).text = getString(R.string.none)

        debug("onCreateView!")
        return myView
    }

    override fun onResume() {
        super.onResume()
        val t = monster.stats.traits
        val o = monster.stats.other

        myView.findViewById<TextView>(R.id.phy_stat).text = when (t.phy) { 0 -> "" else -> t.phy.toString() }
        myView.findViewById<TextView>(R.id.min_stat).text = when (t.min) { 0 -> "" else -> t.min.toString() }
        myView.findViewById<TextView>(R.id.int_stat).text = when (t.int) { 0 -> "" else -> t.int.toString() }
        myView.findViewById<TextView>(R.id.cha_stat).text = when (t.cha) { 0 -> "" else -> t.cha.toString() }
        myView.findViewById<TextView>(R.id.hp_value).text = o.hp.toString()
        myView.findViewById<TextView>(R.id.car_value).text = getString(R.string.carry, o.car)
    }

    fun updateMonster() {
        debug("Update displayed monster data")
        if (this::myView.isInitialized) {
            val t = monster.stats.traits
            val o = monster.stats.other

            myView.findViewById<TextView>(R.id.phy_stat).text = when (t.phy) { 0 -> "" else -> t.phy.toString() }
            myView.findViewById<TextView>(R.id.min_stat).text = when (t.min) { 0 -> "" else -> t.min.toString() }
            myView.findViewById<TextView>(R.id.int_stat).text = when (t.int) { 0 -> "" else -> t.int.toString() }
            myView.findViewById<TextView>(R.id.cha_stat).text = when (t.cha) { 0 -> "" else -> t.cha.toString() }
            myView.findViewById<TextView>(R.id.hp_value).text = o.hp.toString()
            myView.findViewById<TextView>(R.id.car_value).text = getString(R.string.carry, o.car)
        }
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::MonsterStatFragment", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::MonsterStatFragment", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::MonsterStatFragment", message)
    }

    companion object {
        @JvmStatic
        fun newInstance(monster: Monster) =
            MonsterStatFragment(monster).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER, monster)
                }
            }
    }
}
