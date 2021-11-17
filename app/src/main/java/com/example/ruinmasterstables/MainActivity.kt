// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package com.example.ruinmasterstables

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.*
import android.view.View
import android.widget.LinearLayout
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStream
import kotlin.random.Random

data class ConfigurationData (val buttons : List<Buttons>, val tables : List<Tables>)
data class Buttons (val text : String, val table : Int)
data class Tables (val id : Int, val name : String, val options : List<Options>)
data class Options (val chance : Int, val table : List<Int>, val text : String)

private const val ENCOUNTER_TABLE_OFFSET = 25
private const val TREASURE_TABLE_OFFSET = 50

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private inline fun <reified T> ObjectMapper.readValue(s: String): T = this.readValue(s, object : TypeReference<T>() {})
    private val buttonId : Int =  View.generateViewId()
    private lateinit var configData : ConfigurationData

    override fun onClick(v: View) {
        if (v.id == buttonId) {
            debug("Clicked button for table ${v.tag}")
            var terrainText : String = triggerTable(configData.tables, v.tag as Int)
            terrainText = terrainText.trimStart()
            if (terrainText.isNotBlank()) terrainText = replaceDieRolls(terrainText)

            var encounterText : String = triggerTable(configData.tables, ((v.tag as Int)+ENCOUNTER_TABLE_OFFSET))
            encounterText = encounterText.trimStart()
            if (encounterText.isNotBlank()) encounterText = replaceDieRolls(encounterText)

            var treasureText : String = triggerTable(configData.tables, ((v.tag as Int)+TREASURE_TABLE_OFFSET))
            treasureText = treasureText.trimStart()
            if (treasureText.isNotBlank()) treasureText = replaceDieRolls(treasureText)

            showEditDialog(terrainText, encounterText, treasureText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configData = loadConfiguration(assets.open("ruin_masters_tables.json"))
        createButtons(configData.buttons)
    }

    private fun loadConfiguration( configStream: InputStream ) : ConfigurationData {
        val mapper = jacksonObjectMapper()
        mapper.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true )
        return mapper.readValue(configStream.bufferedReader().use(BufferedReader::readText))
    }

    private fun createButtons(buttons : List<Buttons>) {
        val linearLayout = findViewById<View>(R.id.buttons_layout) as LinearLayout //a constraint layout pre-made in design view

        buttons.forEach {
            val dynamicButton = Button(this)
            dynamicButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dynamicButton.text = it.text
            dynamicButton.id = buttonId
            dynamicButton.tag = it.table
            dynamicButton.minimumWidth = 500
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
                debug("Found table named '${table.name}' with id=$tableId")
                val optionResult : Pair<List<Int>,String> = getRandomOption(table.options)

                if (optionResult.second.isNotBlank()) {
                    resultingText += if (resultingText.isNotBlank()) { " ${optionResult.second}" } else { optionResult.second }
                }

                if (optionResult.first.isNotEmpty()) {
                    optionResult.first.forEach {
                        debug("Continue with table id=$it")
                        val tableText : String = triggerTable(tables, it)
                        if (tableText.isNotBlank()) {
                            resultingText += if (resultingText.isNotBlank()) { " $tableText" } else { tableText }
                        }
                    }
                }
                break
            }
        }

        if (!foundTable) warning("Did not find a table with id=$tableId")
        return resultingText
    }

    private fun getRandomOption(options : List<Options>) : Pair<List<Int>,String> {
        var sumOfAllChance = 0
        var sumOfChance = 0

        options.forEach {
            sumOfAllChance += it.chance
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
                debug("Rolled $dieResult and shall replace range=${match.range} with this value.")
                localText = localText.replaceRange(match.range, dieResult.toString())
                debug("Updated text='$localText'")
            }
            match = regex.find(localText)
        }
        return localText
    }

    private fun showEditDialog(terrainText: String, encounterText: String, treasureText: String) {
        val displayResultFragment: DisplayResultFragment = DisplayResultFragment.newInstance(terrainText, encounterText, treasureText)
        displayResultFragment.show(supportFragmentManager, "fragment_display_result")
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::MainActivity", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::MainActivity", message)
    }
}
