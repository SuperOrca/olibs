package com.github.SuperOrca.olibs

import org.bukkit.plugin.java.JavaPlugin

class OLibs : JavaPlugin() {
    override fun onEnable() {
        GUIManager.register(this)
    }
}