package de.gutselcraft.gutselCraft.votesleep.listeners

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.plugin.Plugin

class WorldChangeListener(private val plugin: Plugin) : Listener {
    
    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        try {
            val voteWorld = SleepVoteManager.getVoteWorld()
            
            if (voteWorld != null) {
                val player = event.player
                
                if (player.world == voteWorld) {
                    // Player entered the vote world
                    SleepVoteManager.addToVotes(player)
                } else if (event.from == voteWorld) {
                    // Player left the vote world
                    SleepVoteManager.removeFromVotes(player)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
