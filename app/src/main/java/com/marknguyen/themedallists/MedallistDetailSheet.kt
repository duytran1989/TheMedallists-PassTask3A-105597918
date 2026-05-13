package com.marknguyen.themedallists

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MedallistDetailSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_COUNTRY = "country"
        private const val ARG_IOC = "ioc_code"
        private const val ARG_TIMES = "times_competed"
        private const val ARG_GOLD = "gold"
        private const val ARG_SILVER = "silver"
        private const val ARG_BRONZE = "bronze"

        fun newInstance(m: Medallist): MedallistDetailSheet {
            val sheet = MedallistDetailSheet()
            sheet.arguments = Bundle().apply {
                putString(ARG_COUNTRY, m.country)
                putString(ARG_IOC, m.iocCode)
                putInt(ARG_TIMES, m.timesCompeted)
                putInt(ARG_GOLD, m.gold)
                putInt(ARG_SILVER, m.silver)
                putInt(ARG_BRONZE, m.bronze)
            }
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_medallist_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val country = args.getString(ARG_COUNTRY, "")
        val ioc = args.getString(ARG_IOC, "")
        val times = args.getInt(ARG_TIMES, 0)
        val gold = args.getInt(ARG_GOLD, 0)
        val silver = args.getInt(ARG_SILVER, 0)
        val bronze = args.getInt(ARG_BRONZE, 0)
        val total = gold + silver + bronze

        val iconColorRes = when {
            gold > 0 -> R.color.medal_gold
            silver > 0 -> R.color.medal_silver
            bronze > 0 -> R.color.medal_bronze
            else -> R.color.text_minor
        }
        val iconColor = ContextCompat.getColor(requireContext(), iconColorRes)

        view.findViewById<ImageView>(R.id.iv_sheet_medal).imageTintList =
            ColorStateList.valueOf(iconColor)

        view.findViewById<TextView>(R.id.tv_sheet_name).text = country

        view.findViewById<TextView>(R.id.tv_sheet_ioc).apply {
            text = ioc
            setTextColor(iconColor)
        }

        view.findViewById<TextView>(R.id.tv_sheet_times).text =
            "Times Competed: $times"

        view.findViewById<TextView>(R.id.tv_sheet_gold).apply {
            text = "Gold:   $gold"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_gold))
        }
        view.findViewById<TextView>(R.id.tv_sheet_silver).apply {
            text = "Silver:  $silver"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_silver))
        }
        view.findViewById<TextView>(R.id.tv_sheet_bronze).apply {
            text = "Bronze: $bronze"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_bronze))
        }
        view.findViewById<TextView>(R.id.tv_sheet_total).text =
            "Total Medals: $total"
    }
}
