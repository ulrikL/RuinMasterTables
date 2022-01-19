// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import java.io.Serializable
import kotlin.random.Random

data class TableData (var buttons : List<Buttons>, var tables : List<Tables>) : Serializable
data class Buttons (var text : String, var table : Int)
data class Tables (var id : Int, var name : String, var options : List<Options>)
data class Options (var chance : Int, var table : ArrayList<Int>, var text : String)

private const val ENCOUNTER_TABLE_OFFSET = 25
private const val TREASURE_TABLE_OFFSET = 50
const val CONFIG_FILE_TABLE_ID_OFFSET = 100
const val DEFAULT_CHANCE_SUM = 10

private const val ARG_TABLE_DATA =  "ARG_TABLE_DATA"
private const val ARG_TABLE_DATA_FILES =  "ARG_TABLE_DATA_FILES"

class TablesFragment(tables: TableData, files: ArrayList<String>) : Fragment(), View.OnClickListener {
    private val buttonId : Int =  View.generateViewId()
    private var tableData : TableData = tables
    private var tableFiles = files
    private lateinit var myContext: Context
    private lateinit var myView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            tableData = it.getSerializable(ARG_TABLE_DATA) as TableData
            tableFiles = it.getStringArrayList(ARG_TABLE_DATA_FILES) as ArrayList<String>
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
        myView = inflater.inflate(R.layout.fragment_tables, container, false)

        //Draw the UI based on the loaded data
        createButtons(tableData.buttons)

        return myView
    }

    override fun onClick(v: View) {
        if (v.id == buttonId) {
            debug("Clicked button to trigger table ${getActualTableId(v.tag as Int)} in '${getConfigFileName(v.tag as Int)}'.")
            var terrainText : String = triggerTable(tableData.tables, v.tag as Int)
            terrainText = terrainText.trimStart()
            if (terrainText.isNotBlank()) terrainText = replaceDieRolls(terrainText)

            var encounterText : String = triggerTable(tableData.tables, ((v.tag as Int)+ENCOUNTER_TABLE_OFFSET))
            encounterText = encounterText.trimStart()
            if (encounterText.isNotBlank()) encounterText = replaceDieRolls(encounterText)

            var treasureText : String = triggerTable(tableData.tables, ((v.tag as Int)+TREASURE_TABLE_OFFSET))
            treasureText = treasureText.trimStart()
            if (treasureText.isNotBlank()) treasureText = replaceDieRolls(treasureText)

            showEditDialog((v as Button).text.toString(), terrainText, encounterText, treasureText)
        }
    }

    private fun getActualTableId(tableId : Int) : Int {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return (tableId - CONFIG_FILE_TABLE_ID_OFFSET * configFileIndex)
    }

    private fun getConfigFileName(tableId : Int) : String {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return tableFiles.elementAtOrElse(configFileIndex){"unknown file"}
    }

    private fun createButtons(buttons : List<Buttons>) {
        val linearLayout = myView.findViewById<LinearLayout>(R.id.buttons_layout)

        if (linearLayout != null) {
            buttons.forEach { button ->
                val dynamicButton = Button(myView.context)
                dynamicButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                dynamicButton.text = button.text
                dynamicButton.id = buttonId
                dynamicButton.tag = button.table
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

    private fun triggerTable(tables : List<Tables>, tableId : Int) : String {
        var resultingText  = ""
        var foundTable  = false

        for (table in tables) {
            if (table.id == tableId) {
                foundTable = true
                debug("Found table named '${table.name}' with id=${getActualTableId(tableId)} in '${getConfigFileName(tableId)}'.")
                val optionResult : Pair<List<Int>,String> = getRandomOption(table.options)

                if (optionResult.second.isNotBlank()) {
                    resultingText += if (resultingText.isNotBlank()) { " ${optionResult.second}" } else { optionResult.second }
                }

                if (optionResult.first.isNotEmpty()) {
                    optionResult.first.forEach { id ->
                        debug("Continue with table id=${getActualTableId(id)} in '${getConfigFileName(id)}'.")
                        val tableText : String = triggerTable(tables, id)
                        if (tableText.isNotBlank()) {
                            resultingText += if (resultingText.isNotBlank()) { " $tableText" } else { tableText }
                        }
                    }
                }
                break
            }
        }

        if (!foundTable) warning("Did not find a table with id=${getActualTableId(tableId)} in '${getConfigFileName(tableId)}'.")
        return resultingText
    }

    private fun getRandomOption(options : List<Options>) : Pair<List<Int>,String> {
        var sumOfAllChance = 0
        var sumOfChance = 0

        options.forEach { option ->
            sumOfAllChance += option.chance
        }
        val randomChance : Int = getRandomInt(1, sumOfAllChance)
        debug("Got $randomChance out of $sumOfAllChance when getting a random option")
        for (option in options) {
            sumOfChance += option.chance
            if (randomChance <= sumOfChance) return Pair(option.table, option.text)
        }
        debug("Failed to get an option")
        return Pair(emptyList(),"")
    }

    private fun getRandomInt(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        val rand = Random(System.nanoTime())
        return (start..end).random(rand)
    }

    private fun replaceDieRolls(text : String): String {
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
                debug("Got $dieResult and replace ${match.value} with this value.")
                localText = localText.replaceRange(match.range, dieResult.toString())
                debug("Updated text='$localText'")
            }
            match = regex.find(localText)
        }
        return localText
    }

    private fun showEditDialog(headerText: String, terrainText: String, encounterText: String, treasureText: String) {
        val displayResultFragment: DisplayResultFragment = DisplayResultFragment.newInstance(headerText, terrainText, encounterText, treasureText)
        displayResultFragment.show(requireActivity().supportFragmentManager, "fragment_display_result")
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::TableFragment", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::TableFragment", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::TableFragment", message)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment TableFragment.
         */
        @JvmStatic
        fun newInstance(tables : TableData, files : ArrayList<String>) =
            TablesFragment(tables, files).apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TABLE_DATA, tables)
                    putStringArrayList(ARG_TABLE_DATA_FILES, files)
                }
            }
    }
}
