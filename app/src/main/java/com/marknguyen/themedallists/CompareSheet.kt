package com.marknguyen.themedallists

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CompareSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(a: Medallist, rankA: Int, b: Medallist, rankB: Int): CompareSheet {
            val sheet = CompareSheet()
            sheet.arguments = Bundle().apply {
                putString("a_country", a.country); putString("a_ioc", a.iocCode)
                putInt("a_times", a.timesCompeted); putInt("a_gold", a.gold)
                putInt("a_silver", a.silver); putInt("a_bronze", a.bronze)
                putInt("a_rank", rankA)
                putString("b_country", b.country); putString("b_ioc", b.iocCode)
                putInt("b_times", b.timesCompeted); putInt("b_gold", b.gold)
                putInt("b_silver", b.silver); putInt("b_bronze", b.bronze)
                putInt("b_rank", rankB)
            }
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_compare, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val a = requireArguments()

        val aGold = a.getInt("a_gold"); val bGold = a.getInt("b_gold")
        val aSilver = a.getInt("a_silver"); val bSilver = a.getInt("b_silver")
        val aBronze = a.getInt("a_bronze"); val bBronze = a.getInt("b_bronze")
        val aTimes = a.getInt("a_times"); val bTimes = a.getInt("b_times")
        val aTotal = aGold + aSilver + aBronze
        val bTotal = bGold + bSilver + bBronze
        val aEff = if (aTimes > 0) aTotal.toDouble() / aTimes else 0.0
        val bEff = if (bTimes > 0) bTotal.toDouble() / bTimes else 0.0
        val aRank = a.getInt("a_rank"); val bRank = a.getInt("b_rank")

        view.findViewById<TextView>(R.id.tv_cmp_a_name).text = a.getString("a_country")
        view.findViewById<TextView>(R.id.tv_cmp_b_name).text = a.getString("b_country")
        view.findViewById<TextView>(R.id.tv_cmp_a_ioc).text = a.getString("a_ioc")
        view.findViewById<TextView>(R.id.tv_cmp_b_ioc).text = a.getString("b_ioc")

        val vAGold = view.findViewById<TextView>(R.id.tv_cmp_a_gold)
        val vBGold = view.findViewById<TextView>(R.id.tv_cmp_b_gold)
        vAGold.text = "$aGold"; vBGold.text = "$bGold"
        applyWinner(vAGold, aGold.toDouble(), vBGold, bGold.toDouble())

        val vASilver = view.findViewById<TextView>(R.id.tv_cmp_a_silver)
        val vBSilver = view.findViewById<TextView>(R.id.tv_cmp_b_silver)
        vASilver.text = "$aSilver"; vBSilver.text = "$bSilver"
        applyWinner(vASilver, aSilver.toDouble(), vBSilver, bSilver.toDouble())

        val vABronze = view.findViewById<TextView>(R.id.tv_cmp_a_bronze)
        val vBBronze = view.findViewById<TextView>(R.id.tv_cmp_b_bronze)
        vABronze.text = "$aBronze"; vBBronze.text = "$bBronze"
        applyWinner(vABronze, aBronze.toDouble(), vBBronze, bBronze.toDouble())

        val vATotal = view.findViewById<TextView>(R.id.tv_cmp_a_total)
        val vBTotal = view.findViewById<TextView>(R.id.tv_cmp_b_total)
        vATotal.text = "$aTotal"; vBTotal.text = "$bTotal"
        applyWinner(vATotal, aTotal.toDouble(), vBTotal, bTotal.toDouble())

        val vAEff = view.findViewById<TextView>(R.id.tv_cmp_a_eff)
        val vBEff = view.findViewById<TextView>(R.id.tv_cmp_b_eff)
        vAEff.text = "%.2f".format(aEff); vBEff.text = "%.2f".format(bEff)
        applyWinner(vAEff, aEff, vBEff, bEff)

        val vARank = view.findViewById<TextView>(R.id.tv_cmp_a_rank)
        val vBRank = view.findViewById<TextView>(R.id.tv_cmp_b_rank)
        vARank.text = if (aRank > 0) "#$aRank" else "—"
        vBRank.text = if (bRank > 0) "#$bRank" else "—"
        // Lower rank number = better
        applyWinner(vARank, if (aRank > 0) -aRank.toDouble() else Double.MIN_VALUE,
                    vBRank, if (bRank > 0) -bRank.toDouble() else Double.MIN_VALUE)
    }

    private fun applyWinner(viewA: TextView, valueA: Double, viewB: TextView, valueB: Double) {
        val ctx = requireContext()
        val winColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
        val tieColor = ContextCompat.getColor(ctx, R.color.text_primary)
        val loseColor = ContextCompat.getColor(ctx, R.color.text_minor)

        when {
            valueA > valueB -> {
                viewA.setTypeface(null, Typeface.BOLD); viewA.setTextColor(winColor)
                viewB.setTypeface(null, Typeface.NORMAL); viewB.setTextColor(loseColor)
            }
            valueB > valueA -> {
                viewB.setTypeface(null, Typeface.BOLD); viewB.setTextColor(winColor)
                viewA.setTypeface(null, Typeface.NORMAL); viewA.setTextColor(loseColor)
            }
            else -> {
                viewA.setTypeface(null, Typeface.BOLD); viewA.setTextColor(tieColor)
                viewB.setTypeface(null, Typeface.BOLD); viewB.setTextColor(tieColor)
            }
        }
    }
}
