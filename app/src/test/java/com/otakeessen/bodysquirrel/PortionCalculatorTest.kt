package com.otakeessen.bodysquirrel

import com.otakeessen.bodysquirrel.data.PortionCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PortionCalculatorTest {

    @Test
    fun halfPortionIsHalfCalories() {
        val kcal = PortionCalculator.scaleCalories(
            totalKcal = 400.0,
            totalWeightG = 200.0,
            eatenWeightG = 100.0
        )
        assertEquals(200.0, kcal, 0.001)
    }

    @Test
    fun fullPortionReturnsFullCalories() {
        val kcal = PortionCalculator.scaleCalories(
            totalKcal = 426.0,
            totalWeightG = 340.0,
            eatenWeightG = 340.0
        )
        assertEquals(426.0, kcal, 0.001)
    }

    @Test
    fun zeroDishWeightReturnsFullCalories() {
        val kcal = PortionCalculator.scaleCalories(
            totalKcal = 250.0,
            totalWeightG = 0.0,
            eatenWeightG = 50.0
        )
        assertEquals(250.0, kcal, 0.001)
    }

    @Test
    fun quarterPortionScalesByWeight() {
        val kcal = PortionCalculator.scaleCalories(
            totalKcal = 480.0,
            totalWeightG = 400.0,
            eatenWeightG = 100.0
        )
        assertEquals(120.0, kcal, 0.001)
    }
}
