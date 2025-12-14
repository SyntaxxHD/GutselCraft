package de.gutselcraft.gutselCraft.joinmessages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class JoinQuitMessageListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Hide the default join message immediately
        // This prevents showing the message with the original username before SimpleNicks loads
        event.joinMessage(null)

        // Schedule a delayed message after SimpleNicks has had time to load the nickname
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            // Use translatable component for language support
            plugin.server.broadcast(
                Component.translatable(
                    "multiplayer.player.joined",
                    Component.text(player.displayName)
                ).color(NamedTextColor.YELLOW)
            )
        }, 5L) // 5 tick delay
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(
            Component.translatable(
                "multiplayer.player.left",
                Component.text(event.player.displayName)
            ).color(NamedTextColor.YELLOW)
        )
    }
}
