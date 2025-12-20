package de.gutselcraft.gutselCraft.votesleep.listeners

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.plugin.Plugin

class PlayerBedEnterListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerBedEnter(event: PlayerBedEnterEvent) {
        val player = event.player
        
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return
        
        // Delay by 1 tick to ensure player is actually sleeping
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            // Verify player is still sleeping (they might have left bed immediately)
            if (!player.isSleeping) return@Runnable
            
            if (SleepVoteManager.getVoteWorld() == null) {
                // No vote running - auto-start vote
                SleepVoteManager.startVote(player)
            } else if (SleepVoteManager.getVoteWorld() == player.world) {
                // Vote is running in this world - auto-vote YES
                SleepVoteManager.agreeToSleep(player)
            }
        }, 1L)
    }
}
