package de.gutselcraft.gutselCraft.votesleep.listeners

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.plugin.Plugin

class GameModeChangeListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        
        // Delay execution to allow game mode to update
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (SleepVoteManager.getVoteWorld() != null) {
                if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE) {
                    SleepVoteManager.addToVotes(player)
                } else {
                    SleepVoteManager.removeFromVotes(player)
                }
            }
        }, 1L)
    }
}
