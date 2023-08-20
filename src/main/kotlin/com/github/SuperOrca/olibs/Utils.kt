package com.github.SuperOrca.olibs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

private val serializer = MiniMessage.builder()
    .postProcessor { component -> component.decoration(TextDecoration.ITALIC, false) }
    .build()
private val endings = arrayOf("k", "M", "B", "T", "Q", "QT", "S", "SP", "O")
private val smallCapitalMap = mapOf(
    'a' to 'ᴀ',
    'b' to 'ʙ',
    'c' to 'ᴄ',
    'd' to 'ᴅ',
    'e' to 'ᴇ',
    'f' to 'ғ',
    'g' to 'ɢ',
    'h' to 'ʜ',
    'i' to 'ɪ',
    'j' to 'ᴊ',
    'k' to 'ᴋ',
    'l' to 'ʟ',
    'm' to 'ᴍ',
    'n' to 'ɴ',
    'o' to 'ᴏ',
    'p' to 'ᴘ',
    'q' to 'ǫ',
    'r' to 'ʀ',
    's' to 's',
    't' to 'ᴛ',
    'u' to 'ᴜ',
    'v' to 'ᴠ',
    'w' to 'ᴡ',
    'x' to 'x',
    'y' to 'ʏ',
    'z' to 'ᴢ'
)

fun component(text: String): Component {
    return serializer.deserialize(text)
}

fun String.small(): String {
    val output = StringBuilder()
    for (char in this) {
        if (char in 'a'..'z') {
            output.append(smallCapitalMap[char])
        } else {
            output.append(char)
        }
    }
    return output.toString()
}

fun Long.compact(): String {
    if (this < 1000L) return "" + this
    val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()
    return "%.1f%s".format(this / 1000.0.pow(exp.toDouble()), endings[exp - 1])
}

fun Double.compact(): String {
    if (this < 1000.0) return "%.0f".format(this)
    val exp = (ln(this) / ln(1000.0)).toInt()
    return "%.1f%s".format(this / 1000.0.pow(exp.toDouble()), endings[exp - 1])
}

fun Long.format(): String {
    return DecimalFormat("#,###").format(this)
}

fun Double.format(): String {
    return DecimalFormat("#,###").format(this)
}

fun Long.formatDuration(): String {
    return DurationFormatUtils.formatDurationWords(this, true, true)
}

fun String.title(): String =
    lowercase().split(" ")
        .joinToString(" ") { it -> it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

fun Double.round(places: Int): Double {
    return BigDecimal(this).setScale(places, RoundingMode.HALF_EVEN).toDouble()
}

fun String.parseId(): String {
    return this.lowercase().replace("_", " ")
}

fun String.scramble(): String {
    return toCharArray().toMutableList().shuffled().joinToString("")
}

fun ItemStack.amount(amount: Int): ItemStack {
    val clone = this.clone()
    clone.amount = amount
    return clone
}

fun Player.playSound(sound: Sound, volume: Float, pitch: Float) {
    playSound(location, sound, volume, pitch)
}

fun Int.roman(): String {
    val romanMap = mapOf(
        1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
        100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
        10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I"
    )

    var remaining = this
    val result = StringBuilder()

    for ((value, symbol) in romanMap) {
        while (remaining >= value) {
            remaining -= value
            result.append(symbol)
        }
    }

    return result.toString()
}

fun Long.compactDuration(): String {
    val seconds = (this / 1000L) % 60L
    val minutes = (this / (1000L * 60L)) % 60L
    val hours = (this / (1000L * 60L * 60L)) % 24L
    val days = (this / (1000L * 60L * 60L * 24L)) % 365L

    val builder = StringBuilder()

    if (days > 0) {
        builder.append("${days}d")
    }
    if (hours > 0) {
        builder.append("${hours}h")
    }
    if (minutes > 0) {
        builder.append("${minutes}m")
    }
    if (seconds > 0) {
        builder.append("${seconds}s")
    }

    return builder.toString()
}