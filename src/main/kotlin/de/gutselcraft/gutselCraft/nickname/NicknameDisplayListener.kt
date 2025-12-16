package de.gutselcraft.gutselCraft.nickname

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
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
    
    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        
        val display = advancement.display ?: return
        
        val messageKey = when (display.frame()) {
            io.papermc.paper.advancement.AdvancementDisplay.Frame.CHALLENGE -> "chat.type.advancement.challenge"
            io.papermc.paper.advancement.AdvancementDisplay.Frame.GOAL -> "chat.type.advancement.goal"
            else -> "chat.type.advancement.task"
        }
        
        event.message(null)
        
        plugin.server.broadcast(
            Component.translatable(
                messageKey,
                player.displayName(),
                display.title()
            )
        )
    }
    
    private fun updateOverheadName(player: org.bukkit.entity.Player) {
        player.customName(player.displayName())
        player.isCustomNameVisible = true
    }
}
