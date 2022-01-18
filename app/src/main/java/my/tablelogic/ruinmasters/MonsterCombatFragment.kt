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
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import android.widget.TableRow
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import kotlin.math.roundToInt

private const val ARG_MONSTER = "ARG_MONSTER"

class MonsterCombatFragment(private var monster: Monster) : Fragment() {
    private lateinit var myContext: Context
    private lateinit var myView: View
    private infix fun Int.fdiv(i: Int): Int = ((this / i.toDouble()).roundToInt())
    private infix fun Int.fmul(i: Double): Int = ((this * i).roundToInt())


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
        myView = inflater.inflate(R.layout.fragment_monster_combat, container, false)
        val at = monster.combat.attacks
        val b = monster.combat.body
        val o = monster.stats.other

        when (monster.combat.body.type) {
            HUMANOID -> {
                showBodyPart(0,"Head", "1", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Right arm", "2", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Left arm", "3", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Torso", "4-6", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(4,"Right leg", "7-8", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(5,"Left leg", "9-10", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(5, b.comment)
            }
            QUADRUPED -> {
                showBodyPart(0,"Head", "1", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Right forelimb", "2-3", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Left forelimb", "4-5", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Body", "6-8", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(4,"Right hindlimb", "9", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(5,"Left hindlimb", "10", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(5, b.comment)
            }
            GIANT -> {
                showBodyPart(0,"Head", "1", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Right arm", "2", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Left arm", "3", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Torso", "4", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(4,"Right leg", "5-7", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(5,"Left leg", "8-10", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(5, b.comment)
            }
            WINGED_QUADRUPED -> {
                showBodyPart(0,"Head", "1", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Right wing", "2", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Left wing", "3", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Right forelimb", "4", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(4,"Left forelimb", "5", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(5,"Body", "6-7", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(6,"Right hindlimb", "8", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(7,"Left hindlimb", "9", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(8,"Tail", "10", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(8, b.comment)
            }
            SNAKE -> {
                showBodyPart(0,"Head", "1-2", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Upper body", "3-5", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Middle", "6-7", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Lower body", "8-9", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(4,"Tail", "10", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(4, b.comment)
            }
            SPIRIT -> {
                showBodyPart(0,"Spirit", "N/A", o.hp, o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(0, b.comment)
            }
            CENTAUR -> {
                myView.findViewById<TextView>(R.id.bod_hit_die).text = getString(R.string.c_bp_d20)
                showBodyPart(0,"Head", "1-2", (o.hp fmul 0.2), o.arm, b.worn_armor)
                showBodyPart(1,"Right arm", "3-4", (o.hp fmul 0.2), o.arm, b.worn_armor)
                showBodyPart(2,"Left arm", "5-6", (o.hp fmul 0.2), o.arm, b.worn_armor)
                showBodyPart(3,"Torso", "7-10", (o.hp fmul 0.4), o.arm, b.worn_armor)
                showBodyPart(4,"Right front leg", "11-12", (o.hp fmul 0.3), o.arm, b.worn_armor)
                showBodyPart(5,"Left front leg", "13-14", (o.hp fmul 0.3), o.arm, b.worn_armor)
                showBodyPart(6,"Body", "15-18", (o.hp fmul 0.7), o.arm, b.worn_armor)
                showBodyPart(7,"Right hind leg", "19", (o.hp fmul 0.3), o.arm, b.worn_armor)
                showBodyPart(8,"Left hind leg", "20", (o.hp fmul 0.3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(8, b.comment)
            }
            WINGED_HUMANOID -> {
                myView.findViewById<TextView>(R.id.bod_hit_die).text = getString(R.string.c_bp_d20)
                showBodyPart(0,"Head", "1-2", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(1,"Right arm", "3-4", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(2,"Left arm", "5-6", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(3,"Torso", "7-10", (o.hp fdiv 2), o.arm, b.worn_armor)
                showBodyPart(4,"Right wing", "11-12", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(5,"Left wing", "13-14", (o.hp fdiv 4), o.arm, b.worn_armor)
                showBodyPart(6,"Right leg", "15-17", (o.hp fdiv 3), o.arm, b.worn_armor)
                showBodyPart(7,"Left leg", "18-20", (o.hp fdiv 3), o.arm, b.worn_armor)
                if (b.comment.isNotEmpty()) showBodyComment(7, b.comment)
            }
            else -> error("Unknown type of body defined! Found ${monster.combat.body.type}.")
        }

        at.forEachIndexed { pos, attack -> showAttack(pos, attack, o.db) }

        return myView
    }

    private fun showBodyPart(pos: Int, part: String, chance: String, hp: Int, natural_armor: Int, worn_armor: List<Int>) {
        val tr = myView.findViewById<TableLayout>(R.id.monsterCombatBodyTable).getChildAt(pos + 2) as TableRow
        tr.visibility = TableRow.VISIBLE
        when { pos % 2 == 1 -> tr.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
        (tr.getChildAt(0) as TextView).text = part
        (tr.getChildAt(1) as TextView).text = chance
        (tr.getChildAt(2) as TextView).text = hp.toString()

        val armorString = when {
            natural_armor > 0 && worn_armor[pos] > 0 -> "${worn_armor[pos]}+${natural_armor}"
            natural_armor > 0 -> "${natural_armor}"
            worn_armor[pos] > 0 -> "${worn_armor[pos]}"
            else -> ""
        }
        (tr.getChildAt(3) as TextView).text = armorString
    }

    private fun showBodyComment(lastUsedPos: Int, comment: String) {
        val trComment = myView.findViewById<TableRow>(R.id.bp_comment)
        trComment.visibility = TableRow.VISIBLE
        when { (lastUsedPos + 1) % 2 == 1 -> trComment.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
        (trComment.getChildAt(0) as TextView).text = comment
    }

    private fun showAttack(pos: Int, attack: Attacks, db: Int) {
        val t = myView.findViewById<TableLayout>(R.id.monsterCombatAttackTable)
        val trAtt = t.getChildAt((pos*2)+2) as TableRow
        val trCom = t.getChildAt((pos*2+1)+2) as TableRow
        val dbValue = when {
            db > 0 -> "+${db}"
            db < 0 -> "-${db}"
            else -> ""
        }
        trAtt.visibility = TableRow.VISIBLE
        when { pos % 2 == 1 -> trAtt.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
        (trAtt.getChildAt(0) as TextView).text = attack.type
        (trAtt.getChildAt(1) as TextView).text = getString(R.string.skill, attack.skill)
        val damageString = if (attack.db) attack.damage+dbValue else attack.damage
        (trAtt.getChildAt(2) as TextView).text = damageString
        if (attack.comment.isNotEmpty()) {
            trAtt.setPadding(trAtt.paddingLeft, trAtt.paddingTop, trAtt.paddingRight,0)
            trCom.setPadding(trCom.paddingLeft,0, trCom.paddingRight, trCom.paddingBottom)
            when { pos % 2 == 1 -> trCom.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.rm_table_dark) }
            trCom.visibility = TableRow.VISIBLE
            (trCom.getChildAt(0) as TextView).text = attack.comment
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(monster: Monster) =
            MonsterCombatFragment(monster).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER, monster)
                }
            }
    }
}
