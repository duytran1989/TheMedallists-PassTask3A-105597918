package com.marknguyen.themedallists

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LastViewedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_viewed)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("medallists_prefs", MODE_PRIVATE)
        val country = prefs.getString("last_country", null)

        if (country == null) {
            findViewById<TextView>(R.id.tv_no_data).visibility = View.VISIBLE
            return
        }

        val iocCode = prefs.getString("last_ioc_code", "")!!
        val timesCompeted = prefs.getInt("last_times_competed", 0)
        val gold = prefs.getInt("last_gold", 0)
        val silver = prefs.getInt("last_silver", 0)
        val bronze = prefs.getInt("last_bronze", 0)
        val total = gold + silver + bronze

        val iconColorRes = when {
            gold > 0 -> R.color.medal_gold
            silver > 0 -> R.color.medal_silver
            bronze > 0 -> R.color.medal_bronze
            else -> R.color.text_minor
        }
        val iconColor = ContextCompat.getColor(this, iconColorRes)

        findViewById<ImageView>(R.id.iv_medal_last).apply {
            visibility = View.VISIBLE
            imageTintList = ColorStateList.valueOf(iconColor)
        }

        findViewById<TextView>(R.id.tv_last_name).apply {
            visibility = View.VISIBLE
            text = country
        }

        findViewById<TextView>(R.id.tv_last_detail).apply {
            visibility = View.VISIBLE
            text = "$iocCode  •  $timesCompeted appearances"
        }

        findViewById<TextView>(R.id.tv_last_event).apply {
            visibility = View.VISIBLE
            text = "Gold: $gold   Silver: $silver   Bronze: $bronze"
        }

        findViewById<TextView>(R.id.tv_last_medal).apply {
            visibility = View.VISIBLE
            text = "Total Medals: $total"
            setTextColor(iconColor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
