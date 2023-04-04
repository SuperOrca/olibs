package io.github.superorca.olibs

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

fun component(text: String): Component {
    return serializer.deserialize(text)
}

fun smallCaps(string: String): String {
    val length = string.length
    val smallCaps = Formatter(StringBuilder(length))
    for (i in 0 until length) {
        val c = string[i]
        if (c in 'A'..'Z' && c != 'X') {
            smallCaps.format("%c", Character.codePointOf("LATIN LETTER SMALL CAPITAL $c"))
        } else {
            smallCaps.format("%c", c)
        }
    }
    return smallCaps.toString()
}

fun compact(number: Long): String {
    if (number < 1000L) return "" + number
    val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
    return "%.1f%s".formatted(number / 1000.0.pow(exp.toDouble()), endings[exp - 1])
}

fun compact(number: Double): String {
    if (number < 1000.0) return "%.0f".formatted(number)
    val exp = (ln(number) / ln(1000.0)).toInt()
    return "%.1f%s".formatted(number / 1000.0.pow(exp.toDouble()), endings[exp - 1])
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