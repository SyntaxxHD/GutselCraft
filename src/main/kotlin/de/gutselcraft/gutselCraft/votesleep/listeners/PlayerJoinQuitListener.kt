package de.gutselcraft.gutselCraft.votesleep.listeners

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PlayerJoinQuitListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (SleepVoteManager.getVoteWorld() != null) {
            SleepVoteManager.removeFromVotes(event.player)
        }
    }
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (SleepVoteManager.getVoteWorld() != null) {
            SleepVoteManager.addToVotes(event.player)
        }
    }
}
