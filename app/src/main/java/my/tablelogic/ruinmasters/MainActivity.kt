// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.BufferedReader
import java.io.InputStream

class PageAdapter(fm:FragmentManager, lc:Lifecycle, td:ConfigurationData, tf:ArrayList<String>, md:MonsterData, mf:ArrayList<String>) : FragmentStateAdapter(fm, lc) {
    private val tableData = td
    private val tableFiles = tf
    private val monsterData = md
    private val monsterFiles = mf

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> { return TablesFragment.newInstance(tableData, tableFiles) }
            1 -> { return MonstersFragment.newInstance(monsterData, monsterFiles) }
        }
        return TablesFragment.newInstance(tableData, tableFiles)
    }
}

class MainActivity : AppCompatActivity() {
    private inline fun <reified T> ObjectMapper.readValue(s: String): T = this.readValue(s, object : TypeReference<T>() {})

    private lateinit var tables : Pair<ConfigurationData, ArrayList<String>>
    private lateinit var monsters : Pair<MonsterData, ArrayList<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tables = loadTableData()
        isConfigurationDataValid(tables.first, tables.second)
        monsters = loadMonsterData()

        (findViewById<TextView>(R.id.custom_title)).typeface = Typeface.createFromAsset(assets, "fonts/Becker.ttf")

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = PageAdapter(supportFragmentManager, lifecycle, tables.first, tables.second, monsters.first, monsters.second)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                0 -> tab.text = "Tables"
                1 -> tab.text = "Monsters"
            }
        }.attach()
        tabLayout.setFont()
    }

    private fun loadTableData() : Pair<ConfigurationData, ArrayList<String>> {
        val loadedConfigurationData = ConfigurationData(emptyList(), emptyList())
        val loadedConfigurationFiles = ArrayList<String>()

        val files = assets.list("tables")
        if (files != null) {
            for ((index, file) in files.withIndex()) {
                if (file.endsWith(".json")) {
                    debug("Found configuration file named '$file'")
                    loadedConfigurationFiles += file.toString()
                    val tempConfigData = loadConfiguration(assets.open("tables/$file"))
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
                    loadedConfigurationData.buttons += tempConfigData.buttons
                    loadedConfigurationData.tables += tempConfigData.tables
                }
            }
        }  else {
            error("Cannot find/open *.json files in assets!")
        }

        return Pair(loadedConfigurationData, loadedConfigurationFiles)
    }

    private fun isConfigurationDataValid( tableData : ConfigurationData, tableDataFiles: ArrayList<String>) {
        val allIds = ArrayList<Int>()
        val allTableOptions = ArrayList<Int>()

        tableData.tables.forEach{ table ->
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
                error(getTableNameFromId(it.key, tableData, tableDataFiles))
            }
        }
        val allDistinctTableOptions = allTableOptions.distinct()
        val missingTableIds = ArrayList<Int>()
        allDistinctTableOptions.forEach{
            if (it !in allIds) {
                missingTableIds += it
            }
        }
        if (missingTableIds.isNotEmpty()) {
            error("There are options using undefined table IDs!")
            missingTableIds.forEach {
                error(getTableNameFromUsedOptionsTableReferences(it, tableData, tableDataFiles))
            }
        }
        tableData.tables.forEach{ table ->
            var sumOfChance = 0
            table.options.forEach{ option ->
                sumOfChance += option.chance
                if (option.text.count { c -> c == '[' } > option.text.count { c -> c == ']' }) {
                    error("'${table.name}' (${table.id}) in '${getConfigFileName(table.id, tableDataFiles)}' seem to miss a ']' in '${option.text}'.")
                } else if (option.text.count { c -> c == '[' } < option.text.count { c -> c == ']' }) {
                    error("'${table.name}' (${table.id}) in '${getConfigFileName(table.id, tableDataFiles)}' seem to miss a '[' in '${option.text}'.")
                }
            }
            if (sumOfChance != DEFAULT_CHANCE_SUM) {
                warning("'${table.name}' (${getActualTableId(table.id)}) in '${getConfigFileName(table.id, tableDataFiles)}' does not use a default chance (sum is $sumOfChance).")
            }
        }
    }

    private fun getTableNameFromId(id : Int, tableData : ConfigurationData, tableDataFiles: ArrayList<String>) : String {
        var foundNames = "ID ${getActualTableId(id)} is used by "
        tableData.tables.forEach{ table ->
            if (table.id == id) {
                foundNames += "'${table.name}' "
            }
        }
        foundNames += "in '${getConfigFileName(id, tableDataFiles)}'."
        return foundNames
    }

    private fun getTableNameFromUsedOptionsTableReferences(tableId : Int, tableData: ConfigurationData, tableDataFiles: ArrayList<String>) : String {
        var foundNames = "Undefined ID ${getActualTableId(tableId)} is used by "
        tableData.tables.forEach{ table ->
            table.options.forEach { option ->
                option.table.forEach { id ->
                    if (id == tableId) {
                        foundNames += "'${table.name}' "
                    }
                }
            }
        }
        foundNames += "in '${getConfigFileName(tableId, tableDataFiles)}'."
        return foundNames
    }

    private fun getActualTableId(tableId : Int) : Int {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return (tableId - CONFIG_FILE_TABLE_ID_OFFSET * configFileIndex)
    }

    private fun getConfigFileName(tableId : Int, tableDataFiles: ArrayList<String>) : String {
        val configFileIndex = (tableId / CONFIG_FILE_TABLE_ID_OFFSET) - 1
        return tableDataFiles.elementAtOrElse(configFileIndex){"unknown file"}
    }

    private fun loadConfiguration( configStream: InputStream) : ConfigurationData {
        val mapper = jacksonObjectMapper()
        mapper.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true )
        return mapper.readValue(configStream.bufferedReader().use(BufferedReader::readText))
    }

    private fun loadMonsterData() : Pair<MonsterData, ArrayList<String>> {
        val loadedMonsterData = MonsterData(emptyList())
        val loadedMonsterFiles = ArrayList<String>()

        val files = assets.list("monsters")
        if (files != null) {
            for ((index, file) in files.withIndex()) {
                if (file.endsWith(".json")) {
                    debug("Found monster file named '$file'")
                    loadedMonsterFiles += file.toString()
                    val tempMonsterData = loadMonsterData(assets.open("monsters/$file"))
                    //Each file get an offset to allow multiple files. This is done to make it easy to split the files
                    tempMonsterData.monster.forEach { monster ->
                        monster.id += index*MONSTER_FILE_ID_OFFSET
                    }

                    //Put all of the loaded and modified data into the data structures
                    loadedMonsterData.monster += tempMonsterData.monster
                }
            }
        }  else {
            error("Cannot find/open *.json files in assets!")
        }

        return Pair(loadedMonsterData, loadedMonsterFiles)
    }

    private fun loadMonsterData( monsterStream: InputStream) : MonsterData {
        val mapper = jacksonObjectMapper()
        mapper.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true )
        return mapper.readValue(monsterStream.bufferedReader().use(BufferedReader::readText))
    }

    private fun TabLayout.setFont() {
        val viewGroup = getChildAt(0) as ViewGroup
        val tabsCount = viewGroup.childCount
        for (j in 0 until tabsCount) {
            val viewGroupChildAt = viewGroup.getChildAt(j) as ViewGroup
            val tabChildCount = viewGroupChildAt.childCount
            for (i in 0 until tabChildCount) {
                val tabViewChild = viewGroupChildAt.getChildAt(i)
                if (tabViewChild is TextView) {
                    tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/BarcelonaITCStd-Bold.otf")
                }
            }
        }
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