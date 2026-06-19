package com.example.developmentofuserinterfaceshw1

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ScoresTableFragment : Fragment() {

    private lateinit var scoresTable: TableLayout
    private lateinit var noScoresText: TextView
    private var selectedRow: TableRow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_scores_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scoresTable = view.findViewById(R.id.scoresTable)
        noScoresText = view.findViewById(R.id.noScoresText)
        renderScores()
    }

    private fun renderScores() {
        val scores = HighScoreStore(requireContext()).getScores().take(MAX_VISIBLE_SCORES)
        scoresTable.removeAllViews()

        if (scores.isEmpty()) {
            noScoresText.visibility = View.VISIBLE
            publishSelection(null)
            return
        }

        noScoresText.visibility = View.GONE
        scoresTable.addView(createHeaderRow())
        addDivider()

        scores.forEachIndexed { index, score ->
            val row = createScoreRow(index + 1, score)
            row.setOnClickListener {
                selectRow(row)
                publishSelection(score)
            }
            scoresTable.addView(row)
            addDivider()

            if (index == 0) {
                selectRow(row)
                publishSelection(score)
            }
        }
    }

    private fun createHeaderRow(): TableRow {
        return TableRow(requireContext()).apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_teal_dark))
            addView(createCell(getString(R.string.rank_header), isHeader = true, weight = 0.8f))
            addView(createCell(getString(R.string.score_header), isHeader = true, weight = 1f))
            addView(createCell(getString(R.string.distance_header), isHeader = true, weight = 1.2f))
            addView(createCell(getString(R.string.date_time_header), isHeader = true, weight = 2f))
        }
    }

    private fun createScoreRow(rank: Int, score: HighScore): TableRow {
        return TableRow(requireContext()).apply {
            isClickable = true
            isFocusable = true
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
            addView(createCell(rank.toString(), weight = 0.8f, gravity = Gravity.CENTER))
            addView(createCell(score.score.toString(), weight = 1f, gravity = Gravity.CENTER))
            addView(createCell("${score.distance} m", weight = 1.2f, gravity = Gravity.CENTER))
            addView(createCell(score.dateTime, weight = 2f, gravity = Gravity.CENTER_VERTICAL))
        }
    }

    private fun createCell(
        text: String,
        isHeader: Boolean = false,
        weight: Float,
        gravity: Int = Gravity.CENTER
    ): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.gravity = gravity
            textSize = if (isHeader) 13f else 12f
            typeface = if (isHeader) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isHeader) R.color.text_light else R.color.text_dark
                )
            )
            setPadding(dp(8), dp(10), dp(8), dp(10))
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    private fun selectRow(row: TableRow) {
        selectedRow?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_light))
        row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_soft))
        selectedRow = row
    }

    private fun publishSelection(score: HighScore?) {
        val result = Bundle().apply {
            putBoolean(ScoreSelectionContract.KEY_HAS_LOCATION, score?.hasLocation == true)
            if (score?.hasLocation == true) {
                putDouble(ScoreSelectionContract.KEY_LATITUDE, score.latitude ?: 0.0)
                putDouble(ScoreSelectionContract.KEY_LONGITUDE, score.longitude ?: 0.0)
            }
        }
        parentFragmentManager.setFragmentResult(ScoreSelectionContract.REQUEST_KEY, result)
    }

    private fun addDivider() {
        scoresTable.addView(
            View(requireContext()).apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_soft))
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                )
            }
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val MAX_VISIBLE_SCORES = 10
    }
}
