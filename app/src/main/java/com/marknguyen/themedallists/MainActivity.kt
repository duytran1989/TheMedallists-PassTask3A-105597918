package com.marknguyen.themedallists

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private enum class SortOrder { ALPHA_ASC, ALPHA_DESC, GOLD_DESC, TOTAL_DESC, EFFICIENCY_DESC }
    private enum class MedalFilter { ALL, GOLD, SILVER, BRONZE }

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var adapter: MedallistAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var allMedallists: List<Medallist>

    private var sortOrder = SortOrder.ALPHA_ASC
    private var medalFilter = MedalFilter.ALL
    private var showFavouritesOnly = false
    private var searchQuery = ""
    private val favourites = mutableSetOf<String>()

    private var actionMode: ActionMode? = null
    private var actionModeTarget: Medallist? = null

    // ── ActionMode callback ──────────────────────────────────────────────────

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.context_menu, menu)
            // Tint existing bookmark icon to white for the action bar
            menu.findItem(R.id.context_favourite)?.icon?.mutate()?.let { icon ->
                DrawableCompat.setTint(icon, Color.WHITE)
                menu.findItem(R.id.context_favourite)?.icon = icon
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            val target = actionModeTarget ?: return false
            mode.title = target.country
            mode.subtitle = "G:${target.gold}  S:${target.silver}  B:${target.bronze}"
            val isFav = favourites.contains(target.country)
            menu.findItem(R.id.context_favourite)?.title =
                if (isFav) "Remove Favourite" else "Add Favourite"
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val target = actionModeTarget ?: return false
            return when (item.itemId) {
                R.id.context_share -> {
                    shareCountry(target)
                    mode.finish()
                    true
                }
                R.id.context_favourite -> {
                    val wasFav = favourites.contains(target.country)
                    if (wasFav) favourites.remove(target.country) else favourites.add(target.country)
                    sharedPrefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                    adapter.update(buildListItems(), favourites.toSet())
                    Toast.makeText(
                        this@MainActivity,
                        if (wasFav) "${target.country} removed from favourites"
                        else "★ ${target.country} added to favourites",
                        Toast.LENGTH_SHORT
                    ).show()
                    mode.invalidate()
                    true
                }
                R.id.context_compare -> {
                    compareWithLastViewed(target)
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionModeTarget = null
            adapter.setSelected(null)
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("medallists_prefs", MODE_PRIVATE)
        sharedPrefs.getStringSet("favourites", emptySet())?.let { favourites.addAll(it) }

        allMedallists = loadMedallists()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedallistAdapter(
            items = buildListItems(),
            favourites = favourites.toSet(),
            onItemClick = { medallist ->
                saveLastViewed(medallist)
                MedallistDetailSheet.newInstance(medallist, getRank(medallist))
                    .show(supportFragmentManager, "medallist_detail")
            },
            onItemLongClick = { medallist ->
                actionMode?.finish()
                actionModeTarget = medallist
                adapter.setSelected(medallist.country)
                actionMode = startSupportActionMode(actionModeCallback)
            },
            onFavouriteClick = { medallist ->
                val wasInFavourites = favourites.contains(medallist.country)
                if (wasInFavourites) favourites.remove(medallist.country)
                else favourites.add(medallist.country)
                sharedPrefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                adapter.update(buildListItems(), favourites.toSet())

                if (wasInFavourites) {
                    Snackbar.make(recyclerView, "${medallist.country} removed from favourites",
                        Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            favourites.add(medallist.country)
                            sharedPrefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                            adapter.update(buildListItems(), favourites.toSet())
                        }.show()
                } else {
                    Toast.makeText(this, "★ ${medallist.country} added to favourites",
                        Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter

        // Filter chips
        findViewById<ChipGroup>(R.id.chip_group).setOnCheckedStateChangeListener { _, ids ->
            medalFilter = when (ids.firstOrNull()) {
                R.id.chip_gold   -> MedalFilter.GOLD
                R.id.chip_silver -> MedalFilter.SILVER
                R.id.chip_bronze -> MedalFilter.BRONZE
                else             -> MedalFilter.ALL
            }
            adapter.updateItems(buildListItems())
        }

        // Search bar
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

    // ── Share ────────────────────────────────────────────────────────────────

    private fun shareCountry(medallist: Medallist) {
        val rank = getRank(medallist)
        val efficiency = if (medallist.timesCompeted > 0)
            "%.1f".format(medallist.totalMedals.toFloat() / medallist.timesCompeted)
        else "N/A"
        val text = "${medallist.country} (${medallist.iocCode})\n" +
            "Olympic Medals — Ranked #$rank globally\n\n" +
            "Gold:   ${medallist.gold}\n" +
            "Silver: ${medallist.silver}\n" +
            "Bronze: ${medallist.bronze}\n" +
            "Total:  ${medallist.totalMedals}\n\n" +
            "Medals per appearance: $efficiency"
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, "${medallist.country} Olympic Medals")
                },
                "Share via"
            )
        )
    }

    // ── Compare ──────────────────────────────────────────────────────────────

    private fun compareWithLastViewed(medallist: Medallist) {
        val lastCountry = sharedPrefs.getString("last_country", null)
        if (lastCountry == null) {
            Snackbar.make(recyclerView, "Tap a row first to set a comparison target",
                Snackbar.LENGTH_SHORT).show()
            return
        }
        if (lastCountry == medallist.country) {
            Toast.makeText(this, "Open a different country to compare against", Toast.LENGTH_SHORT).show()
            return
        }
        val lastMedallist = Medallist(
            country = lastCountry,
            iocCode = sharedPrefs.getString("last_ioc_code", "") ?: "",
            timesCompeted = sharedPrefs.getInt("last_times_competed", 0),
            gold = sharedPrefs.getInt("last_gold", 0),
            silver = sharedPrefs.getInt("last_silver", 0),
            bronze = sharedPrefs.getInt("last_bronze", 0)
        )
        CompareSheet.newInstance(medallist, getRank(medallist), lastMedallist, getRank(lastMedallist))
            .show(supportFragmentManager, "compare")
    }

    // ── Filter pipeline ─────────────────────────────────────────────────────

    private fun buildListItems(): List<ListItem> {
        val source = allMedallists.filter { m ->
            (searchQuery.isEmpty() || m.country.contains(searchQuery, ignoreCase = true))
                && matchesMedalFilter(m)
                && (!showFavouritesOnly || favourites.contains(m.country))
        }
        return when (sortOrder) {
            SortOrder.ALPHA_ASC       -> buildAlphaList(source, ascending = true)
            SortOrder.ALPHA_DESC      -> buildAlphaList(source, ascending = false)
            SortOrder.GOLD_DESC       -> buildGoldList(source)
            SortOrder.TOTAL_DESC      -> buildTotalList(source)
            SortOrder.EFFICIENCY_DESC -> buildEfficiencyList(source)
        }
    }

    private fun matchesMedalFilter(m: Medallist) = when (medalFilter) {
        MedalFilter.ALL    -> true
        MedalFilter.GOLD   -> m.gold > 0
        MedalFilter.SILVER -> m.silver > 0
        MedalFilter.BRONZE -> m.bronze > 0
    }

    private fun getRank(medallist: Medallist): Int {
        val idx = allMedallists.sortedByDescending { it.totalMedals }
            .indexOfFirst { it.country == medallist.country }
        return if (idx >= 0) idx + 1 else 0
    }

    // ── List builders ────────────────────────────────────────────────────────

    private fun buildAlphaList(source: List<Medallist>, ascending: Boolean): List<ListItem> {
        val sorted = if (ascending) source.sortedBy { it.country.uppercase() }
                     else source.sortedByDescending { it.country.uppercase() }
        val result = mutableListOf<ListItem>()
        var currentLetter = ""
        for (m in sorted) {
            val letter = m.country.first().uppercaseChar().toString()
            if (letter != currentLetter) { currentLetter = letter; result.add(ListItem.Header(letter)) }
            result.add(ListItem.Item(m))
        }
        return result
    }

    private fun buildGoldList(source: List<Medallist>): List<ListItem> {
        val result = mutableListOf<ListItem>()
        var currentTier = ""
        for (m in source.sortedByDescending { it.gold }) {
            val tier = goldTier(m.gold)
            if (tier != currentTier) { currentTier = tier; result.add(ListItem.Header(tier)) }
            result.add(ListItem.Item(m))
        }
        return result
    }

    private fun buildTotalList(source: List<Medallist>): List<ListItem> {
        val result = mutableListOf<ListItem>()
        var currentTier = ""
        for (m in source.sortedByDescending { it.totalMedals }) {
            val tier = totalTier(m.totalMedals)
            if (tier != currentTier) { currentTier = tier; result.add(ListItem.Header(tier)) }
            result.add(ListItem.Item(m))
        }
        return result
    }

    private fun buildEfficiencyList(source: List<Medallist>): List<ListItem> {
        val result = mutableListOf<ListItem>()
        var currentTier = ""
        val sorted = source.sortedByDescending { m ->
            if (m.timesCompeted > 0) m.totalMedals.toDouble() / m.timesCompeted else 0.0
        }
        for (m in sorted) {
            val eff = if (m.timesCompeted > 0) m.totalMedals.toDouble() / m.timesCompeted else 0.0
            val tier = efficiencyTier(eff)
            if (tier != currentTier) { currentTier = tier; result.add(ListItem.Header(tier)) }
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

    private fun totalTier(total: Int): String = when {
        total >= 500 -> "500+ Total Medals"
        total >= 200 -> "200–499 Total Medals"
        total >= 100 -> "100–199 Total Medals"
        total >= 50  -> "50–99 Total Medals"
        total >= 1   -> "1–49 Total Medals"
        else         -> "No Medals"
    }

    private fun efficiencyTier(eff: Double): String = when {
        eff >= 10.0 -> "10+ medals/appearance"
        eff >= 5.0  -> "5.0–9.9 medals/appearance"
        eff >= 2.0  -> "2.0–4.9 medals/appearance"
        eff >= 1.0  -> "1.0–1.9 medals/appearance"
        eff > 0.0   -> "Under 1 medal/appearance"
        else        -> "No medals"
    }

    // ── SharedPreferences ────────────────────────────────────────────────────

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

    // ── CSV loader ───────────────────────────────────────────────────────────

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

    // ── Options menu ─────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_sort_name)?.title = when (sortOrder) {
            SortOrder.ALPHA_ASC  -> getString(R.string.menu_sort_za)
            SortOrder.ALPHA_DESC -> getString(R.string.menu_sort_az)
            else                 -> getString(R.string.menu_sort_az)
        }
        menu.findItem(R.id.menu_sort_gold)?.title =
            if (sortOrder == SortOrder.GOLD_DESC) getString(R.string.menu_sort_gold_active)
            else getString(R.string.menu_sort_gold)
        menu.findItem(R.id.menu_sort_total)?.title =
            if (sortOrder == SortOrder.TOTAL_DESC) getString(R.string.menu_sort_total_active)
            else getString(R.string.menu_sort_total)
        menu.findItem(R.id.menu_sort_efficiency)?.title =
            if (sortOrder == SortOrder.EFFICIENCY_DESC) getString(R.string.menu_sort_efficiency_active)
            else getString(R.string.menu_sort_efficiency)
        menu.findItem(R.id.menu_favourites)?.title =
            if (showFavouritesOnly) getString(R.string.menu_favourites_active)
            else getString(R.string.menu_favourites)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sort_name -> {
                sortOrder = if (sortOrder == SortOrder.ALPHA_ASC) SortOrder.ALPHA_DESC
                            else SortOrder.ALPHA_ASC
                adapter.updateItems(buildListItems()); invalidateOptionsMenu(); true
            }
            R.id.menu_sort_gold -> {
                sortOrder = if (sortOrder == SortOrder.GOLD_DESC) SortOrder.ALPHA_ASC
                            else SortOrder.GOLD_DESC
                adapter.updateItems(buildListItems()); invalidateOptionsMenu(); true
            }
            R.id.menu_sort_total -> {
                sortOrder = if (sortOrder == SortOrder.TOTAL_DESC) SortOrder.ALPHA_ASC
                            else SortOrder.TOTAL_DESC
                adapter.updateItems(buildListItems()); invalidateOptionsMenu(); true
            }
            R.id.menu_sort_efficiency -> {
                sortOrder = if (sortOrder == SortOrder.EFFICIENCY_DESC) SortOrder.ALPHA_ASC
                            else SortOrder.EFFICIENCY_DESC
                adapter.updateItems(buildListItems()); invalidateOptionsMenu(); true
            }
            R.id.menu_favourites -> {
                showFavouritesOnly = !showFavouritesOnly
                adapter.updateItems(buildListItems()); invalidateOptionsMenu(); true
            }
            R.id.menu_clear_favourites -> {
                if (favourites.isEmpty()) {
                    Toast.makeText(this, "No favourites to clear", Toast.LENGTH_SHORT).show()
                } else {
                    val cleared = favourites.toMutableSet()
                    favourites.clear()
                    sharedPrefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                    adapter.update(buildListItems(), favourites.toSet()); invalidateOptionsMenu()
                    Snackbar.make(recyclerView, "${cleared.size} favourite(s) cleared",
                        Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            favourites.addAll(cleared)
                            sharedPrefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                            adapter.update(buildListItems(), favourites.toSet())
                            invalidateOptionsMenu()
                        }.show()
                }
                true
            }
            R.id.menu_statistics -> {
                StatsSheet.newInstance(allMedallists).show(supportFragmentManager, "stats"); true
            }
            R.id.menu_last_viewed -> {
                startActivity(Intent(this, LastViewedActivity::class.java)); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
