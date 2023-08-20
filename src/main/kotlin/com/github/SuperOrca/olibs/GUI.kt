package com.github.SuperOrca.olibs

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.IntStream

class GUIManager {
    class InventoryListener(plugin: Plugin) : Listener {
        private val plugin: Plugin

        init {
            this.plugin = plugin
        }

        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            if (e.inventory.holder is GUI && e.clickedInventory != null) {
                val wasCancelled = e.isCancelled
                e.isCancelled = true
                (e.inventory.holder as GUI).handleClick(e)

                if (!wasCancelled && !e.isCancelled) {
                    e.isCancelled = false
                }
            }
        }

        @EventHandler
        fun onInventoryOpen(e: InventoryOpenEvent) {
            if (e.inventory.holder is GUI) {
                (e.inventory.holder as GUI).handleOpen(e)
            }
        }

        @EventHandler
        fun onInventoryClose(e: InventoryCloseEvent) {
            if (e.inventory.holder is GUI) {
                val inv: GUI = e.inventory.holder as GUI
                if (inv.handleClose(e)) {
                    Bukkit.getScheduler().runTask(plugin, Runnable { inv.open(e.player as Player) })
                }
            }
        }

        @EventHandler
        fun onPluginDisable(e: PluginDisableEvent) {
            if (e.plugin === plugin) {
                closeAll()
                REGISTERED.set(false)
            }
        }
    }

    companion object {
        private val REGISTERED = AtomicBoolean(false)

        fun register(plugin: Plugin) {
            Objects.requireNonNull(plugin, "plugin")
            check(!REGISTERED.getAndSet(true)) { "OGUI is already registered" }
            Bukkit.getPluginManager().registerEvents(InventoryListener(plugin), plugin)
        }

        fun closeAll() {
            Bukkit.getOnlinePlayers().stream()
                .filter { p: Player -> p.openInventory.topInventory.holder is GUI }
                .forEach { obj: Player -> obj.closeInventory() }
        }
    }
}

open class GUI(size: Int, type: InventoryType, title: Component) : InventoryHolder {
    private val itemHandlers: MutableMap<Int, Consumer<InventoryClickEvent>?> = mutableMapOf()
    private val openHandlers: MutableList<Consumer<InventoryOpenEvent>> = mutableListOf()
    private val closeHandlers: MutableList<Consumer<InventoryCloseEvent>> = mutableListOf()
    private val clickHandlers: MutableList<Consumer<InventoryClickEvent>> = mutableListOf()

    private val inventory: Inventory
    private var closeFilter: Predicate<Player>? = null

    constructor(size: Int) : this(size, InventoryType.CHEST.defaultTitle())

    constructor(size: Int, title: Component) : this(size, InventoryType.CHEST, title)

    constructor(type: InventoryType) : this(Objects.requireNonNull(type, "type"), type.defaultTitle())

    constructor(type: InventoryType, title: Component) : this(0, Objects.requireNonNull(type, "type"), title)

    init {
        if (type == InventoryType.CHEST && size > 0) {
            this.inventory = Bukkit.createInventory(this, size, title)
        } else {
            this.inventory = Bukkit.createInventory(this, type, title)
        }

        if (this.inventory.holder != this) {
            throw IllegalStateException("Inventory holder is not OGUI, found: " + this.inventory.holder)
        }
    }

    fun onOpen(e: InventoryOpenEvent) {
    }

    fun onClick(e: InventoryClickEvent) {
    }

    fun onClose(e: InventoryCloseEvent) {
    }

    fun addItem(item: ItemStack) {
        addItem(item, null)
    }

    fun addItem(item: ItemStack, handler: Consumer<InventoryClickEvent>?) {
        val slot = this.inventory.firstEmpty()
        if (slot >= 0) {
            setItem(slot, item, handler)
        }
    }

    fun setItem(slot: Int, item: ItemStack) {
        setItem(slot, item, null)
    }

    fun setItem(slot: Int, item: ItemStack, handler: Consumer<InventoryClickEvent>?) {
        this.inventory.setItem(slot, item)

        this.itemHandlers[slot] = handler
    }

    fun setItems(slotFrom: Int, slotTo: Int, item: ItemStack) {
        setItems(slotFrom, slotTo, item, null)
    }

    fun setItems(slotFrom: Int, slotTo: Int, item: ItemStack, handler: Consumer<InventoryClickEvent>?) {
        for (i in slotFrom..slotTo) {
            setItem(i, item, handler)
        }
    }

    fun setItems(slots: IntArray, item: ItemStack) {
        setItems(slots, item, null)
    }

    fun setItems(slots: IntArray, item: ItemStack, handler: Consumer<InventoryClickEvent>?) {
        for (slot in slots) {
            setItem(slot, item, handler)
        }
    }

    fun removeItem(slot: Int) {
        this.inventory.clear(slot)
        this.itemHandlers.remove(slot)
    }

    fun removeItems(vararg slots: Int) {
        for (slot in slots) {
            removeItem(slot)
        }
    }

    fun setCloseFilter(closeFilter: Predicate<Player>) {
        this.closeFilter = closeFilter
    }

    fun addOpenHandler(openHandler: Consumer<InventoryOpenEvent>) {
        this.openHandlers.add(openHandler)
    }

    fun addCloseHandler(closeHandler: Consumer<InventoryCloseEvent>) {
        this.closeHandlers.add(closeHandler)
    }

    fun addClickHandler(clickHandler: Consumer<InventoryClickEvent>) {
        this.clickHandlers.add(clickHandler)
    }

    fun open(player: Player) {
        player.openInventory(this.inventory)
    }

    fun getBorders(): IntArray {
        val size = this.inventory.size
        return IntStream.range(0, size)
            .filter { i -> size < 27 || i < 9 || i % 9 == 0 || (i - 8) % 9 == 0 || i > size - 9 }.toArray()
    }

    fun getCorners(): IntArray {
        val size = this.inventory.size
        return IntStream.range(0, size)
            .filter { i -> i < 2 || (i in 7..9) || i == 17 || i == size - 18 || (i > size - 11 && i < size - 7) || i > size - 3 }
            .toArray()
    }

    override fun getInventory(): Inventory {
        return this.inventory
    }

    fun handleOpen(e: InventoryOpenEvent) {
        onOpen(e)

        this.openHandlers.forEach { c -> c.accept(e) }
    }

    fun handleClose(e: InventoryCloseEvent): Boolean {
        onClose(e)

        this.closeHandlers.forEach { c -> c.accept(e) }

        return this.closeFilter?.test(e.player as Player) ?: false
    }

    fun handleClick(e: InventoryClickEvent) {
        onClick(e)

        this.clickHandlers.forEach { c -> c.accept(e) }

        itemHandlers[e.rawSlot]?.accept(e)
    }
}