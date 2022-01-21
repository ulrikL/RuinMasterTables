// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import java.util.regex.Pattern

private const val ARG_HEADER = "ARG_HEADER"
private const val ARG_TERRAIN = "ARG_TERRAIN"
private const val ARG_ENCOUNTER = "ARG_ENCOUNTER"
private const val ARG_TREASURE =  "ARG_TREASURE"

class DisplayTableResultFragment : DialogFragment() {
    private var headerText: String? = null
    private var terrainText: String? = null
    private var encounterText: String? = null
    private var treasureText: String? = null
    private lateinit var myView: View
    private lateinit var myContext: Context
    private lateinit var monsterTagIdMap : Map<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            headerText = it.getString(ARG_HEADER)
            terrainText = it.getString(ARG_TERRAIN)
            encounterText = it.getString(ARG_ENCOUNTER)
            treasureText = it.getString(ARG_TREASURE)
        }
        monsterTagIdMap = (activity as MainActivity).getMonsterTagMap()
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
        myView = inflater.inflate(R.layout.fragment_display_result, container, false)
        val header = myView.findViewById<TextView>(R.id.tvHeaderText)
        val terrain = myView.findViewById<TextView>(R.id.tvTerrainText)
        val encounter = myView.findViewById<TextView>(R.id.tvEncounterText)
        val treasure = myView.findViewById<TextView>(R.id.tvTreasureText)
        val dismissButton = myView.findViewById<AppCompatButton>(R.id.btnDismiss)

        header.text = arguments?.getString(ARG_HEADER)
        terrain.text = arguments?.getString(ARG_TERRAIN)
        encounter.text = arguments?.getString(ARG_ENCOUNTER)
        treasure.text = arguments?.getString(ARG_TREASURE)

        setClickableTags(terrain)
        setClickableTags(encounter)

        dismissButton.setOnClickListener { dismiss() }

        return myView
    }

    override fun onResume() {
        super.onResume()
        setWidthPercent(95)
    }

    private fun countMatches(string: String, pattern: String): Int {
        return string.split(pattern, ignoreCase = true)
            .dropLastWhile { it.isEmpty() }
            .toTypedArray().size - 1
    }

    private fun setClickableTags(clickableTextView: TextView) {
        val str = clickableTextView.text.toSpannable()

        debug("Parsing '${str}'")

        // Go through the tags in length order to resolve the 'giant wolf' vs 'wolf' problem
        val foundTags = (monsterTagIdMap.keys.filter { str.contains(it, ignoreCase = true) }).sortedByDescending { it.length }
        if (foundTags.isNotEmpty()) {
            foundTags.forEach { tag ->
                val monsterId = monsterTagIdMap.getOrDefault(tag, -1)
                debug("Found tag=$tag with id=$monsterId ${countMatches(str.toString(), tag)} times.")

                val matcher = Pattern.compile(tag, Pattern.CASE_INSENSITIVE).matcher(str)

                while (matcher.find()) {
                    val matchStart = matcher.start(0)
                    val matchEnd = matcher.end()
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            Log.d("RuinMastersTables::DisplayTableResultFragment", "Clicked on $tag.")
                            if (monsterId > 0) {
                                val monster = (activity as MainActivity).getMonster(monsterId)
                                if (monster != null) {
                                    debug("Show monster with id=${monster.id} and name='${monster.name}'")
                                    val displayMonsterFragment: DisplayMonsterFragment = DisplayMonsterFragment.newInstance(monster)
                                    displayMonsterFragment.show(requireActivity().supportFragmentManager, "fragment_display_monster")
                                } else {
                                    Log.d("RuinMastersTables::DisplayTableResultFragment","Failed to get monster.")
                                }
                            } else {
                                warning("Invalid monster Id=monsterId")
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = ContextCompat.getColor(myContext, R.color.rm_text_dark)//Color.parseColor("#689899")
                            ds.isFakeBoldText = true
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }
                    debug("Set span for tag=$tag starting at $matchStart and ending at $matchEnd.")
                    str.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        } else {
            debug("No tags found in text.")
        }

        // Make the text view text clickable
        clickableTextView.movementMethod = LinkMovementMethod()
        clickableTextView.text = str
    }

    private fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun warning(message: String) {
        if (BuildConfig.DEBUG) Log.w("RuinMastersTables::DisplayTableResultFragment", message)
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("RuinMastersTables::DisplayTableResultFragment", message)
    }

    companion object {
        @JvmStatic
        fun newInstance(headerText: String, terrainText: String, encounterText: String, treasureText: String) =
            DisplayTableResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_HEADER, headerText)
                    putString(ARG_TERRAIN, terrainText)
                    putString(ARG_ENCOUNTER, encounterText)
                    putString(ARG_TREASURE, treasureText)
                }
            }
    }
}
