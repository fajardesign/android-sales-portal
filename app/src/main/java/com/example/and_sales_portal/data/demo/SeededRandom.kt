package com.example.and_sales_portal.data.demo

import java.util.Random

/**
 * Random for sample data with a mixed seed: `java.util.Random` gives correlated first values for nearby seeds
 * (e.g. one per day), so the seed goes through the SplitMix64 finalizer first.
 */
fun seededRandom(x: Long): Random {
    var z = x + -0x61c8864680b583ebL
    z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
    z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
    return Random(z xor (z ushr 31))
}
