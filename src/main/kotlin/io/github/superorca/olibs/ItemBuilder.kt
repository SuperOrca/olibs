package io.github.superorca.olibs

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.function.Consumer

class ItemBuilder(private val item: ItemStack) {
    constructor(material: Material?) : this(ItemStack(material!!))

    private fun edit(function: Consumer<ItemStack>): ItemBuilder {
        function.accept(item)
        return this
    }

    private fun meta(metaConsumer: Consumer<ItemMeta>): ItemBuilder {
        return edit { item: ItemStack ->
            val meta = item.itemMeta
            metaConsumer.accept(meta)
            item.itemMeta = meta
        }
    }

    private fun <T : ItemMeta?> meta(metaClass: Class<T>, metaConsumer: Consumer<T>): ItemBuilder {
        return meta { meta: ItemMeta? ->
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta))
            }
        }
    }

    fun type(material: Material?): ItemBuilder {
        return edit { item: ItemStack -> item.type = material!! }
    }

    fun amount(amount: Int): ItemBuilder {
        return edit { item: ItemStack -> item.amount = amount }
    }

    fun unbreakable(value: Boolean): ItemBuilder {
        return meta { meta: ItemMeta -> meta.isUnbreakable = value }
    }

    @JvmOverloads
    fun enchant(enchantment: Enchantment?, level: Int = 1): ItemBuilder {
        return meta { meta: ItemMeta -> meta.addEnchant(enchantment!!, level, true) }
    }

    fun removeEnchant(enchantment: Enchantment?): ItemBuilder {
        return meta { meta: ItemMeta -> meta.removeEnchant(enchantment!!) }
    }

    fun removeEnchants(): ItemBuilder {
        return meta { meta: ItemMeta ->
            meta.enchants.keys.forEach(Consumer { ench: Enchantment? ->
                meta.removeEnchant(
                    ench!!
                )
            })
        }
    }

    fun name(name: Component?): ItemBuilder {
        return meta { meta: ItemMeta -> meta.displayName(name) }
    }

    fun lore(lore: Component): ItemBuilder {
        return lore(listOf(lore))
    }

    fun lore(vararg lore: Component?): ItemBuilder {
        return lore(listOf(*lore))
    }

    fun lore(lore: List<Component?>?): ItemBuilder {
        return meta { meta: ItemMeta -> meta.lore(lore) }
    }

    fun flags(vararg flags: ItemFlag?): ItemBuilder {
        return meta { meta: ItemMeta -> flags.forEach { flag -> meta.addItemFlags(flag!!) } }
    }

    fun flags(): ItemBuilder {
        return flags(*ItemFlag.values())
    }

    private fun removeFlags(vararg flags: ItemFlag?): ItemBuilder {
        return meta { meta: ItemMeta -> flags.forEach { flag -> meta.removeItemFlags(flag!!) } }
    }

    fun removeFlags(): ItemBuilder {
        return removeFlags(*ItemFlag.values())
    }

    fun skullOwner(player: OfflinePlayer?): ItemBuilder {
        return meta(SkullMeta::class.java) { meta: SkullMeta -> meta.owningPlayer = player }
    }

    fun build(): ItemStack {
        return item
    }

    companion object {
        fun copyOf(item: ItemStack): ItemBuilder {
            return ItemBuilder(item.clone())
        }
    }
}