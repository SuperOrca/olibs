package io.github.superorca.olibs

import org.bukkit.plugin.java.JavaPlugin

class OLibs : JavaPlugin() {
    override fun onEnable() {
        GUIManager.register(this)
    }
}