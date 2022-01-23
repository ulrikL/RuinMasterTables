// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.roundToInt

private const val ARG_MONSTER = "ARG_MONSTER"

class MonsterStatFragment(private var monster: Monster) : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            monster = it.getSerializable(ARG_MONSTER) as Monster
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_monster_stat, container, false)
        val t = monster.stats.traits
        val s = monster.stats.skills
        val o = monster.stats.other
        val m = monster.stats.move
        v.findViewById<TextView>(R.id.phy_stat).text = t.phy
        v.findViewById<TextView>(R.id.min_stat).text = t.min
        v.findViewById<TextView>(R.id.int_stat).text = t.int
        v.findViewById<TextView>(R.id.cha_stat).text = t.cha
        v.findViewById<TextView>(R.id.bur_skill).text = getString(R.string.skill, s.bur)
        v.findViewById<TextView>(R.id.mel_skill).text = getString(R.string.skill, s.mel)
        v.findViewById<TextView>(R.id.kno_skill).text = getString(R.string.skill, s.kno)
        v.findViewById<TextView>(R.id.soc_skill).text = getString(R.string.skill, s.soc)
        v.findViewById<TextView>(R.id.mag_skill).text = getString(R.string.skill, s.mag)
        v.findViewById<TextView>(R.id.sur_skill).text = getString(R.string.skill, s.sur)
        v.findViewById<TextView>(R.id.car_value).text = getString(R.string.carry, o.car)
        if (o.siz.rem(1).equals(0.0)) {
            v.findViewById<TextView>(R.id.siz_value).text = (o.siz.roundToInt()).toString()
        } else {
            v.findViewById<TextView>(R.id.siz_value).text = o.siz.toString()
        }
        v.findViewById<TextView>(R.id.hp_value).text = o.hp.toString()
        v.findViewById<TextView>(R.id.arm_value).text = o.arm.toString()
        v.findViewById<TextView>(R.id.act_value).text = o.act
        if (m.land > 0)
            v.findViewById<TextView>(R.id.mov_land).text = getString(R.string.move, m.land, (m.land/2))
        else
            v.findViewById<TextView>(R.id.mov_land).text = getString(R.string.none)
        if (m.air > 0)
            v.findViewById<TextView>(R.id.mov_air).text = getString(R.string.move, m.air, (m.air/2))
        else
            v.findViewById<TextView>(R.id.mov_air).text = getString(R.string.none)
        return v
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
