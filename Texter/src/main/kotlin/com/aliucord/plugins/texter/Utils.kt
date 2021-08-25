package com.aliucord.plugins.texter

import java.lang.StringBuilder
import java.util.*

fun String.clapify() = ":clap:" + this.replace(" ", ":clap:") + ":clap:"
fun String.reverse() = StringBuilder(this).reverse().toString()

fun String.mock(): String {
    val builder = StringBuilder()
    val random = Random()
    for (character in this.chunked(1)) {
        builder.append(if (random.nextBoolean()) character.lowercase() else character.uppercase())
    }
    return builder.toString()
}