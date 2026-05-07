package com.example.healthapp

import java.time.Instant

object InsightsAnalyzer {

    fun generateInsights(todaySteps: Long, todayCalories: Double, weekSteps: List<Pair<Instant, Long>>): List<String> {
        val insights = mutableListOf<String>()

        // Insight for today
        if (todaySteps > 10000) {
            insights.add("Incredible job today! You've crushed your 10,000 steps goal. Keep up the momentum!")
        } else if (todaySteps > 5000) {
            insights.add("You're halfway there! Great work staying active. A quick walk could help you reach your daily goal.")
        } else {
            insights.add("Every step counts! Consider taking a short break to walk and stretch your legs.")
        }

        if (todayCalories > 500) {
            insights.add("You're burning a lot of energy today! Make sure to stay hydrated.")
        }

        // Insight for the week
        if (weekSteps.size >= 7) {
            val totalWeekSteps = weekSteps.sumOf { it.second }
            val avgWeekSteps = totalWeekSteps / 7

            if (avgWeekSteps > 8000) {
                insights.add("Consistent effort! You are averaging over 8,000 steps this week. Your physical health is thriving.")
            } else if (avgWeekSteps > 4000) {
                insights.add("Good consistency this week. Small increments in your daily steps can make a big difference over time.")
            }

            val latest = weekSteps.last().second
            val previous = weekSteps[weekSteps.size - 2].second
            if (latest > previous && previous > 0) {
                val percentIncrease = ((latest - previous).toDouble() / previous * 100).toInt()
                if (percentIncrease > 10) {
                    insights.add("Awesome progress! You were $percentIncrease% more active today compared to yesterday.")
                }
            }
        }

        if (insights.isEmpty()) {
            insights.add("Keep moving forward! Tracking your activity is the first step towards a healthier you.")
        }

        return insights
    }
}
