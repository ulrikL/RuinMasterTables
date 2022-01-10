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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedReader
import java.io.InputStream
import java.io.Serializable
import kotlin.random.Random

data class MonsterData (var monster : List<Monster>) : Serializable
data class Monster (var id : Int, var name : String, var tags : List<String>, var stats : Stats, var combat : Combat, var notes : List<Notes>)
data class Stats (var traits : Traits, var skills : Skills, var other : Other)
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
//            var terrainText : String = triggerTable(monsterData.tables, v.tag as Int)
//            terrainText = terrainText.trimStart()
//            if (terrainText.isNotBlank()) terrainText = replaceDieRolls(terrainText)
//
//            var encounterText : String = triggerTable(monsterData.tables, ((v.tag as Int)+ENCOUNTER_TABLE_OFFSET))
//            encounterText = encounterText.trimStart()
//            if (encounterText.isNotBlank()) encounterText = replaceDieRolls(encounterText)
//
//            var treasureText : String = triggerTable(monsterData.tables, ((v.tag as Int)+TREASURE_TABLE_OFFSET))
//            treasureText = treasureText.trimStart()
//            if (treasureText.isNotBlank()) treasureText = replaceDieRolls(treasureText)
//
//            showEditDialog((v as Button).text.toString(), terrainText, encounterText, treasureText)
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
                dynamicButton.typeface = Typeface.createFromAsset(myView.context.assets, "fonts/BarcelonaITCStd-Medium.otf")
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
        val regex = "\\[\\b(\\d*)d(\\d*)([\\+\\-]*)(\\d*)]".toRegex()
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

    private fun showMonsterDialog(monster: Monster) {
        debug("Show monster with id=${monster.id} and name=${monster.name}")
//        val displayMonsterFragment: DisplayMonsterFragment = DisplayMonsterFragment.newInstance(monster)
//        displayMonsterFragment.show(requireActivity().supportFragmentManager, "fragment_monster_result")
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
