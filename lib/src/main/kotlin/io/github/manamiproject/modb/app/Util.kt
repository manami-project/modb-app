package io.github.manamiproject.modb.app

import kotlin.math.pow

/**
 * Calculates the difference of two numbers `>= 0`.
 * It doesn't matter which of the parameters is the higher or lower number.
 * @since 1.0.0
 * @param number1 Any number `>= 0`
 * @param number2 Any number `>= 0`
 * @return Integer showing the difference between the two parameters.
 */
fun difference(number1: Int, number2: Int): Int {
    require(number1 >= 0 && number2 >= 0) { "Passed numbers must be positive or 0" }

    val higherToLower = if (number1 > number2) number1 to number2 else number2 to number1

    return higherToLower.first - higherToLower.second
}

/**
 * This function allows to compare two numbers and make an assumption on how likely it is that they mean the same thing
 * although being different.
 * **Background:** For anime there are often differences in episodes and years. Although they should be the
 * same, because they describe the same anime, those can differ between meta data providers.
 * An example for episodes is if the number of episodes includes a recap episode on one meta data provider whereas it's
 * not part of the entry on the other meta data provider.
 * For years this can especially happen for the winter season overlapping in years or if they use release dates from
 * different countries.
 * @since 1.0.0
 * @param number1 Any number `>=0`
 * @param number2 Any number `>=0`
 * @param factor The higher this factor is, the lower the probability of two numbers which have a difference mean the same thing. The factor must be `>1`
 * @return A value in a percentage representation (0.0 to 1.0) that indicates how likely it is that [number1] and [number2] are equal.
 */
fun weightedProbabilityOfTwoNumbersBeingEqual(number1: Int, number2: Int, factor: Int): Double {
    require(factor > 1) { "Factor must be > 1" }

    if (number1 == number2) {
        return 1.0
    }

    val difference = difference(number1, number2)
    val probability = 1.0 - (factor.toDouble().pow(difference) / 100.0)

    return probability.takeIf { it > 0.0 } ?: 0.0
}
