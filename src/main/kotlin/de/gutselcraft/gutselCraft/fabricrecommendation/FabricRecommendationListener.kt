package de.gutselcraft.gutselCraft.fabricrecommendation

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

class FabricRecommendationListener(private val plugin: Plugin) : Listener {

    private val checkedPlayers = mutableSetOf<String>()
    private val fabricPlayers = mutableSetOf<String>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // Reset tracking for this player
        checkedPlayers.remove(player.name)
        fabricPlayers.remove(player.name)
        
        // Register plugin channels to detect Fabric
        val fabricChannels = listOf(
            "fabric:container",
            "minecraft:register"
        )
        
        // Mark that we're checking this player
        checkedPlayers.add(player.name)
        
        // Wait a moment for the client to send their plugin channels
        object : BukkitRunnable() {
            override fun run() {
                // Check if player is still online
                if (!player.isOnline) {
                    checkedPlayers.remove(player.name)
                    return
                }
                
                // Check if we detected Fabric (via the channel registration)
                if (!fabricPlayers.contains(player.name)) {
                    // Player is likely using vanilla client
                    sendFabricRecommendation(player)
                }
                
                // Cleanup
                checkedPlayers.remove(player.name)
            }
        }.runTaskLater(plugin, 40L) // Wait 2 seconds (40 ticks)
    }

    /**
     * Check if a player has Fabric by examining their registered channels
     */
    fun checkForFabric(player: Player): Boolean {
        // Check if player has any Fabric-specific channels registered
        val channels = player.getListeningPluginChannels()
        
        return channels.any { channel ->
            channel.contains("fabric", ignoreCase = true) ||
            channel.contains("modloader", ignoreCase = true)
        }
    }

    private fun sendFabricRecommendation(player: Player) {
        // Double-check for Fabric channels one more time
        if (checkForFabric(player)) {
            fabricPlayers.add(player.name)
            return
        }
        
        // Send chat message recommending Fabric with clickable link
        val message = Component.text("Fabric wird für diesen Server empfohlen! Am einfachsten installierst du es über die ")
            .color(NamedTextColor.RED)
            .append(
                Component.text("Modrinth Desktop App")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl("https://modrinth.com/app"))
            )
            .append(
                Component.text(".")
                    .color(NamedTextColor.RED)
            )
        
        player.sendMessage(message)
    }
}
