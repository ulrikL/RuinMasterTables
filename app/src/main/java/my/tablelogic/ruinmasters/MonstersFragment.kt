// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.Serializable
import kotlin.math.roundToInt
import kotlin.random.Random

data class MonsterData (var monster : List<Monster>) : Serializable
data class Monster (var id : Int, var name : String, var tags : List<String>, var stats : Stats, var combat : Combat, var notes : List<Notes>) : Serializable {
    fun deepCopy():Monster {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(mapper.writeValueAsString(this), Monster::class.java)
    }
}
data class Stats (var traits : Traits, var skills : Skills, var other : Other, var move : Move)
data class Move (var land : Int, var air : Int)
data class Traits (var phy : String, var min : String, var int : String, var cha : String)
data class Skills (var bur : Int, var kno : Int, var mag : Int, var mel : Int, var soc : Int, var sur : Int)
data class Other (var siz : Double, var hp : Int, var car : Int, var db : Int, var act : Int, var arm : Int)
data class Combat (var body : String, var attacks : List<Attacks>, var abilities : List<Abilities>)
data class Attacks (var type : String, var skill : Int, var damage : String, var db : Boolean)
data class Abilities (var type : String, var description : String)
data class Notes (var header : String, var text : String)

const val MONSTER_FILE_ID_OFFSET = 10

private const val ARG_MONSTER_DATA =  "ARG_MONSTER_DATA"
private const val ARG_MONSTER_DATA_FILES =  "ARG_MONSTER_DATA_FILES"

const val HUMANOID = "humanoid"
const val QUADRUPED = "quadruped"
const val GIANT = "giant"
const val WINGED_QUADRUPED = "winged_quadruped"
const val SNAKE = "snake"
const val SPIRIT = "spirit"
const val CENTAUR = "centaur"
const val WINGED_HUMANOID = "winged_humanoid"

class MonstersFragment(monsters: MonsterData, files: ArrayList<String>) : Fragment(), View.OnClickListener {
    private val buttonId : Int =  View.generateViewId()
    private var monsterData = monsters
    private var monsterFiles = files
    private lateinit var myContext: Context
    private lateinit var myView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            monsterData = it.getSerializable(ARG_MONSTER_DATA) as MonsterData
            monsterFiles = it.getStringArrayList(ARG_MONSTER_DATA_FILES) as ArrayList<String>
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
        myView = inflater.inflate(R.layout.fragment_monsters, container, false)

        //Draw the UI based on the loaded data
        createMonsterButtons(monsterData.monster)

        return myView
    }

    override fun onClick(v: View) {
        if (v.id == buttonId) {
            debug("Clicked button to show monster ${getActualMonsterId(v.tag as Int)} in '${getMonsterFileName(v.tag as Int)}'.")
            monsterData.monster.forEach { monster ->
                if (monster.id == v.tag) {
                    showMonsterDialog(rollRandomValues(monster.deepCopy()))
                    return
                }
            }
            error("Did not find id='${v.tag}' monsterData. Should be '${getActualMonsterId(v.tag as Int)}' in '${getMonsterFileName(v.tag as Int)}'.")
        }
    }

    private fun getActualMonsterId(monsterId : Int) : Int {
        var id : Int = monsterId

        if (monsterId > MONSTER_FILE_ID_OFFSET) {
            val monsterFileIndex = (monsterId / MONSTER_FILE_ID_OFFSET)
            id = (monsterId - MONSTER_FILE_ID_OFFSET * monsterFileIndex)
        }

        return id
    }

    private fun getMonsterFileName(monsterId : Int) : String {
        var monsterFileIndex = 0

        if (monsterId > MONSTER_FILE_ID_OFFSET) {
            monsterFileIndex = (monsterId / MONSTER_FILE_ID_OFFSET)
        }

        return monsterFiles.elementAtOrElse(monsterFileIndex) { "unknown file" }
    }

    private fun createMonsterButtons(monsters : List<Monster>) {
        val linearLayout = myView.findViewById<LinearLayout>(R.id.monster_buttons_layout)

        if (linearLayout != null) {
            monsters.forEach { monster ->
                val dynamicButton = Button(myView.context)
                dynamicButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                dynamicButton.text = monster.name
                dynamicButton.id = buttonId
                dynamicButton.tag = monster.id
                dynamicButton.minimumWidth = 700
                dynamicButton.setPadding(0, 40, 0, 40)
                dynamicButton.textSize = 20.0F
                dynamicButton.typeface = resources.getFont(R.font.button)
                dynamicButton.backgroundTintList = ContextCompat.getColorStateList(myView.context, R.color.rm_table_dark)
                dynamicButton.setOnClickListener(this)
                linearLayout.addView(dynamicButton)
            }
        } else {
            error("Cannot find the layout ot add buttons to!")
        }
    }

    private fun getRandomInt(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        val rand = Random(System.nanoTime())
        return (start..end).random(rand)
    }

    private fun replaceDieRollsWithModifier(text : String): String {
        var localText : String = text
        val regex = "\\[\\b(\\d*)d(\\d*)([+\\-]*)(\\d*)]".toRegex()
        var match = regex.find(localText)

        while (match != null) {
            if (match.value.isNotBlank()) {
                val numberOfDice : Int = match.groupValues[1].toInt()
                val diceType : Int = match.groupValues[2].toInt()
                val modifyAdd : Boolean = (match.groupValues[3]=="+")
                val modifyRemove : Boolean = (match.groupValues[3]=="-")
                val modifier : Int = if (match.groupValues[4] != "") match.groupValues[4].toInt() else 0
                var dieResult = 0
                debug("Found '${match.value}' with group1=$numberOfDice and group2=$diceType in string.")
                for (i in 1..numberOfDice) dieResult += getRandomInt(1, diceType)
                if (modifyAdd) dieResult += modifier
                if (modifyRemove) dieResult -= modifier
                debug("Rolled $dieResult and replace $match with this value.")
                localText = localText.replaceRange(match.range, dieResult.toString())
                debug("Updated text='$localText'")
            }
            match = regex.find(localText)
        }
        return localText
    }

    private fun rollRandomValues(monster: Monster) : Monster {
        monster.stats.traits.phy = replaceDieRollsWithModifier(monster.stats.traits.phy)
        monster.stats.traits.min = replaceDieRollsWithModifier(monster.stats.traits.min)
        monster.stats.traits.int = replaceDieRollsWithModifier(monster.stats.traits.int)
        monster.stats.traits.cha = replaceDieRollsWithModifier(monster.stats.traits.cha)

        if (monster.stats.other.hp == 0) {
            monster.stats.other.hp =
                (((monster.stats.traits.phy.toInt() + monster.stats.traits.min.toInt() + getRandomInt(1,10)).toDouble()) * monster.stats.other.siz).roundToInt()
        }
        if (monster.stats.other.car == 0) {
            monster.stats.other.car = (monster.stats.traits.phy.toDouble() * monster.stats.other.siz * monster.stats.other.siz).roundToInt()
        }
        monster.stats.other.db = when {
            monster.stats.traits.phy.toInt() in 21..25 -> 1
            monster.stats.traits.phy.toInt() in 26..28 -> 2
            monster.stats.traits.phy.toInt() > 28 -> 3
            else -> 0
        }
        return monster
    }

    private fun showMonsterDialog(monster: Monster) {
        debug("Show monster with id=${monster.id} and name='${monster.name}'")
        val displayMonsterFragment: DisplayMonsterFragment = DisplayMonsterFragment.newInstance(monster)
        displayMonsterFragment.show(requireActivity().supportFragmentManager, "fragment_display_monster")
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::MonsterFragment", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::MonsterFragment", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::MonsterFragment", message)
    }

    companion object {
        @JvmStatic
        fun newInstance(monsters : MonsterData, files : ArrayList<String>) =
            MonstersFragment(monsters, files).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MONSTER_DATA, monsters)
                    putStringArrayList(ARG_MONSTER_DATA_FILES, files)
                }
            }
    }
}
