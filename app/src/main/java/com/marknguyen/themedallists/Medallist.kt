package com.marknguyen.themedallists

data class Medallist(
    val country: String,
    val iocCode: String,
    val timesCompeted: Int,
    val gold: Int,
    val silver: Int,
    val bronze: Int
) {
    val totalMedals: Int get() = gold + silver + bronze
}
