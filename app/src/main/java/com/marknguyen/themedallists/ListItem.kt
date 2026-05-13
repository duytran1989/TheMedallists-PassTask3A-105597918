package com.marknguyen.themedallists

sealed class ListItem {
    data class Header(val letter: String) : ListItem()
    data class Item(val medallist: Medallist) : ListItem()
}
