package com.marknguyen.themedallists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StatsSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(medallists: List<Medallist>): StatsSheet {
            val sheet = StatsSheet()
            val topGold = medallists.maxByOrNull { it.gold }
            val topSilver = medallists.maxByOrNull { it.silver }
            val topBronze = medallists.maxByOrNull { it.bronze }
            val topTotal = medallists.maxByOrNull { it.totalMedals }
            val avgEfficiency = if (medallists.isNotEmpty())
                medallists.filter { it.timesCompeted > 0 }
                    .map { it.totalMedals.toDouble() / it.timesCompeted }
                    .average()
            else 0.0

            sheet.arguments = Bundle().apply {
                putInt("count", medallists.size)
                putInt("total_gold", medallists.sumOf { it.gold })
                putInt("total_silver", medallists.sumOf { it.silver })
                putInt("total_bronze", medallists.sumOf { it.bronze })
                putInt("grand_total", medallists.sumOf { it.totalMedals })
                putString("top_gold", "${topGold?.country ?: "N/A"} (${topGold?.gold ?: 0})")
                putString("top_silver", "${topSilver?.country ?: "N/A"} (${topSilver?.silver ?: 0})")
                putString("top_bronze", "${topBronze?.country ?: "N/A"} (${topBronze?.bronze ?: 0})")
                putString("top_total", "${topTotal?.country ?: "N/A"} (${topTotal?.totalMedals ?: 0})")
                putString("avg_efficiency", "%.2f".format(avgEfficiency))
            }
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_stats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val a = requireArguments()

        view.findViewById<TextView>(R.id.tv_stats_count).text =
            "Countries in dataset: ${a.getInt("count")}"
        view.findViewById<TextView>(R.id.tv_stats_grand_total).text =
            "Total medals awarded: ${a.getInt("grand_total")}"
        view.findViewById<TextView>(R.id.tv_stats_gold_total).text =
            "Total gold medals: ${a.getInt("total_gold")}"
        view.findViewById<TextView>(R.id.tv_stats_silver_total).text =
            "Total silver medals: ${a.getInt("total_silver")}"
        view.findViewById<TextView>(R.id.tv_stats_bronze_total).text =
            "Total bronze medals: ${a.getInt("total_bronze")}"
        view.findViewById<TextView>(R.id.tv_stats_top_gold).text =
            "Most gold: ${a.getString("top_gold")}"
        view.findViewById<TextView>(R.id.tv_stats_top_silver).text =
            "Most silver: ${a.getString("top_silver")}"
        view.findViewById<TextView>(R.id.tv_stats_top_bronze).text =
            "Most bronze: ${a.getString("top_bronze")}"
        view.findViewById<TextView>(R.id.tv_stats_top_total).text =
            "Most medals overall: ${a.getString("top_total")}"
        view.findViewById<TextView>(R.id.tv_stats_efficiency).text =
            "Avg medals per appearance: ${a.getString("avg_efficiency")}"
    }
}
