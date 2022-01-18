// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import android.widget.TableRow
import androidx.core.content.ContextCompat

private const val ARG_MONSTER = "ARG_MONSTER"

class MonsterAbilityFragment(private var monster: Monster) : Fragment() {
    private lateinit var myContext: Context
    private lateinit var myView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            monster = it.getSerializable(ARG_MONSTER) as Monster
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_monster_ability, container, false)
        val a = monster.abilities

        a.forEachIndexed { pos, ability ->  showAbility(pos, ability) }

        return myView
    }

    private fun showAbility(pos: Int, ability: Abilities) {
        val tr = myView.findViewById<TableLayout>(R.id.monsterAbilityTable).getChildAt(pos+2) as TableRow
        tr.visibility = TableRow.VISIBLE
        when { pos % 2 == 1 -> tr.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
        (tr.getChildAt(0) as TextView).text = ability.type
        (tr.getChildAt(1) as TextView).text = ability.description
    }

    companion object {
        @JvmStatic
        fun newInstance(monster: Monster) =
            MonsterAbilityFragment(monster).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER, monster)
                }
            }
    }
}
