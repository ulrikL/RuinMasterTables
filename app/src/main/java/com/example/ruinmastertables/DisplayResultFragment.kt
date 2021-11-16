package com.example.ruinmastertables

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

private const val ARG_TERRAIN = "ARG_TERRAIN"
private const val ARG_ENCOUNTER = "ARG_ENCOUNTER"
private const val ARG_TREASURE =  "ARG_TREASURE"

class DisplayResultFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var terrainText: String? = null
    private var encounterText: String? = null
    private var treasureText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            terrainText = it.getString(ARG_TERRAIN)
            encounterText = it.getString(ARG_ENCOUNTER)
            treasureText = it.getString(ARG_TREASURE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_display_result, container, false)

        (v.findViewById<View>(R.id.tvTerrainText) as TextView).text = arguments?.getString(ARG_TERRAIN)
        (v.findViewById<View>(R.id.tvEncounterText) as TextView).text = arguments?.getString(ARG_ENCOUNTER)
        (v.findViewById<View>(R.id.tvTreasureText) as TextView).text = arguments?.getString(ARG_TREASURE)
        (v.findViewById<View>(R.id.btnDismiss) as AppCompatButton).setOnClickListener { dismiss() }

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

    private fun setFullScreen() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        @JvmStatic
        fun newInstance(terrainText: String, encounterText: String, treasureText: String) =
            DisplayResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TERRAIN, terrainText)
                    putString(ARG_ENCOUNTER, encounterText)
                    putString(ARG_TREASURE, treasureText)
                }
            }
    }
}