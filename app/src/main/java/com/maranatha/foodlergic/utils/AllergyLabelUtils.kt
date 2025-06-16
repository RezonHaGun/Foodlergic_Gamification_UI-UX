package com.maranatha.foodlergic.utils

object AllergyLabelUtils {
    fun getEnglishLabel(label: String): String {
        return when (label.lowercase()) {
            "ikan" -> "Fish"
            "udang" -> "Shrimp"
            "kepiting" -> "Crab"
            "kerang" -> "Shell"
            else -> label
        }
    }
}