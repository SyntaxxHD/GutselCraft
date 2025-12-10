package de.gutselcraft.gutselCraft.welcomebook

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class WelcomeBookListener(private val plugin: Plugin) : Listener {

    private val welcomeBookManager = WelcomeBookManager(plugin)
    private val seenBookKey = NamespacedKey(plugin, "seen_welcome_book")

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // Check if welcome book is enabled
        if (!plugin.config.getBoolean("welcome-book.enabled", true)) {
            return
        }
        
        // Check if player has already seen the welcome book
        if (hasSeenWelcomeBook(player)) {
            return
        }
        
        // Open the welcome book for first-time players
        openWelcomeBook(player)
        
        // Mark player as having seen the book
        markAsSeenWelcomeBook(player)
    }

    private fun hasSeenWelcomeBook(player: Player): Boolean {
        val container = player.persistentDataContainer
        return container.has(seenBookKey, PersistentDataType.BYTE)
    }

    private fun markAsSeenWelcomeBook(player: Player) {
        val container = player.persistentDataContainer
        container.set(seenBookKey, PersistentDataType.BYTE, 1.toByte())
    }

    private fun openWelcomeBook(player: Player) {
        // Schedule the book opening for next tick to ensure player is fully loaded
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (player.isOnline) {
                val book = welcomeBookManager.createWelcomeBook()
                player.openBook(book)
            }
        }, 20L) // Wait 1 second (20 ticks) after join
    }
}
