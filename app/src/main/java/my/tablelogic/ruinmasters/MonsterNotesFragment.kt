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

class MonsterNotesFragment(private var monster: Monster) : Fragment() {
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
        myView = inflater.inflate(R.layout.fragment_monster_notes, container, false)
        val n = monster.notes

        n.forEachIndexed { pos, note ->  showNote(pos, note) }

        return myView
    }

    private fun showNote(pos: Int, note: Notes) {
        val tr = myView.findViewById<TableLayout>(R.id.monsterNotesTable).getChildAt(pos+2) as TableRow
        tr.visibility = TableRow.VISIBLE
        when { pos % 2 == 1 -> tr.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
        (tr.getChildAt(0) as TextView).text = note.header
        (tr.getChildAt(1) as TextView).text = note.text
    }

    companion object {
        @JvmStatic
        fun newInstance(monster: Monster) =
            MonsterNotesFragment(monster).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER, monster)
                }
            }
    }
}
