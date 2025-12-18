package de.gutselcraft.gutselCraft.nickname

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class NicknameDisplayListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage(null)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.server.broadcast(
                Component.translatable(
                    "multiplayer.player.joined",
                    player.displayName()
                ).color(NamedTextColor.YELLOW)
            )
            
            updateOverheadName(player)
        }, 5L)
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(
            Component.translatable(
                "multiplayer.player.left",
                event.player.displayName()
            ).color(NamedTextColor.YELLOW)
        )
    }
    
    private fun updateOverheadName(player: org.bukkit.entity.Player) {
        player.customName(player.displayName())
        player.isCustomNameVisible = true
    }
}
