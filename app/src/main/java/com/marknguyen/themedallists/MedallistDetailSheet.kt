package com.marknguyen.themedallists

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.progressindicator.LinearProgressIndicator

class MedallistDetailSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_COUNTRY = "country"
        private const val ARG_IOC = "ioc_code"
        private const val ARG_TIMES = "times_competed"
        private const val ARG_GOLD = "gold"
        private const val ARG_SILVER = "silver"
        private const val ARG_BRONZE = "bronze"
        private const val ARG_RANK = "rank"

        fun newInstance(m: Medallist, rank: Int): MedallistDetailSheet {
            val sheet = MedallistDetailSheet()
            sheet.arguments = Bundle().apply {
                putString(ARG_COUNTRY, m.country)
                putString(ARG_IOC, m.iocCode)
                putInt(ARG_TIMES, m.timesCompeted)
                putInt(ARG_GOLD, m.gold)
                putInt(ARG_SILVER, m.silver)
                putInt(ARG_BRONZE, m.bronze)
                putInt(ARG_RANK, rank)
            }
            return sheet
        }
    }

    private var targetGoldPct = 0
    private var targetSilverPct = 0
    private var targetBronzePct = 0

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
        val rank = args.getInt(ARG_RANK, 0)
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

        view.findViewById<TextView>(R.id.tv_sheet_rank).apply {
            if (rank > 0) {
                text = "Ranked #$rank globally by total medals"
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        view.findViewById<TextView>(R.id.tv_sheet_times).text = "Times Competed: $times"

        val efficiency = if (times > 0) total.toFloat() / times else 0f
        view.findViewById<TextView>(R.id.tv_sheet_efficiency).text =
            "Medals per appearance: ${"%.1f".format(efficiency)}"

        targetGoldPct = if (total > 0) gold * 100 / total else 0
        targetSilverPct = if (total > 0) silver * 100 / total else 0
        targetBronzePct = if (total > 0) 100 - targetGoldPct - targetSilverPct else 0

        view.findViewById<TextView>(R.id.tv_sheet_gold).apply {
            text = "Gold:   $gold"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_gold))
        }
        view.findViewById<LinearProgressIndicator>(R.id.pb_gold).setProgressCompat(0, false)
        view.findViewById<TextView>(R.id.tv_sheet_gold_pct).text = "$targetGoldPct%"

        view.findViewById<TextView>(R.id.tv_sheet_silver).apply {
            text = "Silver:  $silver"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_silver))
        }
        view.findViewById<LinearProgressIndicator>(R.id.pb_silver).setProgressCompat(0, false)
        view.findViewById<TextView>(R.id.tv_sheet_silver_pct).text = "$targetSilverPct%"

        view.findViewById<TextView>(R.id.tv_sheet_bronze).apply {
            text = "Bronze: $bronze"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.medal_bronze))
        }
        view.findViewById<LinearProgressIndicator>(R.id.pb_bronze).setProgressCompat(0, false)
        view.findViewById<TextView>(R.id.tv_sheet_bronze_pct).text = "$targetBronzePct%"

        view.findViewById<TextView>(R.id.tv_sheet_total).text = "Total Medals: $total"
    }

    override fun onStart() {
        super.onStart()
        val bsDialog = dialog as? BottomSheetDialog ?: return
        val behavior = bsDialog.behavior
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED ||
            behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            view?.post { if (isAdded) animateProgressBars() }
        } else {
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED ||
                        newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        animateProgressBars()
                        behavior.removeBottomSheetCallback(this)
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    private fun animateProgressBars() {
        val v = view ?: return
        v.findViewById<LinearProgressIndicator>(R.id.pb_gold).setProgressCompat(targetGoldPct, true)
        v.findViewById<LinearProgressIndicator>(R.id.pb_silver).setProgressCompat(targetSilverPct, true)
        v.findViewById<LinearProgressIndicator>(R.id.pb_bronze).setProgressCompat(targetBronzePct, true)
    }
}
