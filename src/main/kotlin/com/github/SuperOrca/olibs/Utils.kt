package com.github.SuperOrca.olibs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

private val serializer = MiniMessage.builder()
    .postProcessor { component -> component.decoration(TextDecoration.ITALIC, false) }
    .build()
private val endings = arrayOf("k", "M", "B", "T", "Q", "QT", "S", "SP", "O")
private val smallCapitalMap = mapOf(
    'a' to '\u1d00', 'b' to '\u0299', 'c' to '\u1d04', 'd' to '\u1d05', 'e' to '\u1d07',
    'f' to '\ua730', 'g' to '\u0262', 'h' to '\u029c', 'i' to '\u026a', 'j' to '\u1d0a',
    'k' to '\u1d0b', 'l' to '\u029f', 'm' to '\u1d0d', 'n' to '\u0274', 'o' to '\u1d0f',
    'p' to '\u1d18', 'q' to '\ua731', 'r' to '\u0280', 's' to '\ua731', 't' to '\u1d1b',
    'u' to '\u1d1c', 'v' to '\u1d20', 'w' to '\u1d21', 'x' to '\u1d22', 'y' to '\u028f',
    'z' to '\u1d23'
)

fun component(text: String): Component {
    return serializer.deserialize(text)
}

fun smallCaps(input: String): String {
    val outputBuilder = StringBuilder()
    for (char in input) {
        if (char in 'a'..'z') {
            outputBuilder.append(smallCapitalMap[char])
        } else {
            outputBuilder.append(char)
        }
    }
    return outputBuilder.toString()
}

fun compact(number: Long): String {
    if (number < 1000L) return "" + number
    val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
    return "%.1f%s".format(number / 1000.0.pow(exp.toDouble()), endings[exp - 1])
}

fun compact(number: Double): String {
    if (number < 1000.0) return "%.0f".format(number)
    val exp = (ln(number) / ln(1000.0)).toInt()
    return "%.1f%s".format(number / 1000.0.pow(exp.toDouble()), endings[exp - 1])
}

fun compact(number: Number?): String? {
    return if (number is Long) compact(number) else if (number is Double) compact(number) else null
}

fun format(number: Long): String {
    return DecimalFormat("#,###").format(number)
}

fun format(number: Int): String {
    return DecimalFormat("#,###").format(number.toLong())
}

fun format(number: Double): String {
    return DecimalFormat("#,###").format(number)
}

fun formatDuration(duration: Long): String {
    return DurationFormatUtils.formatDurationWords(duration, true, false)
}

fun componentToString(component: Component): String {
    return LegacyComponentSerializer.legacySection().serialize(component)
}

fun stringToComponent(string: String): Component {
    return if (string.contains("&")) LegacyComponentSerializer.legacyAmpersand()
        .deserialize(string) else return LegacyComponentSerializer.legacySection().deserialize(string)
}

fun String.title(): String =
    lowercase().split(" ")
        .joinToString(" ") { it -> it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }