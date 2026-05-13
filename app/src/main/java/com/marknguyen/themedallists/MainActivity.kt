package com.marknguyen.themedallists

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private enum class SortOrder { ALPHA_ASC, ALPHA_DESC, GOLD_DESC }

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var adapter: MedallistAdapter
    private lateinit var allMedallists: List<Medallist>
    private var sortOrder = SortOrder.ALPHA_ASC
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("medallists_prefs", MODE_PRIVATE)
        allMedallists = loadMedallists()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedallistAdapter(buildListItems()) { medallist ->
            saveLastViewed(medallist)
            MedallistDetailSheet.newInstance(medallist)
                .show(supportFragmentManager, "medallist_detail")
        }
        recyclerView.adapter = adapter

        findViewById<TextInputEditText>(R.id.et_search).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    searchQuery = s?.toString()?.trim() ?: ""
                    adapter.updateItems(buildListItems())
                }
            }
        )
    }

    // Filter first, then sort and add section headers
    private fun buildListItems(): List<ListItem> {
        val source = if (searchQuery.isEmpty()) allMedallists
        else allMedallists.filter { it.country.contains(searchQuery, ignoreCase = true) }

        return when (sortOrder) {
            SortOrder.ALPHA_ASC  -> buildAlphaList(source, ascending = true)
            SortOrder.ALPHA_DESC -> buildAlphaList(source, ascending = false)
            SortOrder.GOLD_DESC  -> buildGoldList(source)
        }
    }

    private fun buildAlphaList(source: List<Medallist>, ascending: Boolean): List<ListItem> {
        val sorted = if (ascending)
            source.sortedBy { it.country.uppercase() }
        else
            source.sortedByDescending { it.country.uppercase() }

        val result = mutableListOf<ListItem>()
        var currentLetter = ""
        for (m in sorted) {
            val letter = m.country.first().uppercaseChar().toString()
            if (letter != currentLetter) {
                currentLetter = letter
                result.add(ListItem.Header(letter))
            }
            result.add(ListItem.Item(m))
        }
        return result
    }

    private fun buildGoldList(source: List<Medallist>): List<ListItem> {
        val sorted = source.sortedByDescending { it.gold }
        val result = mutableListOf<ListItem>()
        var currentTier = ""
        for (m in sorted) {
            val tier = goldTier(m.gold)
            if (tier != currentTier) {
                currentTier = tier
                result.add(ListItem.Header(tier))
            }
            result.add(ListItem.Item(m))
        }
        return result
    }

    private fun goldTier(gold: Int): String = when {
        gold >= 200 -> "200+ Gold Medals"
        gold >= 100 -> "100–199 Gold Medals"
        gold >= 50  -> "50–99 Gold Medals"
        gold >= 20  -> "20–49 Gold Medals"
        gold >= 1   -> "1–19 Gold Medals"
        else        -> "No Gold Medals"
    }

    private fun saveLastViewed(m: Medallist) {
        sharedPrefs.edit()
            .putString("last_country", m.country)
            .putString("last_ioc_code", m.iocCode)
            .putInt("last_times_competed", m.timesCompeted)
            .putInt("last_gold", m.gold)
            .putInt("last_silver", m.silver)
            .putInt("last_bronze", m.bronze)
            .apply()
    }

    private fun loadMedallists(): List<Medallist> {
        val medallists = mutableListOf<Medallist>()
        try {
            assets.open("medallists.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val parts = trimmed.split(",")
                    if (parts.size >= 6) {
                        medallists.add(
                            Medallist(
                                country = parts[0].trim(),
                                iocCode = parts[1].trim(),
                                timesCompeted = parts[2].trim().toIntOrNull() ?: 0,
                                gold = parts[3].trim().toIntOrNull() ?: 0,
                                silver = parts[4].trim().toIntOrNull() ?: 0,
                                bronze = parts[5].trim().toIntOrNull() ?: 0
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return medallists
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_sort)?.title = when (sortOrder) {
            SortOrder.ALPHA_ASC  -> getString(R.string.menu_sort_za)
            SortOrder.ALPHA_DESC -> getString(R.string.menu_sort_az)
            SortOrder.GOLD_DESC  -> getString(R.string.menu_sort_az)
        }
        menu.findItem(R.id.menu_sort_gold)?.title =
            if (sortOrder == SortOrder.GOLD_DESC) getString(R.string.menu_sort_name)
            else getString(R.string.menu_sort_gold)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sort -> {
                sortOrder = when (sortOrder) {
                    SortOrder.ALPHA_ASC  -> SortOrder.ALPHA_DESC
                    SortOrder.ALPHA_DESC -> SortOrder.ALPHA_ASC
                    SortOrder.GOLD_DESC  -> SortOrder.ALPHA_ASC
                }
                adapter.updateItems(buildListItems())
                invalidateOptionsMenu()
                true
            }
            R.id.menu_sort_gold -> {
                sortOrder = if (sortOrder == SortOrder.GOLD_DESC) SortOrder.ALPHA_ASC
                            else SortOrder.GOLD_DESC
                adapter.updateItems(buildListItems())
                invalidateOptionsMenu()
                true
            }
            R.id.menu_last_viewed -> {
                startActivity(Intent(this, LastViewedActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
