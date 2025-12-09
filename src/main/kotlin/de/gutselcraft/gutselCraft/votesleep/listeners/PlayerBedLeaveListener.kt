package de.gutselcraft.gutselCraft.votesleep.listeners

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.plugin.Plugin

class PlayerBedLeaveListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerBedLeave(event: PlayerBedLeaveEvent) {
        val player = event.player
        
        // If a vote is running, unvote this player
        if (SleepVoteManager.getVoteWorld() != null) {
            SleepVoteManager.unvotePlayer(player)
        }
    }
}
