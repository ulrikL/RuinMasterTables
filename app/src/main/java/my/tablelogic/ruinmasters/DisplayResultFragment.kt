// Copyright (c) 2021 Ulrik Laurén
// Part of RuinMastersTables
// MIT License, see LICENSE file

package my.tablelogic.ruinmasters

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

private const val ARG_HEADER = "ARG_HEADER"
private const val ARG_TERRAIN = "ARG_TERRAIN"
private const val ARG_ENCOUNTER = "ARG_ENCOUNTER"
private const val ARG_TREASURE =  "ARG_TREASURE"

class DisplayResultFragment : DialogFragment() {
    private var headerText: String? = null
    private var terrainText: String? = null
    private var encounterText: String? = null
    private var treasureText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            headerText = it.getString(ARG_HEADER)
            terrainText = it.getString(ARG_TERRAIN)
            encounterText = it.getString(ARG_ENCOUNTER)
            treasureText = it.getString(ARG_TREASURE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_display_result, container, false)
        val header = v.findViewById<TextView>(R.id.tvHeaderText)
        val terrain = v.findViewById<TextView>(R.id.tvTerrainText)
        val encounter = v.findViewById<TextView>(R.id.tvEncounterText)
        val treasure = v.findViewById<TextView>(R.id.tvTreasureText)
        val dismissButton = v.findViewById<AppCompatButton>(R.id.btnDismiss)

        header.text = arguments?.getString(ARG_HEADER)
        terrain.text = arguments?.getString(ARG_TERRAIN)
        encounter.text = arguments?.getString(ARG_ENCOUNTER)
        treasure.text = arguments?.getString(ARG_TREASURE)
        dismissButton.setOnClickListener { dismiss() }

        return v
    }

    override fun onResume() {
        super.onResume()
        setWidthPercent(95)
    }

    private fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        @JvmStatic
        fun newInstance(headerText: String, terrainText: String, encounterText: String, treasureText: String) =
            DisplayResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_HEADER, headerText)
                    putString(ARG_TERRAIN, terrainText)
                    putString(ARG_ENCOUNTER, encounterText)
                    putString(ARG_TREASURE, treasureText)
                }
            }
    }
}
