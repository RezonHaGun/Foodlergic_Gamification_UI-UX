package com.maranatha.foodlergic.presentation.leaderboard

data class LeaderboardItem(
    var rank: Int = 0,
    val userId: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val level: String = ""
)


