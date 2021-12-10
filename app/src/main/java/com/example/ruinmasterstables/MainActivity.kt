// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package com.example.ruinmasterstables

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.*
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStream
import kotlin.random.Random

data class ConfigurationData (var buttons : List<Buttons>, var tables : List<Tables>)
data class Buttons (var text : String, var table : Int)
data class Tables (var id : Int, var name : String, var options : List<Options>)
data class Options (var chance : Int, var table : ArrayList<Int>, var text : String)

private const val ENCOUNTER_TABLE_OFFSET = 25
private const val TREASURE_TABLE_OFFSET = 50
private const val CONFIG_FILE_TABLE_ID_OFFSET = 100
private const val DEFAULT_CHANCE_SUM = 10

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private inline fun <reified T> ObjectMapper.readValue(s: String): T = this.readValue(s, object : TypeReference<T>() {})
    private val buttonId : Int =  View.generateViewId()
    private lateinit var configData : ConfigurationData
    private var configFiles = ArrayList<String>()

    override fun onClick(v: View) {
        if (v.id == buttonId) {
            debug("Clicked button to trigger table ${getActualTableId(v.tag as Int)} in '${getConfigFileName(v.tag as Int)}'.")
            var terrainText : String = triggerTable(configData.tables, v.tag as Int)
            terrainText = terrainText.trimStart()
            if (terrainText.isNotBlank()) terrainText = replaceDieRolls(terrainText)

            var encounterText : String = triggerTable(configData.tables, ((v.tag as Int)+ENCOUNTER_TABLE_OFFSET))
            encounterText = encounterText.trimStart()
            if (encounterText.isNotBlank()) encounterText = replaceDieRolls(encounterText)

            var treasureText : String = triggerTable(configData.tables, ((v.tag as Int)+TREASURE_TABLE_OFFSET))
            treasureText = treasureText.trimStart()
            if (treasureText.isNotBlank()) treasureText = replaceDieRolls(treasureText)

            showEditDialog((v as Button).text.toString(), terrainText, encounterText, treasureText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById<TextView>(R.id.custom_title)).typeface = Typeface.createFromAsset(assets, "fonts/Becker.ttf")
        (findViewById<TextView>(R.id.tvIntro)).typeface = Typeface.createFromAsset(assets, "fonts/BarcelonaITCStd-Book.otf")

        val files = assets.list("data")
        if (files != null) {
            for ((index, file) in files.withIndex()) {
                if (file.endsWith(".json")) {
                    debug("Found configuration file named '${file.toString()}'")
                    configFiles += file.toString()
                    val tempConfigData = loadConfiguration(assets.open("data/$file"))
                    //Each file get an offset to allow multiple files. This is done to make it easy to split the files
                    tempConfigData.buttons.forEach { button ->
                        button.table += index*CONFIG_FILE_TABLE_ID_OFFSET
                    }
                    tempConfigData.tables.forEach { table ->
                        table.id += index*CONFIG_FILE_TABLE_ID_OFFSET
                        table.options.forEach { option ->
                            option.table.forEachIndexed { pos, value -> option.table[pos] = value + index*CONFIG_FILE_TABLE_ID_OFFSET }
                        }
                    }

                    //Put all of the loaded and modified data into the data structures
                    if (this::configData.isInitialized) {
                        configData.buttons += tempConfigData.buttons
                        configData.tables += tempConfigData.tables
                    } else {
                        configData = tempConfigData
                    }
                }
            }
        }
        isConfigurationDataValid(configData)
        //Draw the UI based on the loaded data
        createButtons(configData.buttons)
    }

    private fun isConfigurationDataValid( configData : ConfigurationData) {
        val allIds = ArrayList<Int>()
        val allTableOptions = ArrayList<Int>()

        configData.tables.forEach{ table ->
            allIds += table.id
            table.options.forEach{ option ->
                if (option.table.isNotEmpty()) {
                    allTableOptions += option.table
                }
            }
        }
        if (allIds.size != allIds.distinct().count()) {
            val duplicatedIds = allIds.groupingBy { it }.eachCount().filter { it.value > 1 }

            error("There are duplicated table IDs!")
            duplicatedIds.forEach {
                error(getTableNameFromId(it.key))
            }
        }
        val allDistinctTableOptions = allTableOptions.distinct()
        val missingTableIds =  ArrayList<Int>()
        allDistinctTableOptions.forEach{
            if (it !in allIds) {
                missingTableIds += it
            }
        }
        if (missingTableIds.isNotEmpty()) {
            error("There are options using undefined table IDs!")
            missingTableIds.forEach {
                error(getTableNameFromUsedOptionsTableReferences(it))
            }
        }
        configData.tables.forEach{ table ->
            var sumOfChance = 0
            table.options.forEach{ option ->
                sumOfChance += option.chance
                if (option.text.count { c -> c == '[' } > option.text.count { c -> c == ']' }) {
                    error("'${table.name}' (${table.id}) in '${getConfigFileName(table.id)}' seem to miss a ']' in '${option.text}'.")
                } else if (option.text.count { c -> c == '[' } < option.text.count { c -> c == ']' }) {
                    error("'${table.name}' (${table.id}) in '${getConfigFileName(table.id)}' seem to miss a '[' in '${option.text}'.")
                }
            }
            if (sumOfChance != DEFAULT_CHANCE_SUM) {
                warning("'${table.name}' (${getActualTableId(table.id)}) in '${getConfigFileName(table.id)}' does not use a default chance (sum is $sumOfChance).")
            }
        }
    }

    private fun getTableNameFromId(id : Int) : String {
        var foundNames = String()
        if (this::configData.isInitialized) {
            foundNames = "ID ${getActualTableId(id)} is used by "
            configData.tables.forEach{ table ->
                if (table.id == id) {
                    foundNames += "'${table.name}' "
                }
            }
            foundNames += "in '${getConfigFileName(id)}'."
        }
        return foundNames
    }

    private fun getTableNameFromUsedOptionsTableReferences(tableId : Int) : String {
        var foundNames = String()
        if (this::configData.isInitialized) {
            foundNames = "Undefined ID ${getActualTableId(tableId)} is used by "
            configData.tables.forEach{ table ->
                table.options.forEach { option ->
                    option.table.forEach { id ->
                        if (id == tableId) {
                            foundNames += "'${table.name}' "
                        }
                    }
                }
            }
            foundNames += "in '${getConfigFileName(tableId)}'."
        }
        return foundNames
    }

    private fun getActualTableId(tableId : Int) : Int {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return (tableId - CONFIG_FILE_TABLE_ID_OFFSET * configFileIndex)
    }

    private fun getConfigFileName(tableId : Int) : String {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return configFiles.elementAtOrElse(configFileIndex){"unknown file"}
    }

    private fun loadConfiguration( configStream: InputStream ) : ConfigurationData {
        val mapper = jacksonObjectMapper()
        mapper.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true )
        return mapper.readValue(configStream.bufferedReader().use(BufferedReader::readText))
    }

    private fun createButtons(buttons : List<Buttons>) {
        val linearLayout = findViewById<LinearLayout>(R.id.buttons_layout)

        buttons.forEach { button ->
            val dynamicButton = Button(this)
            dynamicButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dynamicButton.text = button.text
            dynamicButton.id = buttonId
            dynamicButton.tag = button.table
            dynamicButton.minimumWidth = 700
            dynamicButton.setPadding(0, 40, 0, 40)
            dynamicButton.textSize = 20.0F
            dynamicButton.typeface = Typeface.createFromAsset(assets, "fonts/BarcelonaITCStd-Medium.otf")
            dynamicButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.rm_table_dark)
            dynamicButton.setOnClickListener(this)
            linearLayout.addView(dynamicButton)
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
        val regex = "\\[\\b(\\d*)d(\\d*)]".toRegex()
        var match = regex.find(localText)

        while (match != null) {
            if (match.value.isNotBlank()) {
                val numberOfDice : Int = match.groupValues[1].toInt()
                val diceType : Int = match.groupValues[2].toInt()
                var dieResult = 0
                debug("Found '${match.value}' with group1=$numberOfDice and group2=$diceType in string.")
                for (i in 1..numberOfDice) dieResult += getRandomInt(1, diceType)
                debug("Rolled $dieResult and replace $match with this value.")
                localText = localText.replaceRange(match.range, dieResult.toString())
                debug("Updated text='$localText'")
            }
            match = regex.find(localText)
        }
        return localText
    }

    private fun showEditDialog(headerText: String, terrainText: String, encounterText: String, treasureText: String) {
        val displayResultFragment: DisplayResultFragment = DisplayResultFragment.newInstance(headerText, terrainText, encounterText, treasureText)
        displayResultFragment.show(supportFragmentManager, "fragment_display_result")
    }

    private fun error(message: String) {
        Log.e("RuinMastersTables::MainActivity", message)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::MainActivity", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::MainActivity", message)
    }
}
