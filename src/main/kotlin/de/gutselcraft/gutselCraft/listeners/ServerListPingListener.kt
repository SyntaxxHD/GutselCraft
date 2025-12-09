package de.gutselcraft.gutselCraft.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.plugin.Plugin

class ServerListPingListener(private val plugin: Plugin) : Listener {

    @EventHandler
    fun onServerListPing(event: ServerListPingEvent) {
        val isEnabled = plugin.config.getBoolean("dynamic-slots.enabled", false)
        if (isEnabled) {
            val maxPlayers = plugin.server.onlinePlayers.size + 1
            event.maxPlayers = maxPlayers
        }
    }
}
