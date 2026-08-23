package com.otakeessen.bodysquirrel.data

object PortionCalculator {
    fun scaleCalories(totalKcal: Double, totalWeightG: Double, eatenWeightG: Double): Double {
        if (totalWeightG <= 0.0) return totalKcal
        return totalKcal * (eatenWeightG / totalWeightG)
    }
}
